package com.gradingsystem.tesla.service;

import com.gradingsystem.tesla.DTO.EvaluationDetails;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RetrieveEvaluationService {
    
    @Autowired
    DocumentSubmissionRepository submissionRepository;
    
    public EvaluationDetails getEvaluationDetails(Long assignmentId, Long studentId){
        DocumentSubmission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        
        return EvaluationDetails.builder()
                .plagiarismScore(submission.getSimilarityScore())
                .grade(submission.getGrade())
                .build();
    }
}
