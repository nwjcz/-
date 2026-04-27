package com.itzx.comment.mapper;

import com.itzx.comment.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    int insertComment(Comment comment);

    Comment findById(@Param("id") Long id);

    List<Comment> findByProductId(@Param("productId") int productId);

    List<Comment> findByOrderAndProduct(@Param("orderId") long orderId,
                                        @Param("productId") int productId,
                                        @Param("userId") int userId);

    int updateMerchantReply(@Param("id") Long id,
                            @Param("merchantReply") String merchantReply);

    int updateAppendContent(@Param("id") Long id,
                            @Param("appendContent") String appendContent);
}
