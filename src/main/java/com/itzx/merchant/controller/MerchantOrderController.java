package com.itzx.merchant.controller;

import com.itzx.merchant.dto.MerchantOrderDTO;
import com.itzx.merchant.mapper.MerchantOrderMapper;
import com.itzx.until.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/order")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class MerchantOrderController {

    @Autowired
    private MerchantOrderMapper merchantOrderMapper;

    @GetMapping("/listAll")
    @ResponseBody
    public Result listAllOrders(HttpServletRequest request,
                                @RequestParam(value = "username", required = false) String username,
                                @RequestParam(value = "status", required = false) Integer status,
                                @RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        List<MerchantOrderDTO> orders = merchantOrderMapper.findByCondition(username, status, startDate, endDate);
        return Result.success(orders);
    }
}
