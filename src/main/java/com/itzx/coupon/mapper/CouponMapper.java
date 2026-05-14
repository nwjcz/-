package com.itzx.coupon.mapper;

import com.itzx.coupon.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponMapper {
    int insert(Coupon coupon);

    int update(Coupon coupon);

    Coupon findById(@Param("id") Long id);

    List<Coupon> findByMerchantId(@Param("merchantId") Integer merchantId);

    List<Coupon> findApprovedBySignInDay(@Param("signInDay") Integer signInDay);

    List<Coupon> findAll();

    List<Coupon> findByStatus(@Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
