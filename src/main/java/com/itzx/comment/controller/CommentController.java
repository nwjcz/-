package com.itzx.comment.controller;

import com.itzx.comment.biz.CommentBiz;
import com.itzx.comment.entity.Comment;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class CommentController {

    @Autowired
    private CommentBiz commentBiz;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/add")
    @ResponseBody
    public Result addComment(HttpServletRequest request,
                             @RequestParam("orderNo") String orderNo,
                             @RequestParam("productId") int productId,
                             @RequestParam(value = "rating", required = false) Integer rating,
                             @RequestParam("content") String content) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        return commentBiz.addComment(userId, orderNo, productId, rating, content);
    }

    @GetMapping("/listByProduct")
    @ResponseBody
    public Result listByProduct(@RequestParam("productId") int productId) {
        return commentBiz.listByProduct(productId);
    }

    @PostMapping("/append")
    @ResponseBody
    public Result appendComment(HttpServletRequest request,
                                @RequestParam("commentId") Long commentId,
                                @RequestParam("content") String content) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        return commentBiz.appendComment(userId, commentId, content);
    }

    @PostMapping("/reply")
    @ResponseBody
    public Result replyComment(HttpServletRequest request,
                               @RequestParam("commentId") Long commentId,
                               @RequestParam("content") String content) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        return commentBiz.replyComment(commentId, content);
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
