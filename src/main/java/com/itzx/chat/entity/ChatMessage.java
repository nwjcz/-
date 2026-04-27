package com.itzx.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long id;
    private String orderNo;
    private String fromRole;
    private String fromUsername;
    private String content;
    private String createTime;
}
