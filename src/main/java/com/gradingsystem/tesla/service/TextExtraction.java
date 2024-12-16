package com.gradingsystem.tesla.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

@Service
public class TextExtraction {

    // Extract text from a PDF file
    public String extractTextFromPDF(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            String normalizedText = text.toLowerCase().replaceAll("\\s+", " ").trim();
            return normalizedText;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF document");
        }
    }

    // Extract text from a Word document
    public String extractTextFromWord(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            // Normalize text: Convert text to lowercase and remove extra spaces
            String normalizedText = text.toString().toLowerCase().replaceAll("\\s+", " ").trim();
            return normalizedText;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from Word document");
        }
    }
    // Extract text from pdf or word file
    public String extractText(MultipartFile file) throws Exception {
        // Check if file exists
        if (file == null || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("File or filename is null");
        }
        
        String extractedText;
        
        if (file.getOriginalFilename().endsWith(".pdf")) {
            extractedText = extractTextFromPDF(file.getInputStream());
        } else if (file.getOriginalFilename().endsWith(".docx")) {
            extractedText = extractTextFromWord(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Unsupported file type");
        }

        // Clean and normalize the extracted text
        String cleanText = cleanText(extractedText);
        return cleanText;
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        // Remove non-printable characters and extra spaces
        text = text.replaceAll("\\p{C}", "") // Remove control characters
                .replaceAll("\\s+", " ") // Replace multiple whitespaces with a single space
                .trim();                        // Trim leading and trailing whitespace

        return ensureUTF8(text);
    }

    // Convert Text to UTF String
    private String ensureUTF8(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8); // Convert to UTF-8
        return new String(bytes, StandardCharsets.UTF_8);     // Reconstruct the String
    }

    // Generate a hash value for a text
    public String generateHash(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
