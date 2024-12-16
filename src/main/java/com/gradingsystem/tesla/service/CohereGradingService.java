package com.gradingsystem.tesla.service;

import com.cohere.api.Cohere;
import com.cohere.api.resources.v2.requests.V2ChatRequest;
import com.cohere.api.types.AssistantMessageResponseContentItem;
import com.cohere.api.types.ChatMessageV2;
import com.cohere.api.types.ChatResponse;
import com.cohere.api.types.UserMessageContent;
import com.cohere.api.types.UserMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CohereGradingService {

    private static final Logger logger = LoggerFactory.getLogger(CohereGradingService.class);

    @Value("${cohere.api.key}")
    private String apiKey;

    @Value("${cohere.api.model}")
    private String apiModel;

    private Cohere cohere;

    // PostConstruct ensures this is run after dependencies are injected
    @PostConstruct
    public void init() {
        // Ensure API Key and Model are not null or empty before using them
        if (apiKey == null || apiModel == null || apiKey.isEmpty() || apiModel.isEmpty()) {
            throw new IllegalArgumentException("API Key and Model cannot be null or empty");
        }
        this.cohere = Cohere.builder()
                .token(apiKey)
                .clientName("grading-system")
                .build();
    }

    // Parse questions and answers from extracted text
    public Map<String, String> parseQuestionsAndAnswers(String documentText) {
        logger.debug("Hello parser 0");
        Map<String, String> qaPairs = new LinkedHashMap<>();

        String regEx = "(?i)question:\\s*(.*?)\\s*answer:\\s*(.*?)(?=question:|$)";
        Pattern pattern = Pattern.compile(regEx, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(documentText);
        int count = 1;
        logger.debug("Hello parser");
        while (matcher.find()) {
            logger.debug("Match Found (" + count + ")");
            String question = matcher.group(1).trim();
            logger.debug("Question " + count + ": {}", question);
            String answer = matcher.group(2).trim();
            logger.debug("Answer " + count + ": {}", answer);
            qaPairs.put(question, answer);
            count++;
        }
        return qaPairs;
    }

    // Evaluate answers without a rubric
    public Map<String, String> evaluateAnswersWithoutRubric(Map<String, String> qaPairs) {
        logger.debug("Hello evaluator");

        Map<String, String> results = new LinkedHashMap<>();

        qaPairs.forEach((question, answer) -> {
            logger.debug("Question123: {}", question);
            logger.debug("Answer: {}", answer);

            String prompt = """
                        Evaluate the following answer to the question on a scale of 1 to 10. Only respond with a score (e.g., 3/10).
                        Question: %s
                        Answer: %s
                        """.formatted(question, answer);

            try {
                String evaluation = callAIAPI(prompt); // Cohere API used here
                results.put(question, evaluation);
                logger.debug("Evaluation: {}", evaluation);
            } catch (Exception e) {
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
            logger.debug("Question: {}", question);
            logger.debug("Student Answer: {}", studentAnswer);
            logger.debug("Rubric Answer: {}", rubricAnswer);
            String prompt = """
                        On a scale of 1 to 10, how well does the student's answer align with the teacher's answer? Only respond with a score (e.g. 3/10).
                        Question: %s
                        Rubric's Answer: %s
                        Student's Answer: %s
                        """.formatted(question, rubricAnswer, studentAnswer);

            try {
                String evaluation = callAIAPI(prompt); // Cohere API used here
                results.put(question, evaluation);
                logger.debug("Evaluation: {}", rubricAnswer);
            } catch (Exception e) {
                results.put(question, "Error evaluating answer: " + e.getMessage());
            }
        });
        return results;
    }

    public String callAIAPI(String prompt) {
        try {
            ChatResponse response = cohere.v2()
                    .chat(
                            V2ChatRequest.builder()
                                    .model(apiModel)
                                    .messages(
                                            List.of(
                                                    ChatMessageV2.user(
                                                            UserMessage.builder()
                                                                    .content(UserMessageContent
                                                                            .of(prompt))
                                                                    .build())))
                                    .build());

            String responseBody = "" + response;
            logger.debug("Response Body: " + responseBody);
            String evaluation = extractEvaluation(responseBody);
            logger.debug("Extracted Evaluation: " + evaluation);
            return evaluation;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to generate response.";
        }
    }

    //  Add scores together
    public Integer calculateAggregateScore(Map<String, String> evaluationResults) {
        logger.debug("Hello aggregator");
        logger.debug("Evaluation Map: " + evaluationResults);

        int totalScore = 0;
        int totalMaxScore = 0;

        // Loop through the evaluation results
        for (String evaluation : evaluationResults.values()) {
            int[] scores = extractScores(evaluation); // Extract both score and max score
            totalScore += scores[0];   // Add student score
            totalMaxScore += scores[1]; // Add max score
        }

        if (totalMaxScore == 0) {
            logger.error("Total max score is zero. Unable to calculate aggregate score.");
            throw new IllegalStateException("Total max score cannot be zero.");
        }
        logger.debug("Total Score: {}, Total Max Score: {}", totalScore, totalMaxScore);

        //calculate final mark
        double aggregateScore = (double) totalScore / totalMaxScore;
        Integer percentage = (int) (aggregateScore * 100);
        return percentage;
    }

    //  Extract both student score and max score from each question's evaluation
    public int[] extractScores(String evaluation) {
    // Extract the scores in the format "10/10"
    String regEx = "(\\d+)/(\\d+)";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(evaluation);

        if (matcher.find()) {
            int studentScore = Integer.parseInt(matcher.group(1));
            int maxScore = Integer.parseInt(matcher.group(2));
            logger.debug("Extracted Evaluation Results: " + studentScore + "/" + maxScore);
            return new int[]{studentScore, maxScore};
        }
        logger.warn("No scores found in evaluation: {}", evaluation);
        return new int[]{0, 10}; // Default to 0/10 if no score is found
    }

    // Extract text
    public static String extractEvaluation(String responseBody) {
        // the regex to match the "content" field
        String regex = "\"text\"\\s*:\\s*\"(\\d+/\\d+)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(responseBody);

        // Extract the first match
        if (matcher.find()) {
            return matcher.group(1); // Group 1 is the captured text
        } else {
            return "No match found!";
        }
    }
}
