package com.itzx.order.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    private Long id;
    private String orderNo;
    private Integer userId;
    private Double totalAmount;
    private Integer status;
    private Integer payType;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private String createTime;
    private String payTime;
    private String shipTime;
    private String finishTime;
    private String cancelTime;
    private String updateTime;
}
