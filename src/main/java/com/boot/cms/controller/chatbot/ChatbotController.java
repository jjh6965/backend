package com.boot.cms.controller.chatbot;

import com.boot.cms.service.chatbot.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = {"http://localhost:5173", "https://jjh6965.github.io/cms", "https://port-0-java-springboot-mbebujvsfb073e29.sel4.cloudtype.app"}, allowCredentials = "true")
public class ChatbotController {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    public String handleMessage(@RequestBody String message) {
        logger.info("Received message: {}", message);
        if (chatbotService == null) {
            logger.error("ChatbotService is not initialized");
            return "챗봇 서비스가 초기화되지 않았습니다.";
        }
        String response = chatbotService.getResponse(message);
        logger.info("Sending response: {}", response);
        return response;
    }
}