package com.gradingsystem.tesla.repository;

import com.gradingsystem.tesla.model.Assignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query("SELECT a.rubric FROM Assignment a WHERE a.id = :id")
    byte[] findRubricById(@Param("id") Long id);    
    
    // Fetch all assignments with a due date in the future
    @Query("SELECT a FROM Assignment a WHERE a.dueDate > CURRENT_DATE")
    List<Assignment> findAllUpcomingAssignments();

    @Query("SELECT a FROM Assignment a WHERE a.id = :id")
    Assignment findAssignmentById(@Param("id") Long id);
}


