package com.gradingsystem.tesla.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CohereService {

    @Value("${cohere.api.key}")
    private String apiKey;

    @Value("${cohere.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public CohereService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateResponse(String prompt) {
        try {
            // Ensure there's no '\r' in the string
            String requestBody = String.format("{\"model\": \"command-xlarge-20221108\", \"prompt\": \"%s\", \"max_tokens\": 100}", prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            return response.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            return "Error: Unable to generate response. Please try again later.";
        }
//        try {
//            // Build request body dynamically
//            Map<String, Object> requestBodyMap = new HashMap<>();
//            requestBodyMap.put("model", "command-xlarge-20221108");
//            requestBodyMap.put("prompt", prompt);
//            requestBodyMap.put("max_tokens", 20); // Set max_tokens programmatically
//
//            // Convert map to JSON string
//            String requestBody = objectMapper.writeValueAsString(requestBodyMap);
//
//            // Set headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer " + apiKey);
//
//            // Create HttpEntity with headers and body
//            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//            // Send POST request
//            ResponseEntity<String> response = restTemplate.exchange(
//                    apiUrl,
//                    HttpMethod.POST,
//                    entity,
//                    String.class
//            );
//
//            // Return response body
//            return response.getBody();
//
//        } catch (JsonProcessingException | RestClientException e) {
//            e.printStackTrace();
//            return "Error: Unable to generate response.";
//        }
    }
}
