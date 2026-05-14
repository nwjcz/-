package com.itzx.coupon.controller;

import com.itzx.coupon.biz.CouponBiz;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupon")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class UserCouponController {

    @Autowired
    private CouponBiz couponBiz;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/myCoupons")
    @ResponseBody
    public Result myCoupons(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        return couponBiz.getUserCoupons(userId);
    }

    private Integer getUserIdFromRequest(HttpServletRequest request) {
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
