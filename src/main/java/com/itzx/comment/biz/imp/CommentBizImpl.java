package com.itzx.comment.biz.imp;

import com.itzx.comment.biz.CommentBiz;
import com.itzx.comment.entity.Comment;
import com.itzx.comment.mapper.CommentMapper;
import com.itzx.order.entity.Orders;
import com.itzx.order.enums.OrderStatus;
import com.itzx.order.mapper.OrdersMapper;
import com.itzx.until.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentBizImpl implements CommentBiz {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    private static final String[] BAD_WORDS = {
        "sb", "傻逼", "煞笔", "妈的", "你妈", "你妈死了", "他妈", "他妈的",
        "操你妈", "草你妈", "艹你妈", "狗日的", "王八蛋", "畜生", "狗娘养",
        "cnm", "nmsl"
    };

    private boolean containsBadWords(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        String lower = content.toLowerCase();
        for (String word : BAD_WORDS) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Result addComment(int userId, String orderNo, int productId, Integer rating, String content) {
        Orders order = ordersMapper.findByOrderNo(orderNo);
        if (order == null || order.getUserId() == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在或无权限评论");
        }
        if (order.getStatus() == null ||
                !order.getStatus().equals(OrderStatus.WAIT_COMMENT.getCode())) {
            return Result.error("订单未完成收货或已评价，暂不能评论");
        }
        if (containsBadWords(content)) {
            return Result.error("请您文明用语");
        }
        List<Comment> existing = commentMapper.findByOrderAndProduct(order.getId(), productId, userId);
        if (existing != null && !existing.isEmpty()) {
            return Result.error("该商品已评论");
        }
        Comment comment = new Comment();
        comment.setOrderId(order.getId());
        comment.setOrderNo(orderNo);
        comment.setProductId(productId);
        comment.setUserId(userId);
        if (rating != null && rating >= 1 && rating <= 5) {
            comment.setRating(rating);
        }
        comment.setContent(content);
        int rows = commentMapper.insertComment(comment);
        if (rows <= 0) {
            return Result.error("评论失败");
        }
        return Result.success("评论成功");
    }

    @Override
    public Result listByProduct(int productId) {
        List<Comment> comments = commentMapper.findByProductId(productId);
        return Result.success(comments);
    }

    @Override
    public Result appendComment(int userId, Long commentId, String content) {
        Comment comment = commentMapper.findById(commentId);
        //添加评论屏蔽词
        if (containsBadWords(content)) {
            return Result.error("请您文明用语");
        }
        if (comment == null || comment.getUserId() == null || !comment.getUserId().equals(userId)) {
            return Result.error("评论不存在或无权限追评");
        }
        if (comment.getAppendContent() != null && !comment.getAppendContent().isEmpty()) {
            return Result.error("该评论已追评");
        }
        int rows = commentMapper.updateAppendContent(commentId, content);
        if (rows <= 0) {
            return Result.error("追评失败");
        }
        return Result.success("追评成功");
    }

    @Override
    public Result replyComment(Long commentId, String content) {
        Comment comment = commentMapper.findById(commentId);
        if (containsBadWords(content)) {
            return Result.error("请您文明用语");
        }
        if (comment == null) {
            return Result.error("评论不存在");
        }
        if (comment.getMerchantReply() != null && !comment.getMerchantReply().isEmpty()) {
            return Result.error("该评论已回复");
        }
        int rows = commentMapper.updateMerchantReply(commentId, content);
        if (rows <= 0) {
            return Result.error("回复失败");
        }
        return Result.success("回复成功");
    }
}
