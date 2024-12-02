package com.gradingsystem.tesla.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
//import java.net.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CohereGradingService {
//    private static final Logger logger = LoggerFactory.getLogger(CohereGradingService.class);

    @Value("${cohere.api.key}")
    private String apiKey;

    @Value("${cohere.api.url}")
    private String apiUrl;
    
    private static final String MODEL = "command-xlarge-nightly";
    private static final double temperature = 0.7;
    private static final int maxTokens = 300;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CohereGradingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Extract text from a PDF file
    public String extractTextFromPDF(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            String normalizedText = text.toLowerCase().replaceAll("\\s+", " ").trim();
            return normalizedText;
        } catch (IOException e) {
//            logger.error("Error extracting text from PDF", e);
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
//            logger.error("Error extracting text from Word document", e);
            throw new RuntimeException("Failed to extract text from Word document");
        }
    }

    // Parse questions and answers from extracted text
    public Map<String, String> parseQuestionsAndAnswers(String documentText) {
        Map<String, String> qaPairs = new LinkedHashMap<>();
        String regEx = "Question:\\s*(.*?)\\s*Answer:\\s*(.*?)(?=Question:|$)";
        Pattern pattern = Pattern.compile(regEx, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(documentText);

        while (matcher.find()) {
            String question = matcher.group(1).trim();
            String answer = matcher.group(2).trim();
            qaPairs.put(question, answer);
        }
        return qaPairs;
    }

    // Evaluate answers without a rubric
    public Map<String, String> evaluateAnswersWithoutRubric(Map<String, String> qaPairs) {
        Map<String, String> results = new LinkedHashMap<>();

        qaPairs.forEach((question, answer) -> {
            String prompt = """
                            Evaluate the following answer to the question on a scale of 1 to 10. Only respond with a score (e.g. 3/10).
                            Question: %s
                            Answer: %s
                            """.formatted(question, answer);

            try {
                String evaluation = callAIAPI(prompt);
                results.put(question, evaluation);
            } catch (Exception e) {
//                logger.error("Error evaluating answer for question: " + question, e);
                results.put(question, "Error evaluating answer: " + e.getMessage());
            }
        });
        return results;
    }

    // Evaluate answers with a rubric
    public Map<String, String> evaluateAnswersWithRubric(Map<String, String> qaPairs, String rubricText) {
        Map<String, String> results = new LinkedHashMap<>();
        Map<String, String> rubricQA = parseQuestionsAndAnswers(rubricText);

        qaPairs.forEach((question, studentAnswer) -> {
            String rubricAnswer = rubricQA.getOrDefault(question, ""); // Use rubric answer if available
            String prompt = """
                            On a scale of 1 to 10, how well does the student's answer align with the teacher's answer? Only respond with a score (e.g. 3/10).
                            Question: %s
                            Rubric's Answer: %s
                            Student's Answer: %s
                            """.formatted(question, rubricAnswer, studentAnswer);

            try {
                String evaluation = callAIAPI(prompt);
                results.put(question, evaluation);
            } catch (Exception e) {
//                logger.error("Error evaluating answer with rubric for question: " + question, e);
                results.put(question, "Error evaluating answer: " + e.getMessage());
            }
        });
        return results;
    }

    // Call the OpenAI API for evaluation
    public String callAIAPI(String prompt) {
        try {
            // Build request body dynamically
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", "command-xlarge-nightly");
            requestBodyMap.put("prompt", prompt);
            requestBodyMap.put("max_tokens", maxTokens); 
            
            // Convert map to JSON string
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // Create HttpEntity with headers and body
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // Send POST request
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Return response body
            return parseAIResponse(response.getBody());

        } catch (JsonProcessingException | RestClientException e) {
            e.printStackTrace();
            return "Error: Unable to generate response.";
        }
    }

    private String parseAIResponse(String responseBody) {
        try {
            // Parse the response JSON using ObjectMapper
            JsonNode rootNode = objectMapper.readTree(responseBody);
            // Access the "generations" array
            JsonNode generationsNode = rootNode.get("generations");

            // Ensure the "generations" node exists and is an array with at least one element
            if (generationsNode != null && generationsNode.isArray() && generationsNode.size() > 0) {
                // Extract the first generation's "text" field
                return generationsNode.get(0).get("text").asText().trim();
            } else {
                throw new RuntimeException("Invalid response: 'generations' node is missing or empty.");
            }
        } catch (JsonProcessingException e) {
            // Log or handle parsing error
//        logger.error("Error parsing AI response", e);
            throw new RuntimeException("Error parsing AI response: " + e.getMessage());
        }
    }

    //  Add scores together
    public Map<String, Integer> calculateAggregateScore(Map<String, String> evaluationResults) {
        int totalScore = 0;
        int totalMaxScore = 0;

        // Loop through the evaluation results
        for (String evaluation : evaluationResults.values()) {
            int[] scores = extractScores(evaluation); // Extract both score and max score
            totalScore += scores[0];   // Add student score
            totalMaxScore += scores[1]; // Add max score
        }

        // Return both the total score and the max score
        return Map.of("totalScore", totalScore, "totalMaxScore", totalMaxScore);
    }

    //  Extract both student score and max score from each question's evaluation
    public int[] extractScores(String evaluation) {
        String regEx = "Score:\\s*(\\d+)/(\\d+)"; // Extract both the student score and max score
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(evaluation);

        if (matcher.find()) {
            int studentScore = Integer.parseInt(matcher.group(1));
            int maxScore = Integer.parseInt(matcher.group(2));
            return new int[]{studentScore, maxScore};
        }
//        logger.warn("No score found in evaluation: " + evaluation);
        return new int[]{0, 10}; // Default to 0/10 if no score is found
    }
}
