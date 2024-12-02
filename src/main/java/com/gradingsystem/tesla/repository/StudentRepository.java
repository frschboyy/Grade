package com.gradingsystem.tesla.repository;


import com.gradingsystem.tesla.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    public Student findByUsername(String name);
}
