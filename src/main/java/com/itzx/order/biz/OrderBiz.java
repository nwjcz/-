package com.itzx.order.biz;

import com.itzx.order.entity.OrderItem;
import com.itzx.order.entity.Orders;

import java.util.List;

public interface OrderBiz {

    Orders createOrderFromCart(int userId,
                               String receiverName,
                               String receiverPhone,
                               String receiverAddress,
                               String remark);

    List<Orders> findOrdersByUserId(int userId);

    List<Orders> findAllOrders();

    Orders findByOrderNo(String orderNo);

    List<OrderItem> findItemsByOrderId(long orderId);

    boolean cancelOrder(int userId, String orderNo);

    boolean markShipped(String orderNo);

    boolean confirmReceive(int userId, String orderNo);

    boolean markCommented(int userId, String orderNo);

    boolean applyRefund(int userId, String orderNo);
}
