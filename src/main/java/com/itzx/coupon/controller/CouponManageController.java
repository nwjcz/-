package com.itzx.coupon.controller;

import com.itzx.coupon.biz.CouponBiz;
import com.itzx.coupon.entity.Coupon;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/coupon")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class CouponManageController {

    @Autowired
    private CouponBiz couponBiz;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/create")
    @ResponseBody
    public Result create(HttpServletRequest request, @RequestBody Coupon coupon) {
        Integer merchantId = getMerchantIdFromRequest(request);
        if (merchantId == null) {
            return Result.unAuth("商家未登录或无权限");
        }
        return couponBiz.createCoupon(coupon, merchantId);
    }

    @PostMapping("/update")
    @ResponseBody
    public Result update(HttpServletRequest request, @RequestBody Coupon coupon) {
        Integer merchantId = getMerchantIdFromRequest(request);
        if (merchantId == null) {
            return Result.unAuth("商家未登录或无权限");
        }
        return couponBiz.updateCoupon(coupon, merchantId);
    }

    @GetMapping("/list")
    @ResponseBody
    public Result list(HttpServletRequest request) {
        Integer merchantId = getMerchantIdFromRequest(request);
        if (merchantId == null) {
            return Result.unAuth("商家未登录或无权限");
        }
        return couponBiz.listMerchantCoupons(merchantId);
    }

    private Integer getMerchantIdFromRequest(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (role == null || !"MERCHANT".equals(role)) {
            return null;
        }
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return null;
        }
        User user = userMapper.login(username);
        if (user == null) {
            return null;
        }
        return user.getId();
    }
}
