package com.boot.cms.service.chatbot;

import com.boot.cms.mapper.chatbot.ChatbotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Autowired
    private ChatbotMapper chatbotMapper;

    public String getResponse(String message) {
        if (message == null) {
            logger.warn("Received null message");
            return "메시지를 입력해 주세요.";
        }
        String normalizedMessage = message.replaceAll("\\s+", "").toLowerCase();
        String[] keywords = {"안녕", "이용", "상담", "배달", "예약", "가격", "위치"};
        for (String keyword : keywords) {
            if (normalizedMessage.contains(keyword)) {
                logger.info("Matched keyword: {}", keyword);
                String response = chatbotMapper.findResponseByKeyword(keyword);
                if (response != null) {
                    return response;
                } else {
                    logger.warn("No response found for keyword: {}", keyword);
                }
            }
        }
        logger.info("No matching keyword found for message: {}", message);
        return "죄송합니다. 질문의 의도를 파악하지 못했습니다.";
    }
}