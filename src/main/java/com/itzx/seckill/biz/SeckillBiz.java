package com.itzx.seckill.biz;

import com.itzx.until.Result;

public interface SeckillBiz {
    Result preloadStock(int productId, int stock);

    Result seckillBuy(int userId,
                     int productId,
                     String receiverName,
                     String receiverPhone,
                     String receiverAddress,
                     String remark);
}
