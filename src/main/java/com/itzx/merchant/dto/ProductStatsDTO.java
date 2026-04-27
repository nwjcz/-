package com.itzx.merchant.dto;

public class ProductStatsDTO {
    private Integer productId;
    private String productName;
    private Long salesVolume;
    private Long refundVolume;
    private Double salesAmount;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(Long salesVolume) {
        this.salesVolume = salesVolume;
    }

    public Long getRefundVolume() {
        return refundVolume;
    }

    public void setRefundVolume(Long refundVolume) {
        this.refundVolume = refundVolume;
    }

    public Double getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(Double salesAmount) {
        this.salesAmount = salesAmount;
    }
}
