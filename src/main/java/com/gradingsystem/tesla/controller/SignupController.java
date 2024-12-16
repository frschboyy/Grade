package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {
    
    @Autowired
    private StudentService studentService;

    @GetMapping("/signup")
    public String showSignupPage(HttpSession session, Model model) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (isAdmin != null && isAdmin) {
            // If session exists, redirect to the dashboard
            return "redirect:/add-Assignment";
        }
        
        if (loggedInStudent != null) {
            // If session exists, redirect to the dashboard
            return "redirect:/dashboard";
        }
        model.addAttribute("student", new Student());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@ModelAttribute("student") Student student, HttpSession session) {
        Student added = studentService.saveStudent(student);
        
        if (added != null) {
            // Store the student in the session
            session.setAttribute("loggedInStudent", student);
            session.setAttribute("id", student.getId());
            return "redirect:/dashboard";
        }
        return "redirect:/signup";
    }
}