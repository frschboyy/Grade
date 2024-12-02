package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.service.CohereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final CohereService cohereService;

    @Autowired
    public ChatController(CohereService cohereService) {
        this.cohereService = cohereService;
    }

    @PostMapping("/generate")
    public String generateChatResponse(@RequestBody String prompt) {
        return cohereService.generateResponse(prompt);
    }
}