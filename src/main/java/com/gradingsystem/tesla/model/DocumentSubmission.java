package com.gradingsystem.tesla.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_submission")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id")    // Foreign Key Column
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "assignment_id") // Foreign Key Column
    private Assignment assignment;
        
    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] extractedText;
    
    @Column(nullable=false, length = 64) // SHA-256 produces 64-character hex strings
    private String hashValue;
    
    @Column(nullable=true)
    private Integer grade;
    
    @Column(nullable=true)
    private Integer similarityScore;
}