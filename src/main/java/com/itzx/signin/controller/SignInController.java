package com.itzx.signin.controller;

import com.itzx.signin.biz.SignInBiz;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signin")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class SignInController {

    @Autowired
    private SignInBiz signInBiz;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/doSignIn")
    @ResponseBody
    public Result doSignIn(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        return signInBiz.signIn(userId);
    }

    @GetMapping("/status")
    @ResponseBody
    public Result getStatus(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        return signInBiz.getSignInStatus(userId);
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
