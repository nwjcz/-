package com.itzx.seckill.controller;

import com.itzx.seckill.biz.SeckillBiz;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class SeckillController {

    @Autowired
    private SeckillBiz seckillBiz;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/preload")
    @ResponseBody
    public Result preload(HttpServletRequest request,
                          @RequestParam("productId") int productId,
                          @RequestParam("stock") int stock) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        return seckillBiz.preloadStock(productId, stock);
    }

    @PostMapping("/buy")
    @ResponseBody
    public Result buy(HttpServletRequest request,
                      @RequestParam("productId") int productId,
                      @RequestParam("receiverName") String receiverName,
                      @RequestParam("receiverPhone") String receiverPhone,
                      @RequestParam("receiverAddress") String receiverAddress,
                      @RequestParam(value = "remark", required = false) String remark) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        return seckillBiz.seckillBuy(userId, productId, receiverName, receiverPhone, receiverAddress, remark);
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
