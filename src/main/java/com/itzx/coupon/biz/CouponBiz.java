package com.itzx.coupon.biz;

import com.itzx.coupon.entity.Coupon;
import com.itzx.until.Result;

public interface CouponBiz {
    Result createCoupon(Coupon coupon, Integer merchantId);

    Result updateCoupon(Coupon coupon, Integer merchantId);

    Result listMerchantCoupons(Integer merchantId);

    Result listAllCoupons();

    Result listPendingCoupons();

    Result approveCoupon(Long couponId);

    Result rejectCoupon(Long couponId);

    Result getUserCoupons(Integer userId);
}
