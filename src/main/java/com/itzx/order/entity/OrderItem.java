package com.itzx.order.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;
    private Integer productId;
    private String productName;
    private String productImage;
    private Double unitPrice;
    private Integer quantity;
    private Double totalPrice;
    private String createTime;
    private String updateTime;
    private Long commentId;
}
