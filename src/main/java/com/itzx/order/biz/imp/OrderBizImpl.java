package com.itzx.order.biz.imp;

import com.itzx.cart.entity.Cart;
import com.itzx.cart.mapper.CartMapper;
import com.itzx.electronics.entity.Product;
import com.itzx.electronics.mapper.ProductMapper;
import com.itzx.order.biz.OrderBiz;
import com.itzx.order.entity.OrderItem;
import com.itzx.order.entity.Orders;
import com.itzx.order.enums.OrderStatus;
import com.itzx.order.mapper.OrderItemMapper;
import com.itzx.order.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderBizImpl implements OrderBiz {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public Orders createOrderFromCart(int userId,
                                      String receiverName,
                                      String receiverPhone,
                                      String receiverAddress,
                                      String remark) {
        // 1. 查询当前用户购物车中已勾选的商品
        List<Cart> cartList = cartMapper.findCheckedByUserId(userId);
        if (cartList == null || cartList.isEmpty()) {
            return null;
        }

        // 2. 先校验商品是否存在、库存是否充足，并计算总金额
        double totalAmount = 0.0;
        for (Cart cart : cartList) {
            Product product = productMapper.findProductById(cart.getProductId());
            if (product == null) {
                return null;
            }
            if (product.getStock() != null && product.getStock() < cart.getQuantity()) {
                return null;
            }
            if (product.getPurchaseLimit() != null && product.getPurchaseLimit() > 0
                    && cart.getQuantity() > product.getPurchaseLimit()) {
                return null;
            }
            if (product.getPrice() != null) {
                totalAmount += product.getPrice() * cart.getQuantity();
            }
        }

        // 3. 生成订单号
        String orderNo = generateOrderNo(userId);

        // 4. 插入订单主表
        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.WAIT_PAY.getCode()); // 0-待支付
        order.setPayType(null);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);

        int inserted = ordersMapper.insertOrder(order);
        if (inserted <= 0 || order.getId() == null) {
            return null;
        }

        // 5. 插入订单明细并扣减库存
        for (Cart cart : cartList) {
            Product product = productMapper.findProductById(cart.getProductId());
            if (product == null) {
                return null;
            }

            synchronized (product) {
                // 扣减库存（简单实现）
                if (product.getStock() != null) {
                    int newStock = product.getStock() - cart.getQuantity();
                    if (newStock < 0) {
                        return null;
                    }
                    product.setStock(newStock);
                    productMapper.updataProduct(product);
                }
            }

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getPname());
            item.setProductImage(product.getImageUrl());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            if (product.getPrice() != null) {
                item.setTotalPrice(product.getPrice() * cart.getQuantity());
            }

            orderItemMapper.insertOrderItem(item);
        }

        // 6. 删除购物车中已勾选的记录
        cartMapper.deleteCheckedByUserId(userId);

        return order;
    }

    @Override
    public List<Orders> findOrdersByUserId(int userId) {
        return ordersMapper.findByUserId(userId);
    }

    @Override
    public List<Orders> findAllOrders() {
        return ordersMapper.findAll();
    }

    @Override
    public Orders findByOrderNo(String orderNo) {
        return ordersMapper.findByOrderNo(orderNo);
    }

    @Override
    public List<OrderItem> findItemsByOrderId(long orderId) {
        return orderItemMapper.findByOrderId(orderId);
    }

    @Override
    public boolean cancelOrder(int userId, String orderNo) {
        return ordersMapper.cancelOrder(userId, orderNo) > 0;
    }

    @Override
    public boolean markShipped(String orderNo) {
        return ordersMapper.markShipped(orderNo) > 0;
    }

    @Override
    public boolean confirmReceive(int userId, String orderNo) {
        return ordersMapper.confirmReceive(userId, orderNo) > 0;
    }

    @Override
    public boolean markCommented(int userId, String orderNo) {
        return ordersMapper.markCommented(userId, orderNo) > 0;
    }

    @Override
    public boolean applyRefund(int userId, String orderNo) {
        return ordersMapper.applyRefund(userId, orderNo) > 0;
    }

    private String generateOrderNo(int userId) {
        return System.currentTimeMillis() + String.valueOf(userId) + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
