package com.itzx.cart.mapper;

import com.itzx.cart.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {

    Cart findByUserIdAndProductId(@Param("userId") int userId, @Param("productId") int productId);

    List<Cart> findByUserId(@Param("userId") int userId);

    List<Cart> findCheckedByUserId(@Param("userId") int userId);

    int insertCart(Cart cart);

    int updateCart(Cart cart);

    int deleteByUserIdAndProductId(@Param("userId") int userId, @Param("productId") int productId);

    int deleteByUserId(@Param("userId") int userId);

    int deleteCheckedByUserId(@Param("userId") int userId);
}
