package com.gradingsystem.tesla.service;

import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student getStudent(String username) {
        return studentRepository.findByUsername(username);
    }
}
