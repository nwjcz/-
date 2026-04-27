package com.itzx.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChatSession {
    private Long id;
    private String buyerUsername;      // 会话所属买家用户名
    private String buyerDisplayName;   // 展示给客服看的名称
    private String lastMsgContent;     // 最后一条消息内容
    private String lastMsgTime;        // 最后一条消息时间
    private String lastMsgFrom;        // BUYER / MERCHANT / ADMIN
    private Integer unreadCount;       // 买家->商家未读数
    private Boolean hasUnreplied;      // 是否存在“未回复”的买家消息
    private String createdAt;          // 创建时间
    private String updatedAt;          // 更新时间
}
