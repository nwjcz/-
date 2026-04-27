package com.itzx.chat.controller;

import com.itzx.chat.entity.UserChatMessage;
import com.itzx.chat.entity.UserChatSession;
import com.itzx.chat.mapper.UserChatMessageMapper;
import com.itzx.chat.mapper.UserChatSessionMapper;
import com.itzx.until.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userChat")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class UserChatController {

    @Autowired
    private UserChatMessageMapper userChatMessageMapper;

    @Autowired
    private UserChatSessionMapper userChatSessionMapper;

    @GetMapping("/sessions")
    @ResponseBody
    public Result sessions(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        if (username == null || role == null) {
            return Result.unAuth("请先登录");
        }
        if (!"ADMIN".equals(role) && !"MERCHANT".equals(role)) {
            return Result.unAuth("无权限");
        }
        List<UserChatSession> list = userChatSessionMapper.findAllSessions();
        return Result.success(list);
    }

    @GetMapping("/history")
    @ResponseBody
    public Result history(HttpServletRequest request,
                          @RequestParam(value = "buyerUsername", required = false) String buyerUsername) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        if (username == null || role == null) {
            return Result.unAuth("请先登录");
        }
        // 普通买家：只能查看自己的会话
        if (!"ADMIN".equals(role) && !"MERCHANT".equals(role)) {
            buyerUsername = username;
        } else {
            if (buyerUsername == null || buyerUsername.isEmpty()) {
                return Result.error("buyerUsername不能为空");
            }
        }
        List<UserChatMessage> list = userChatMessageMapper.findByBuyerUsername(buyerUsername);
        return Result.success(list);
    }

    @PostMapping("/markRead")
    @ResponseBody
    public Result markRead(HttpServletRequest request,
                           @RequestParam("buyerUsername") String buyerUsername) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        if (username == null || role == null) {
            return Result.unAuth("请先登录");
        }
        if (!"ADMIN".equals(role) && !"MERCHANT".equals(role)) {
            return Result.unAuth("无权限");
        }
        userChatSessionMapper.resetUnread(buyerUsername);
        return Result.success("标记已读成功");
    }
}
