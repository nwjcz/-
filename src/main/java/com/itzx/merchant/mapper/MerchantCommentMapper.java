package com.itzx.merchant.mapper;

import com.itzx.merchant.dto.MerchantCommentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantCommentMapper {

    List<MerchantCommentDTO> findByProductId(@Param("productId") int productId);
}
