package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.repository.AssignmentRepository;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import com.gradingsystem.tesla.repository.StudentRepository;
import com.gradingsystem.tesla.service.CohereGradingService;
import com.gradingsystem.tesla.service.PlagiarismService;
import com.gradingsystem.tesla.service.TextExtraction;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final PlagiarismService plagiarismService;
    private final CohereGradingService gradingService;
    private final TextExtraction textExtraction;

    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final AssignmentRepository assignmentRepository;

    @Autowired
    public SubmissionController(
            PlagiarismService plagiarismService,
            CohereGradingService gradingService,
            DocumentSubmissionRepository documentSubmissionRepository,
            AssignmentRepository assignmentRepository,
            StudentRepository studentRepository,
            TextExtraction textExtraction) {
        this.plagiarismService = plagiarismService;
        this.gradingService = gradingService;
        this.documentSubmissionRepository = documentSubmissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.textExtraction = textExtraction;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, String>> evaluateSubmission(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Long assignmentId = (Long) session.getAttribute("assignmentId");
        Student student = (Student) session.getAttribute("loggedInStudent");
        
        try {
            // Extract and hash text
            String newSubmission = textExtraction.extractText(file);
            String newSubmissionHash = textExtraction.generateHash(newSubmission);

            // Check for duplicate submissions
            DocumentSubmission duplicateSubmission = documentSubmissionRepository
                    .findByAssignmentIdAndHashValue(assignmentId, newSubmissionHash);

            if (duplicateSubmission != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Duplicate submission detected for this assignment."));
            }

            // Check for plagiarism
            double plagiarismScore = 0;
            List<DocumentSubmission> allSubmissions = documentSubmissionRepository.findAll();
            for (DocumentSubmission submission : allSubmissions) {
                // Convert extracted text to string (if it's stored as bytes or other format)
                String existingSubmission = new String(submission.getExtractedText(), StandardCharsets.UTF_8);
         
                try {
                    // Calculate TF-IDF similarity using Cosine Similarity
                    double score = plagiarismService.calculateTFIDFSimilarity(newSubmission, existingSubmission);
                    if (score > plagiarismScore) {
                        plagiarismScore = score;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Check if plagiarism score exceeds thresholds
                if (plagiarismScore >= 0.9) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Duplicate submission detected for this assignment."));
                }
            }
            
            // logging purposes
            if (plagiarismScore >= 0.7) { // threshold: 30%
                System.out.println("Potential plagiarism detected: " + plagiarismScore);
            }

            Integer percentage;
            if (plagiarismScore < 0.7) {
                // Parse questions and answers
                Map<String, String> answerToQuestion = gradingService.parseQuestionsAndAnswers(newSubmission);

                Map<String, String> evaluationResults;

                Assignment assignment = assignmentRepository.findAssignmentById(assignmentId);
                byte[] rubric = assignment.getRubric();

                String rubricText;
                if (rubric != null) {
                    rubricText = new String(rubric, StandardCharsets.UTF_8);
                    // Evaluate based on rubric
                    evaluationResults = gradingService.evaluateAnswersWithRubric(answerToQuestion, rubricText);
                } else {
                    // Evaluate without rubric
                    evaluationResults = gradingService.evaluateAnswersWithoutRubric(answerToQuestion);
                }

                // Iterate through the map and print each key-value pair
                for (Map.Entry<String, String> entry : evaluationResults.entrySet()) {
                    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                }

                System.out.print("Entry Size: " + evaluationResults.entrySet().size());

                // Calculate student's mark for assignment
                Map<String, Integer> scores = gradingService.calculateAggregateScore(evaluationResults);
                int totalScore = scores.get("totalScore");
                int totalMaxScore = scores.get("totalMaxScore");
                double aggregateScore = (double) totalScore / totalMaxScore;
                percentage = (int) (aggregateScore * 100);
            } else {
                percentage = 0;
            }

            // Retrieve the assignment
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            // Save document submission
            DocumentSubmission submission = DocumentSubmission.builder()
                    .student(student)
                    .assignment(assignment)
                    .hashValue(newSubmissionHash)
                    .extractedText(newSubmission.getBytes(StandardCharsets.UTF_8))
                    .grade(percentage)
                    .similarityScore((int) (plagiarismScore * 100))
                    .build();

            System.out.println("Assignment: " + assignment.getId() + ", " + assignment.getTitle() + ", " + assignment.getDescription());
            System.out.println("Student: " + student.getId() + ", " + student.getUsername() + ", " + student.getEmail());
            System.out.println("Grade: " + percentage);
            DecimalFormat numberFormat = new DecimalFormat("#.00");
            System.out.println("PlagiarismScore: " + plagiarismScore);
            System.out.println("SubmissionText: " + newSubmission);
            System.out.println("HashValue: " + newSubmissionHash);
            System.out.println("SubmissionBytes: " + Arrays.toString(newSubmission.getBytes(StandardCharsets.UTF_8)) + "\n\n");

            documentSubmissionRepository.save(submission);

            if (plagiarismScore < 0.7) {
                return ResponseEntity.ok(Map.of("message", "Submission Processed", "score", String.valueOf(percentage)));
            } else {
                return ResponseEntity.ok(Map.of("message", "Submission Processed: Plagiarism Detected!", "Similarity Score", String.valueOf(numberFormat.format(plagiarismScore * 100))));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
