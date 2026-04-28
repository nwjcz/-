package com.itzx.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillOrderCreateMessage {
    private String orderNo;
    private int userId;
    private int productId;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
}
