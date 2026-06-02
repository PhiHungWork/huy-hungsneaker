package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.entity.Order;
import com.example.QuanLiThuChi.repository.OrderRepository;
import com.example.QuanLiThuChi.repository.ProductRepository;
import com.example.QuanLiThuChi.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Doanh thu tháng hiện tại
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        BigDecimal monthlyRevenue = orderRepository.sumCompletedRevenueAfter(startOfMonth);
        if (monthlyRevenue == null) {
            monthlyRevenue = BigDecimal.ZERO;
        }
        model.addAttribute("monthlyRevenue", monthlyRevenue);

        // 2. Đơn hàng mới
        long newOrders = orderRepository.countByStatusIn(Arrays.asList("NEW", "PENDING"));
        model.addAttribute("newOrders", newOrders);

        // 3. Sản phẩm hết hàng (tính theo tổng tồn biến thể)
        long outOfStock = productRepository.findAll().stream()
                .filter(p -> {
                    Integer totalStock = productVariantRepository.sumStockByProductId(p.getId());
                    return totalStock == null || totalStock <= 0;
                })
                .count();
        model.addAttribute("outOfStock", outOfStock);

        // 4. Dữ liệu biểu đồ 7 ngày
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<Order> lastSevenDaysOrders = orderRepository.findCompletedOrdersAfter(sevenDaysAgo);
        
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            chartLabels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("vi")));
            
            BigDecimal dayRevenue = lastSevenDaysOrders.stream()
                    .filter(o -> o.getOrderDate().toLocalDate().equals(date))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            chartData.add(dayRevenue);
        }
        
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        return "admin/dashboard";
    }
}
