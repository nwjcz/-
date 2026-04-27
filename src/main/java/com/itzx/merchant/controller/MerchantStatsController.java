package com.itzx.merchant.controller;

import com.itzx.merchant.dto.ProductStatsDTO;
import com.itzx.merchant.mapper.MerchantStatsMapper;
import com.itzx.until.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/stats")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class MerchantStatsController {

    @Autowired
    private MerchantStatsMapper merchantStatsMapper;

    @GetMapping("/product")
    public Result productStats(HttpServletRequest request,
                               @RequestParam(value = "startDate", required = false) String startDate,
                               @RequestParam(value = "endDate", required = false) String endDate,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "topN", required = false) Integer topN) {
        String role = (String) request.getAttribute("role");
        if (role == null || !("ADMIN".equals(role) || "MERCHANT".equals(role))) {
            return Result.unAuth("无权限操作");
        }

        LocalDate today = LocalDate.now();
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.minusDays(6).toString();
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.toString();
        }
        if (topN == null || topN <= 0) {
            topN = 10;
        }

        List<ProductStatsDTO> stats = merchantStatsMapper.findProductStats(startDate, endDate, keyword, topN);
        return Result.success(stats);
    }
}
