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
    public String handleSignup(@ModelAttribute("student") Student student) {
        studentService.saveStudent(student);
        return "redirect:/";
    }

    @GetMapping("/")
    public String showLoginPage(HttpSession session, HttpServletResponse response) {
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
        
        //  Else show login page
        return "login";
    }

    @PostMapping("/")
    public String handleLogin(@RequestParam String username, @RequestParam String password, 
                              HttpSession session, Model model) {
        
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

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        if (loggedInStudent == null)
            return "redirect:/"; // Redirect to login if not authenticated

        // Pass the username to the dashboard
        model.addAttribute("username", loggedInStudent.getUsername());
        return "dashboard";
    }
    
    @GetMapping("/add-Assignment")
    public String showAddAssignmentPage(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin != null && isAdmin)
            return "addAssignment"; // Return the add-assignment view

        return "redirect:/"; // Redirect to login if not authenticated as admin
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/";
    }
    
    @GetMapping("/submit-page")
    public String getSubmitAssignmentPage(Model model, HttpSession session) {
        if ((Student) session.getAttribute("loggedInStudent") == null)
            return "redirect:/"; // Redirect to login if not authenticated
        
        // Add assignment details to the model
        model.addAttribute("id", (Long) session.getAttribute("assignmentId"));
        model.addAttribute("title", (String) session.getAttribute("title"));
        model.addAttribute("description", (String) session.getAttribute("description"));
        model.addAttribute("dueDate", session.getAttribute("dueDate"));

        // Return the view
        return "submitAssignmentPage";
    }
    
    @GetMapping("/evaluation-page")
    public String getEvaluationPage(Model model, HttpSession session) {
        if ((Student) session.getAttribute("loggedInStudent") == null)
            return "redirect:/"; // Redirect to login if not authenticated
        
        // Add assignment details to the model
        model.addAttribute("grade", (Integer) session.getAttribute("grade"));
        model.addAttribute("plagiarism", (Integer) session.getAttribute("plagiarism"));

        // Return the view
        return "resultsPage";
    }
}