package com.boot.cms.controller.chatbot;

import com.boot.cms.service.chatbot.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = {"http://localhost:5173", "https://your-frontend.cloudtype.app", "https://jh9695-backend.cloudtype.app"}, allowCredentials = "true")
public class ChatbotController {
    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    public String handleMessage(@RequestBody String message) {
        return chatbotService.getResponse(message);
    }
}
