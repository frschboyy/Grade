package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private StudentService studentService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @GetMapping("/")
    public String showLoginPage(HttpSession session, HttpServletResponse response) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        // If admin credentials are recognized, direct to admin dashboard
        if (isAdmin != null && isAdmin) {
            return "redirect:/add-Assignment";
        }

        // If session exists, redirect to the dashboard
        if (loggedInStudent != null) {
            return "redirect:/dashboard";
        }

        //  Else show login page
        return "login";
    }

    @PostMapping("/")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        // Check for admin credentials
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            session.setAttribute("isAdmin", true); // Add admin session attribute
            return "redirect:/add-Assignment"; // Redirect to Add Assignment page
        }

        Student student = studentService.getStudent(username);

        if (student != null && student.getPassword().equals(password)) {
            
            // Store the student in the session
            session.setAttribute("loggedInStudent", student);
            session.setAttribute("id", student.getId());
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Invalid username or password");
        return "login";
    }
}
