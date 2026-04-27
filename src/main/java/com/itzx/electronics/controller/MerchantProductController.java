package com.itzx.electronics.controller;

import com.itzx.electronics.biz.ProductBiz;
import com.itzx.electronics.entity.Product;
import com.itzx.until.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/product")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class MerchantProductController {

    @Autowired
    private ProductBiz productBiz;

    @PostMapping("/addProduct")
    @ResponseBody
    public Result addProduct(HttpServletRequest request, @RequestBody Product product) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        boolean success = productBiz.addProduct(product);
        if (!success) {
            return Result.error("添加商品失败");
        }
        return Result.success("添加商品成功");
    }

    @PostMapping("/updateProduct")
    @ResponseBody
    public Result updateProduct(HttpServletRequest request, @RequestBody Product product) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }
        boolean success = productBiz.updateProduct(product);
        if (!success) {
            return Result.error("修改商品失败");
        }
        return Result.success("修改商品成功");
    }
}
