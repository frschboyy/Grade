package com.gradingsystem.tesla.service;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;
import org.apache.lucene.util.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlagiarismService {

    @Autowired
    private TextPreprocessing textPreprocessing;

    // Method to calculate TF-IDF similarity using Cosine Similarity
    public double calculateTFIDFSimilarity(String newText, String existingText) throws IOException {
        // Clean the incoming text
        String text1 = textPreprocessing.preprocessText(newText);
        String text2 = textPreprocessing.preprocessText(existingText);

        // Create a Lucene in-memory directory to store the indexed data
        Directory directory = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer(); // for tokenizing text
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // Index the two documents
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            // Create and add the first document
            Document newDocument = new Document();
            newDocument.add(new Field("content", text1, CustomFieldType.TYPE_STORED_WITH_TERM_VECTORS));
            writer.addDocument(newDocument);

            // Create and add the second document
            Document existingDocument = new Document();
            existingDocument.add(new Field("content", text2, CustomFieldType.TYPE_STORED_WITH_TERM_VECTORS));
            writer.addDocument(existingDocument);
        }

        // Retrieve the term vectors from the index
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            Terms vector1 = reader.getTermVector(0, "content"); // new document
            Terms vector2 = reader.getTermVector(1, "content"); // existing document

            // Handle cases where term vectors might be null
            if (vector1 == null || vector2 == null) {
                throw new RuntimeException("Term vectors are null. Indexing might have failed.");
            }

            // Compute TF-IDF maps for both documents
            Map<String, Double> tfidf1 = getTFIDFMap(vector1, reader);
            Map<String, Double> tfidf2 = getTFIDFMap(vector2, reader);

            // Calculate and return the cosine similarity between the two TF-IDF vectors
            return computeCosineSimilarity(tfidf1, tfidf2);
        }
    }

    // Extract term frequencies from a document's term vector
    private Map<String, Double> getTFIDFMap(Terms terms, IndexReader reader) throws IOException {
        Map<String, Double> tfidfMap = new HashMap<>();
        TermsEnum termsEnum = terms.iterator(); // Iterate through the terms in the term vector
        BytesRef term;

        while ((term = termsEnum.next()) != null) {
            String termText = term.utf8ToString(); // Convert the term to a string
            int termFreq = (int) termsEnum.totalTermFreq(); // Get the term frequency in the document
            int docFreq = reader.docFreq(new Term("content", termText)); // Get the number of documents containing the term

            // Calculate Inverse Document Frequenct (IDF): log(N / (1 + DF))
            double idf = Math.log((double) reader.numDocs() / (1 + docFreq));

            // Calculate TF-IDF: TF * IDF
            double tfidf = termFreq * idf;

            // Store the TF-IDF value for the term
            tfidfMap.put(termText, tfidf);
        }

        return tfidfMap;
    }

    // Compute cosine similarity between two term frequency maps
    private double computeCosineSimilarity(Map<String, Double> newDoc, Map<String, Double> existingDoc) {
        // Create a set of all unique terms from both documents
        Set<String> terms = new HashSet<>(newDoc.keySet());
        terms.addAll(existingDoc.keySet());

        // Initialize the dot product and norms for cosine similarity
        double dotProduct = 0;
        double normNewDoc = 0;
        double normExisingDoc = 0;

        // Iterate through each unque term
        for (String term : terms) {
            // Get TF-IDF values for the term in both documents
            double freqNewDoc = newDoc.getOrDefault(term, 0.0);
            double freqExisingDoc = existingDoc.getOrDefault(term, 0.0);

            // Update the dot product and norms
            dotProduct += freqNewDoc * freqExisingDoc;
            normNewDoc += freqNewDoc * freqNewDoc;
            normExisingDoc += freqExisingDoc * freqExisingDoc;
        }

        // Calculate and return cosine similarity
        return dotProduct / (Math.sqrt(normNewDoc) * Math.sqrt(normExisingDoc));
    }

    private class CustomFieldType {

        public static final FieldType TYPE_STORED_WITH_TERM_VECTORS;

        static {
            TYPE_STORED_WITH_TERM_VECTORS = new FieldType();
            TYPE_STORED_WITH_TERM_VECTORS.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            TYPE_STORED_WITH_TERM_VECTORS.setStored(true);
            TYPE_STORED_WITH_TERM_VECTORS.setStoreTermVectors(true);
            TYPE_STORED_WITH_TERM_VECTORS.setStoreTermVectorPositions(true);
            TYPE_STORED_WITH_TERM_VECTORS.setStoreTermVectorOffsets(true);
            TYPE_STORED_WITH_TERM_VECTORS.freeze();
        }
    }
}
