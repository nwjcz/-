package com.itzx.order.biz;

import java.util.Map;

public interface PayBiz {

    String createAlipayPagePay(String orderNo);

    boolean handleAlipayNotify(Map<String, String> params);
}
