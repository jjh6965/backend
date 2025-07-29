package com.boot.cms.mapper.chatbot;

import org.apache.ibatis.annotations.Select;

public interface ChatbotMapper {
    @Select("Select response FROM chatbot_responses WHERE keyword = #{keyword}")
    String findResponseByKeyword(String keyword);
}
