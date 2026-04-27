package com.itzx.order.mapper;

import com.itzx.order.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrdersMapper {

    int insertOrder(Orders order);

    Orders findByOrderNo(@Param("orderNo") String orderNo);

    List<Orders> findByUserId(@Param("userId") int userId);

    List<Orders> findAll();

    int cancelOrder(@Param("userId") int userId, @Param("orderNo") String orderNo);

    int markPaid(@Param("orderNo") String orderNo);

    int markShipped(@Param("orderNo") String orderNo);

    int confirmReceive(@Param("userId") int userId, @Param("orderNo") String orderNo);

    int markCommented(@Param("userId") int userId, @Param("orderNo") String orderNo);

    int applyRefund(@Param("userId") int userId, @Param("orderNo") String orderNo);

}
