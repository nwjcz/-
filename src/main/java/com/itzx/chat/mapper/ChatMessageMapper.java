package com.itzx.chat.mapper;

import com.itzx.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessage message);

    List<ChatMessage> findByOrderNo(@Param("orderNo") String orderNo);
}
