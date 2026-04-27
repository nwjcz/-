package com.itzx.order.biz.imp;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.itzx.config.AlipayConfig;
import com.itzx.order.biz.PayBiz;
import com.itzx.order.entity.Orders;
import com.itzx.order.enums.OrderStatus;
import com.itzx.order.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PayBizImpl implements PayBiz {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrdersMapper ordersMapper;

    @Value("${alipay.debug-skip-sign:false}")
    private boolean debugSkipSign;

    @Override
    public String createAlipayPagePay(String orderNo) {
        Orders order = ordersMapper.findByOrderNo(orderNo);
        if (order == null || order.getStatus() == null || order.getStatus() != OrderStatus.WAIT_PAY.getCode()) {
            return null;
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayConfig.getReturnUrl());
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        String subject = "订单" + order.getOrderNo();
        String totalAmount = String.format("%.2f", order.getTotalAmount());

        String bizContent = "{" +
                "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "\"total_amount\":\"" + totalAmount + "\"," +
                "\"subject\":\"" + subject + "\"" +
                "}";
        request.setBizContent(bizContent);

        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            return null;
        }
    }

    @Override
    public boolean handleAlipayNotify(Map<String, String> params) {
        System.out.println("PayBizImpl.handleAlipayNotify params = " + params);
        if (debugSkipSign) {
            System.out.println("PayBizImpl.handleAlipayNotify debugSkipSign = true, skip sign verify");
        } else {
            try {
                boolean signVerified = AlipaySignature.rsaCheckV1(
                        params,
                        alipayConfig.getAlipayPublicKey(),
                        "UTF-8",
                        "RSA2");
                if (!signVerified) {
                    System.out.println("PayBizImpl.handleAlipayNotify signVerified = false");
                    return false;
                }
                System.out.println("PayBizImpl.handleAlipayNotify signVerified = true");
            } catch (AlipayApiException e) {
                System.out.println("PayBizImpl.handleAlipayNotify AlipayApiException: " + e.getMessage());
                return false;
            }
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmountStr = params.get("total_amount");

        System.out.println("PayBizImpl.handleAlipayNotify outTradeNo=" + outTradeNo
                + ", tradeStatus=" + tradeStatus + ", totalAmount=" + totalAmountStr);

        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            System.out.println("PayBizImpl.handleAlipayNotify tradeStatus not success, tradeStatus=" + tradeStatus);
            return false;
        }

        Orders order = ordersMapper.findByOrderNo(outTradeNo);
        if (order == null) {
            System.out.println("PayBizImpl.handleAlipayNotify order not found, outTradeNo=" + outTradeNo);
            return false;
        }

        String orderAmount = String.format("%.2f", order.getTotalAmount());
        if (!orderAmount.equals(totalAmountStr)) {
            System.out.println("PayBizImpl.handleAlipayNotify amount not match, db=" + orderAmount + ", notify=" + totalAmountStr);
            return false;
        }

        if (order.getStatus() != null && order.getStatus() == OrderStatus.WAIT_SHIP.getCode()) {
            System.out.println("PayBizImpl.handleAlipayNotify already WAIT_SHIP, outTradeNo=" + outTradeNo);
            return true;
        }

        System.out.println("PayBizImpl.handleAlipayNotify update to WAIT_SHIP, current status=" + order.getStatus());
        int rows = ordersMapper.markPaid(outTradeNo);
        System.out.println("PayBizImpl.handleAlipayNotify markPaid rows = " + rows);
        return rows > 0;
    }
}
