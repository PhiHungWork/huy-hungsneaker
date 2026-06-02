package com.example.QuanLiThuChi.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

public interface ExcelExportService {
    void exportRevenueReport(HttpServletResponse response, LocalDateTime startDate, LocalDateTime endDate) throws Exception;
}
