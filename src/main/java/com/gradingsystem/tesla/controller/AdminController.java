package com.gradingsystem.tesla.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    @GetMapping("/add-Assignment")
    public String showAddAssignmentPage(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin != null && isAdmin) {
            return "addAssignment"; // Return the add-assignment view
        }

        return "redirect:/"; // Redirect to login if not authenticated as admin
    }
}
