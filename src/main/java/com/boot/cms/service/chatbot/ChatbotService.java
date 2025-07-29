package com.boot.cms.service.chatbot;

import com.boot.cms.mapper.chatbot.ChatbotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {
    @Autowired
    private ChatbotMapper chatbotMapper;

    public String getResponse(String message) {
        String normalizedMessage = message.replaceAll("\\s+", "").toLowerCase();
        String[] keywords = {"안녕", "이용", "상담", "배달", "예약", "가격", "위치"}; // DB와 동기화된 키워드 목록
        for (String keyword : keywords) {
            if (normalizedMessage.contains(keyword)) {
                String response = chatbotMapper.findResponseByKeyword(keyword);
                if (response != null) {
                    return response;
                }
            }
        }
        return "죄송합니다. 질문의 의도를 파악하지 못했습니다.";
    }

}
