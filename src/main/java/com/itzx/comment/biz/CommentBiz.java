package com.itzx.comment.biz;

import com.itzx.until.Result;

public interface CommentBiz {

    Result addComment(int userId,
                      String orderNo,
                      int productId,
                      Integer rating,
                      String content);

    Result listByProduct(int productId);

    Result appendComment(int userId,
                         Long commentId,
                         String content);

    Result replyComment(Long commentId,
                        String content);
}
