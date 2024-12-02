package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.DTO.AssignmentDTO;
import com.gradingsystem.tesla.DTO.EvaluationDetails;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.RetrieveEvaluationService;
import com.gradingsystem.tesla.service.TextExtraction;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final TextExtraction textExtraction;
    private final AssignmentService assignmentService;
    private final RetrieveEvaluationService retrievalService;

    @Autowired
    public AssignmentController(
            AssignmentService assignmentService,
            RetrieveEvaluationService retrievalService,
            TextExtraction textExtraction) 
    {
        this.assignmentService = assignmentService;
        this.retrievalService = retrievalService;
        this.textExtraction = textExtraction;
    }

    //  Fetch unsubmitted assignments
    @GetMapping("/upcoming")
    public List<Assignment> getUnsubmittedAssignments(HttpSession session) {
        Long studentId = (Long) session.getAttribute("id");
        return assignmentService.getUpcomingAssignments(studentId);
    }

    //  Fetch submitted assignments
    @GetMapping("/submitted")
    public List<Assignment> getSubmittedAssignments(HttpSession session) {
        Long studentId = (Long) session.getAttribute("id");
        return assignmentService.getSubmittedAssignments(studentId);
    }

    // Endpoint to create a new assignment
    @PostMapping
    public ResponseEntity<String> createAssignment(
            @RequestParam("assignmentName") String assignmentName,
            @RequestParam("dueDate") String dueDate,
            @RequestParam("description") String description,
            @RequestParam(value = "uploadFile", required = false) MultipartFile file) {

        // Validate fields
        if (assignmentName == null || dueDate == null || description == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: Missing required fields");
        }

        try {
            LocalDateTime dueDateTime = LocalDateTime.parse(dueDate);

            // Create the assignment
            Assignment assignment = Assignment.builder()
                    .description(description)
                    .dueDate(dueDateTime)
                    .title(assignmentName)
                    .build();

            // Handle optional file
            if (file != null && !file.isEmpty()) {
                String extractedRubric = textExtraction.extractText(file);
                System.out.println("proposed rubric: " + extractedRubric);
                assignment.setRubric(extractedRubric.getBytes(StandardCharsets.UTF_8));
            } else {
                assignment.setRubric(null);
            }

            System.out.println("new rubric: " + Arrays.toString(assignment.getRubric()));

            // Save the assignment
            Assignment createdAssignment = assignmentService.createAssignment(assignment);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Assignment '" + createdAssignment.getTitle() + "' added successfully");

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating assignment: " + ex.getMessage());
        }
    }

    @PostMapping("/pushAssignmentDetails")
    public String saveAssignmentDetails(@RequestBody AssignmentDTO details, HttpSession session) {

        System.out.println(details);
        System.out.println(details.getId());
        System.out.println(details.getTitle());
        System.out.println(details.getDescription());
        System.out.println(details.getDueDate());

        // Save data
        session.setAttribute("assignmentId", details.getId());
        session.setAttribute("title", details.getTitle());
        session.setAttribute("description", details.getDescription());
        session.setAttribute("dueDate", details.getDueDate());
        System.out.println("Added to session");

        return "redirect:/submit-page";
    }

    @PostMapping("/pushEvaluationDetails")
    public String saveEvaluationDetails(@RequestParam Long assignmentId, HttpSession session) {
        // Fetch evaluation details
        EvaluationDetails details = retrievalService.getEvaluationDetails(assignmentId, (Long) session.getAttribute("id"));

        // Save data
        session.setAttribute("grade", details.getGrade());
        session.setAttribute("plagiarism", details.getPlagiarismScore());
        System.out.println("Added to session");
        return "redirect:/evaluation-page";
    }
}
