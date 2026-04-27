package com.itzx.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChatMessage {
    private Long id;
    private String buyerUsername; // 会话所属买家用户名
    private String fromRole;      // USER / MERCHANT / ADMIN
    private String fromUsername;  // 实际发送方用户名
    private String content;       // 消息内容
    private String createTime;    // 发送时间
}
