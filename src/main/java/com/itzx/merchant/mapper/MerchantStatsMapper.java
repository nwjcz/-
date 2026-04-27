package com.itzx.merchant.mapper;

import com.itzx.merchant.dto.ProductStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantStatsMapper {

    List<ProductStatsDTO> findProductStats(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("keyword") String keyword,
                                           @Param("topN") Integer topN);
}
