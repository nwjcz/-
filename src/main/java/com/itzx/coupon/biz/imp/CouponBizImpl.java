package com.itzx.coupon.biz.imp;

import com.itzx.coupon.biz.CouponBiz;
import com.itzx.coupon.entity.Coupon;
import com.itzx.coupon.entity.UserCoupon;
import com.itzx.coupon.mapper.CouponMapper;
import com.itzx.coupon.mapper.UserCouponMapper;
import com.itzx.until.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("couponBiz")
public class CouponBizImpl implements CouponBiz {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    public Result createCoupon(Coupon coupon, Integer merchantId) {
        if (coupon.getSignInDay() == null || !Arrays.asList(1, 3, 7).contains(coupon.getSignInDay())) {
            return Result.error("签到天数只能为第1天、第3天或第7天");
        }
        if (coupon.getAmount() == null || coupon.getAmount().doubleValue() <= 0) {
            return Result.error("优惠券金额必须大于0");
        }
        coupon.setMerchantId(merchantId);
        coupon.setStatus(0); // 待审核
        couponMapper.insert(coupon);
        return new Result(200, "优惠券创建成功，等待管理员审核", coupon);
    }

    @Override
    public Result updateCoupon(Coupon coupon, Integer merchantId) {
        Coupon existing = couponMapper.findById(coupon.getId());
        if (existing == null) {
            return Result.error("优惠券不存在");
        }
        if (!existing.getMerchantId().equals(merchantId)) {
            return Result.error("无权修改此优惠券");
        }
        if (existing.getStatus() == 1) {
            return Result.error("已审核通过的优惠券不可修改");
        }
        couponMapper.update(coupon);
        return new Result(200, "优惠券更新成功", null);
    }

    @Override
    public Result listMerchantCoupons(Integer merchantId) {
        List<Coupon> coupons = couponMapper.findByMerchantId(merchantId);
        return Result.success(coupons);
    }

    @Override
    public Result listAllCoupons() {
        List<Coupon> coupons = couponMapper.findAll();
        return Result.success(coupons);
    }

    @Override
    public Result listPendingCoupons() {
        List<Coupon> coupons = couponMapper.findByStatus(0);
        return Result.success(coupons);
    }

    @Override
    public Result approveCoupon(Long couponId) {
        Coupon coupon = couponMapper.findById(couponId);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        if (coupon.getStatus() != 0) {
            return Result.error("该优惠券已审核，不可重复操作");
        }
        // 将该签到日的其他已通过优惠券设为失效（同一天只保留最新通过的那一个）
        List<Coupon> approvedList = couponMapper.findApprovedBySignInDay(coupon.getSignInDay());
        if (approvedList != null) {
            for (Coupon c : approvedList) {
                couponMapper.updateStatus(c.getId(), 3); // 3: 已失效（被替换）
            }
        }
        couponMapper.updateStatus(couponId, 1);
        return Result.success("审核通过");
    }

    @Override
    public Result rejectCoupon(Long couponId) {
        Coupon coupon = couponMapper.findById(couponId);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        if (coupon.getStatus() != 0) {
            return Result.error("该优惠券已审核，不可重复操作");
        }
        couponMapper.updateStatus(couponId, 2);
        return Result.success("已拒绝");
    }

    @Override
    public Result getUserCoupons(Integer userId) {
        List<UserCoupon> userCoupons = userCouponMapper.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : userCoupons) {
            Coupon coupon = couponMapper.findById(uc.getCouponId());
            Map<String, Object> item = new HashMap<>();
            item.put("userCouponId", uc.getId());
            item.put("status", uc.getStatus());
            item.put("receiveTime", uc.getReceiveTime());
            item.put("expireTime", uc.getExpireTime());
            item.put("coupon", coupon);
            result.add(item);
        }
        return Result.success(result);
    }
}
