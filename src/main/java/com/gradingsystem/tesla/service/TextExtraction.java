package com.gradingsystem.tesla.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TextExtraction {

    private final CohereGradingService gradingService;

    @Autowired
    public TextExtraction(CohereGradingService gradingService) {
        this.gradingService = gradingService;
    }

    // Extract text from pdf or word file
    public String extractText(MultipartFile file) throws Exception {
        // Check if file exists
        if (file == null || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("File or filename is null");
        }
        
        String extractedText;
        
        if (file.getOriginalFilename().endsWith(".pdf")) {
            extractedText = gradingService.extractTextFromPDF(file.getInputStream());
        } else if (file.getOriginalFilename().endsWith(".docx")) {
            extractedText = gradingService.extractTextFromWord(file.getInputStream());
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
