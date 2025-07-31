package com.boot.cms.controller.chatbot;

import com.boot.cms.service.chatbot.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    public String handleMessage(@RequestBody String message) {
        return chatbotService.getResponse(message);
    }
}
