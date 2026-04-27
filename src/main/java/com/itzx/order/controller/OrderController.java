package com.itzx.order.controller;

import com.itzx.comment.entity.Comment;
import com.itzx.comment.mapper.CommentMapper;
import com.itzx.order.biz.OrderBiz;
import com.itzx.order.biz.PayBiz;
import com.itzx.order.entity.OrderItem;
import com.itzx.order.entity.Orders;
import com.itzx.until.Result;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private PayBiz payBiz;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    @PostMapping("/createOrder")
    @ResponseBody
    public Result createOrder(HttpServletRequest request,
                              @RequestParam("receiverName") String receiverName,
                              @RequestParam("receiverPhone") String receiverPhone,
                              @RequestParam("receiverAddress") String receiverAddress,
                              @RequestParam(value = "remark", required = false) String remark) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        Orders order = orderBiz.createOrderFromCart(userId, receiverName, receiverPhone, receiverAddress, remark);
        if (order == null) {
            return Result.error("创建订单失败");
        }
        return Result.success(order);
    }

    @GetMapping("/listOrders")
    @ResponseBody
    public Result listOrders(HttpServletRequest request) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        List<Orders> orders = orderBiz.findOrdersByUserId(userId);
        return Result.success(orders);
    }

    @GetMapping("/orderDetail")
    @ResponseBody
    public Result orderDetail(@RequestParam("orderNo") String orderNo) {
        Orders order = orderBiz.findByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        List<OrderItem> items = orderBiz.findItemsByOrderId(order.getId());

        // 为前端补充当前用户在该订单下每个商品的评论ID，用于区分“评价/追评”
        if (order.getUserId() != null && items != null && !items.isEmpty()) {
            for (OrderItem item : items) {
                List<Comment> comments = commentMapper.findByOrderAndProduct(
                        order.getId(),
                        item.getProductId(),
                        order.getUserId()
                );
                if (comments != null && !comments.isEmpty()) {
                    item.setCommentId(comments.get(0).getId());
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return Result.success(data);
    }

    @PostMapping("/cancelOrder")
    @ResponseBody
    public Result cancelOrder(HttpServletRequest request,
                              @RequestParam("orderNo") String orderNo) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = orderBiz.cancelOrder(userId, orderNo);
        if (!success) {
            return Result.error("取消订单失败");
        }
        return Result.success("取消订单成功");
    }

    @PostMapping("/shipOrder")
    @ResponseBody
    public Result shipOrder(HttpServletRequest request,
                            @RequestParam("orderNo") String orderNo) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        boolean success = orderBiz.markShipped(orderNo);
        if (!success) {
            return Result.error("发货失败");
        }
        return Result.success("发货成功");
    }

    @PostMapping("/confirmReceive")
    @ResponseBody
    public Result confirmReceive(HttpServletRequest request,
                                 @RequestParam("orderNo") String orderNo) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = orderBiz.confirmReceive(userId, orderNo);
        if (!success) {
            return Result.error("确认收货失败");
        }
        return Result.success("确认收货成功");
    }

    @PostMapping("/finishComment")
    @ResponseBody
    public Result finishComment(HttpServletRequest request,
                                @RequestParam("orderNo") String orderNo) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = orderBiz.markCommented(userId, orderNo);
        if (!success) {
            return Result.error("完成评价失败");
        }
        return Result.success("完成评价成功");
    }

    @PostMapping("/applyRefund")
    @ResponseBody
    public Result applyRefund(HttpServletRequest request,
                              @RequestParam("orderNo") String orderNo) {
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.unAuth("用户未登录");
        }
        boolean success = orderBiz.applyRefund(userId, orderNo);
        if (!success) {
            return Result.error("申请退款/售后失败");
        }
        return Result.success("申请退款/售后成功");
    }

    @GetMapping(value = "/pay", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String pay(@RequestParam("orderNo") String orderNo) {
        String form = payBiz.createAlipayPagePay(orderNo);
        if (form == null) {
            return "支付发起失败";
        }
        return form;
    }

    @PostMapping("/alipayNotify")
    @ResponseBody
    public String alipayNotify(@RequestParam Map<String, String> params) {
        System.out.println("OrderController.alipayNotify params = " + params);
        boolean success = payBiz.handleAlipayNotify(params);
        System.out.println("OrderController.alipayNotify handle result = " + success);
        return success ? "success" : "fail";
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
