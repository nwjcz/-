package com.itzx.merchant.controller;

import com.itzx.comment.mapper.CommentMapper;
import com.itzx.merchant.dto.MerchantCommentDTO;
import com.itzx.merchant.mapper.MerchantCommentMapper;
import com.itzx.until.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/comment")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class MerchantCommentController {

    @Autowired
    private MerchantCommentMapper merchantCommentMapper;

    @Autowired
    private CommentMapper commentMapper;

    @GetMapping("/listByProduct")
    @ResponseBody
    public Result listByProduct(HttpServletRequest request,
                                @RequestParam("productId") int productId) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        List<MerchantCommentDTO> list = merchantCommentMapper.findByProductId(productId);
        return Result.success(list);
    }

    @PostMapping("/reply")
    @ResponseBody
    public Result replyComment(HttpServletRequest request,
                               @RequestParam("commentId") Long commentId,
                               @RequestParam("replyContent") String replyContent) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        int rows = commentMapper.updateMerchantReply(commentId, replyContent);
        if (rows <= 0) {
            return Result.error("回复失败");
        }
        return Result.success("回复成功");
    }
}
