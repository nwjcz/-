package com.itzx.electronics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Detail {
    private Integer id;
    private String text;
    private String origin;
    private String imageUrl;
    private String spec;
    private String createTime;
    private String updateTime;
}
