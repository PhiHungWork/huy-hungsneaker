package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.entity.Order;
import com.example.QuanLiThuChi.repository.OrderRepository;
import com.example.QuanLiThuChi.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ExcelExportService excelExportService;
    private final OrderRepository orderRepository;

    @GetMapping
    public String reportIndex(@RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              Model model) {
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            java.util.List<Order> orders = orderRepository.findCompletedOrdersBetweenDates(start, end);
            
            model.addAttribute("orders", orders);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
            java.math.BigDecimal totalRevenue = orders.stream()
                    .map(Order::getTotalAmount)
                    .filter(amount -> amount != null)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            model.addAttribute("totalRevenue", totalRevenue);
        }
        return "reports/index";
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response, 
                              @RequestParam("startDate") String startDate, 
                              @RequestParam("endDate") String endDate) throws Exception {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
        excelExportService.exportRevenueReport(response, start, end);
    }
}
