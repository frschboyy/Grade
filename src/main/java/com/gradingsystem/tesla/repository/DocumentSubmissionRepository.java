package com.gradingsystem.tesla.repository;

import com.gradingsystem.tesla.model.DocumentSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentSubmissionRepository extends JpaRepository<DocumentSubmission, Long> {    
    List<DocumentSubmission> findByAssignmentId(Long assignmentId);
    
    List<DocumentSubmission> findByStudentId(Long studentId);

    DocumentSubmission findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    DocumentSubmission findByAssignmentIdAndStudentIdAndHashValue(Long assignmentId, Long studentId, String hashValue);
}
