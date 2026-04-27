package com.itzx.electronics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Integer id;
    private String pname;
    private Double price;
    private String imageUrl;
    private Integer detailId;
    private Integer stock;
    private Integer purchaseLimit;
    private Integer status;
    private String createTime;
    private String updateTime;

}
