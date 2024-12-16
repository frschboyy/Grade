package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.Student;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");

        // Redirect to login if not authenticated
        if (loggedInStudent == null) {
            return "redirect:/";
        }

        // Pass the username to the dashboard
        model.addAttribute("username", loggedInStudent.getUsername());
        return "dashboard";
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/";
    }

    @GetMapping("/submit-page")
    public String getSubmitAssignmentPage(Model model, HttpSession session) {

        // Redirect to login if not authenticated
        if ((Student) session.getAttribute("loggedInStudent") == null) {
            return "redirect:/";
        }

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

        // Redirect to login if not authenticated
        if ((Student) session.getAttribute("loggedInStudent") == null) {
            return "redirect:/";
        }

        // Add assignment details to the model
        model.addAttribute("grade", (Integer) session.getAttribute("grade"));
        model.addAttribute("plagiarism", (Integer) session.getAttribute("plagiarism"));
        model.addAttribute("results", (Map<String, String>) session.getAttribute("results"));

        // Return the view
        return "resultsPage";
    }
}
