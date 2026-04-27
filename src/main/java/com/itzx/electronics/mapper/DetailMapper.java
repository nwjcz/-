package com.itzx.electronics.mapper;

import com.itzx.electronics.entity.Detail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetailMapper {
    //显示商品详情
    Detail findDetailById(int id);

}
