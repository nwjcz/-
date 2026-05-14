package com.itzx.coupon.controller;

import com.itzx.coupon.biz.CouponBiz;
import com.itzx.until.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupon")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class CouponAuditController {

    @Autowired
    private CouponBiz couponBiz;

    @GetMapping("/listAll")
    @ResponseBody
    public Result listAll(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.unAuth("无权限操作");
        }
        return couponBiz.listAllCoupons();
    }

    @GetMapping("/listPending")
    @ResponseBody
    public Result listPending(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.unAuth("无权限操作");
        }
        return couponBiz.listPendingCoupons();
    }

    @PostMapping("/approve")
    @ResponseBody
    public Result approve(HttpServletRequest request, @RequestParam Long couponId) {
        if (!isAdmin(request)) {
            return Result.unAuth("无权限操作");
        }
        return couponBiz.approveCoupon(couponId);
    }

    @PostMapping("/reject")
    @ResponseBody
    public Result reject(HttpServletRequest request, @RequestParam Long couponId) {
        if (!isAdmin(request)) {
            return Result.unAuth("无权限操作");
        }
        return couponBiz.rejectCoupon(couponId);
    }

    private boolean isAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return "ADMIN".equals(role);
    }
}
