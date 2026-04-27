package com.itzx.cart.controller;

import com.itzx.cart.biz.CartBiz;
import com.itzx.cart.entity.Cart;
import com.itzx.cart.entity.CartItemVO;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class CartController {

    @Autowired
    private CartBiz cartBiz;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/addToCart")
    @ResponseBody
    public Result addToCart(HttpServletRequest request,
                            @RequestParam("productId") int productId,
                            @RequestParam("quantity") int quantity) {
        //获取前端传回的用户ID
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = cartBiz.addToCart(userId, productId, quantity);
        if (!success) {
            return Result.error("加入购物车失败");
        }
        return Result.success("加入购物车成功");
    }


    @GetMapping("/listCart")
    @ResponseBody
    public Result listCart(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        List<CartItemVO> cartList = cartBiz.findByUserId(userId);
        return Result.success(cartList);
    }

    @PostMapping("/updateCart")
    @ResponseBody
    public Result updateCart(HttpServletRequest request,
                             @RequestParam("productId") int productId,
                             @RequestParam("quantity") int quantity,
                             @RequestParam("checked") int checked) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = cartBiz.updateCart(userId, productId, quantity, checked);
        if (!success) {
            return Result.error("更新购物车失败");
        }
        return Result.success("更新购物车成功");
    }

    @PostMapping("/removeItem")
    @ResponseBody
    public Result removeItem(HttpServletRequest request,
                             @RequestParam("productId") int productId) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = cartBiz.removeCartItem(userId, productId);
        if (!success) {
            return Result.error("删除购物车商品失败");
        }
        return Result.success("删除购物车商品成功");
    }

    @PostMapping("/clearCart")
    @ResponseBody
    public Result clearCart(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = cartBiz.clearCart(userId);
        if (!success) {
            return Result.error("清空购物车失败");
        }
        return Result.success("清空购物车成功");
    }

    private Integer getUserIdFromRequest(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return null;
        }
        User user = userMapper.login(username);
        if (user == null) {
            return null;
        }
        return user.getId();
    }
}
