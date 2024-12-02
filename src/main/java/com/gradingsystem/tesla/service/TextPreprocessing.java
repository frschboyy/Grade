package com.gradingsystem.tesla.service;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.stemmer.PorterStemmer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TextPreprocessing {

    public String preprocessText(String text) {
        // Step 1: Normalize text
        String normalizedText = text.toLowerCase().replaceAll("\\s+", " ").trim();

        try (InputStream modelIn = getClass().getClassLoader().getResourceAsStream("en-token.bin")) {
            if (modelIn == null) {
                throw new IOException("Tokenization model file not found");
            }
            
            // Step 2: Tokenize
            TokenizerModel model = new TokenizerModel(modelIn);
            TokenizerME tokenizer = new TokenizerME(model);
            String[] tokens = tokenizer.tokenize(normalizedText);

            // Step 3: Remove stopwords
            String[] filteredTokens = Arrays.stream(tokens)
                    .filter(token -> !STOPWORDS.contains(token))
                    .toArray(String[]::new);

            // Step 4: Stem tokens
            PorterStemmer stemmer = new PorterStemmer();
            String[] stemmedTokens = Arrays.stream(filteredTokens)
                    .map(stemmer::stem)
                    .toArray(String[]::new);

            // Join tokens back into a single string
            return String.join(" ", stemmedTokens);

        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("text documents cannot be empty");
    }
    // Set of stop words
    private final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at",
            "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
            "can", "can't", "cannot", "could", "couldn't",
            "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during",
            "each",
            "few", "for", "from", "further",
            "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's",
            "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself",
            "let's",
            "me", "more", "most", "mustn't", "my", "myself",
            "no", "nor", "not",
            "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own",
            "same", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such",
            "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too",
            "under", "until", "up",
            "very",
            "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't",
            "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"
    ));
}
