package com.itzx.merchant.mapper;

import com.itzx.merchant.dto.MerchantOrderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantOrderMapper {

    List<MerchantOrderDTO> findByCondition(@Param("username") String username,
                                           @Param("status") Integer status,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);
}
