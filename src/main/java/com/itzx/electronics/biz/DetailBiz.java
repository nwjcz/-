package com.itzx.electronics.biz;

import com.itzx.electronics.entity.Detail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
public interface DetailBiz {
    //显示商品详情
    public Detail findDetailById(int id);
}
