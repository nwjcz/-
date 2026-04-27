package com.itzx.comment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Integer productId;
    private Integer userId;
    private Integer rating; // 评分，1-5，允许为null
    private String content; // 评论内容
    private String merchantReply; // 商家回复
    private String createTime;
    private String updateTime;
    private String replyTime;
    private String appendContent; // 追评内容
    private String appendTime;    // 追评时间
}
