package com.itzx.coupon.mapper;

import com.itzx.coupon.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCouponMapper {
    int insert(UserCoupon userCoupon);

    List<UserCoupon> findByUserId(@Param("userId") Integer userId);

    UserCoupon findById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
