package com.itzx.chat.controller;

import com.itzx.chat.entity.ChatMessage;
import com.itzx.chat.mapper.ChatMessageMapper;
import com.itzx.order.entity.Orders;
import com.itzx.order.mapper.OrdersMapper;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class ChatController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/history")
    @ResponseBody
    public Result history(HttpServletRequest request,
                          @RequestParam("orderNo") String orderNo) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        if (username == null || role == null) {
            return Result.unAuth("请先登录");
        }
        Orders order = ordersMapper.findByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        // 买家：只能看自己的订单；商家/管理员：可以看所有订单
        if (!"ADMIN".equals(role) && !"MERCHANT".equals(role)) {
            User user = userMapper.login(username);
            if (user == null || order.getUserId() == null || !order.getUserId().equals(user.getId())) {
                return Result.unAuth("无权限查看该订单聊天记录");
            }
        }
        List<ChatMessage> list = chatMessageMapper.findByOrderNo(orderNo);
        return Result.success(list);
    }
}
