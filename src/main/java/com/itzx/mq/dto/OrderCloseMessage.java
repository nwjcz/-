package com.itzx.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCloseMessage {
    private String orderNo;
    private int productId;
    private int userId;
}
