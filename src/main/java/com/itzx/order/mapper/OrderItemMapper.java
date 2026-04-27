package com.itzx.order.mapper;

import com.itzx.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    int insertOrderItem(OrderItem item);

    List<OrderItem> findByOrderId(@Param("orderId") long orderId);

}
