package com.itzx.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemVO {
    private Integer productId;
    private String productName;
    private Double unitPrice;
    private Integer quantity;
    private Integer checked;
    private String imageUrl;
}
