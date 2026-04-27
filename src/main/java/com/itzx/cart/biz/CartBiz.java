package com.itzx.cart.biz;

import com.itzx.cart.entity.Cart;
import com.itzx.cart.entity.CartItemVO;

import java.util.List;

public interface CartBiz {

    List<CartItemVO> findByUserId(int userId);

    List<Cart> findCheckedByUserId(int userId);

    boolean addToCart(int userId, int productId, int quantity);

    boolean updateCart(int userId, int productId, int quantity, int checked);

    boolean removeCartItem(int userId, int productId);

    boolean clearCart(int userId);
}
