package com.itzx.cart.biz.imp;

import com.itzx.cart.biz.CartBiz;
import com.itzx.cart.entity.Cart;
import com.itzx.cart.entity.CartItemVO;
import com.itzx.cart.mapper.CartMapper;
import com.itzx.electronics.entity.Product;
import com.itzx.electronics.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartBizImpl implements CartBiz{

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    //
    @Override
    public List<CartItemVO> findByUserId(int userId) {
        List<Cart> cartList = cartMapper.findByUserId(userId);
        List<CartItemVO> result = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMapper.findProductById(cart.getProductId());
            //如果查询到的商品不存在，则跳过
            if (product == null) {
                continue;
            }
            CartItemVO vo = new CartItemVO();
            vo.setProductId(cart.getProductId());
            vo.setProductName(product.getPname());
            vo.setUnitPrice(product.getPrice());
            vo.setQuantity(cart.getQuantity());
            vo.setChecked(cart.getChecked());
            vo.setImageUrl(product.getImageUrl());
            result.add(vo);
        }
        return result;
    }

    //查询购物车中选中的
    @Override
    public List<Cart> findCheckedByUserId(int userId) {
        return cartMapper.findCheckedByUserId(userId);
    }

    //添加商品到购物车
    @Override
    public boolean addToCart(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Product product = productMapper.findProductById(productId);
        if (product == null) {
            return false;
        }
        Cart existing = cartMapper.findByUserIdAndProductId(userId, productId);

        Integer purchaseLimit = product.getPurchaseLimit();
        if (purchaseLimit != null && purchaseLimit > 0) {
            int newQuantity = quantity;
            if (existing != null) {
                newQuantity += existing.getQuantity();
            }
            if (newQuantity > purchaseLimit) {
                return false;
            }
        }

        if (existing == null) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setChecked(1);
            return cartMapper.insertCart(cart) > 0;
        } else {
            existing.setQuantity(existing.getQuantity() + quantity);
            if (existing.getQuantity() <= 0) {
                return cartMapper.deleteByUserIdAndProductId(userId, productId) > 0;
            }
            existing.setChecked(1);
            return cartMapper.updateCart(existing) >0;
        }
    }


    //更新购物车商品信息
    @Override
    public boolean updateCart(int userId, int productId, int quantity, int checked) {
        Cart existing = cartMapper.findByUserIdAndProductId(userId, productId);
        if (existing == null) {
            return false;
        }
        if (quantity <= 0) {
            return cartMapper.deleteByUserIdAndProductId(userId, productId) > 0;
        }
        Product product = productMapper.findProductById(productId);
        if (product == null) {
            return false;
        }
        Integer purchaseLimit = product.getPurchaseLimit();
        if (purchaseLimit != null && purchaseLimit > 0 && quantity > purchaseLimit) {
            return false;
        }
        existing.setQuantity(quantity);
        existing.setChecked(checked);
        return cartMapper.updateCart(existing) > 0;
    }

    //删除购物车商品
    @Override
    public boolean removeCartItem(int userId, int productId) {
        return cartMapper.deleteByUserIdAndProductId(userId, productId) > 0;
    }
    //清空购物车
    @Override
    public boolean clearCart(int userId) {
        return cartMapper.deleteByUserId(userId) > 0;
    }
}
