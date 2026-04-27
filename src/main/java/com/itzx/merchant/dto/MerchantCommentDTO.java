package com.itzx.merchant.dto;

public class MerchantCommentDTO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Integer productId;
    private String productName;
    private Integer userId;
    private String username;
    private Integer rating;
    private String content;
    private String merchantReply;
    private String createTime;
    private String replyTime;
    private String appendContent;
    private String appendTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMerchantReply() { return merchantReply; }
    public void setMerchantReply(String merchantReply) { this.merchantReply = merchantReply; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getReplyTime() { return replyTime; }
    public void setReplyTime(String replyTime) { this.replyTime = replyTime; }
    public String getAppendContent() { return appendContent; }
    public void setAppendContent(String appendContent) { this.appendContent = appendContent; }
    public String getAppendTime() { return appendTime; }
    public void setAppendTime(String appendTime) { this.appendTime = appendTime; }
}
