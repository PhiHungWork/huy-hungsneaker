package com.example.QuanLiThuChi.service.impl;

import com.example.QuanLiThuChi.entity.Order;
import com.example.QuanLiThuChi.repository.OrderRepository;
import com.example.QuanLiThuChi.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {
    private final OrderRepository orderRepository;

    @Override
    public void exportRevenueReport(HttpServletResponse response, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        List<Order> orders = orderRepository.findCompletedOrdersBetweenDates(startDate, endDate);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        Sheet sheet = workbook.createSheet("Báo Cáo Doanh Thu Huy & Hưng");

        // Header Style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Number Style
        CellStyle numberStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        numberStyle.setDataFormat(format.getFormat("#,##0\" ₫\""));

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Mã Đơn", "Ngày", "Khách Hàng", "SĐT", "Tổng Tiền"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            if (i == 4) {
                sheet.setColumnWidth(i, 25 * 256);
            } else {
                sheet.setColumnWidth(i, 20 * 256);
            }
        }

        int rowNum = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getOrderCode());
            row.createCell(1).setCellValue(order.getOrderDate().toString());
            row.createCell(2).setCellValue(order.getCustomerName());
            row.createCell(3).setCellValue(order.getCustomerPhone());
            
            Cell totalCell = row.createCell(4);
            totalCell.setCellValue(order.getTotalAmount().doubleValue());
            totalCell.setCellStyle(numberStyle);
        }

        // Footer Row
        Row footerRow = sheet.createRow(rowNum);
        Cell footerLabel = footerRow.createCell(3);
        footerLabel.setCellValue("TỔNG CỘNG:");
        
        Cell sumCell = footerRow.createCell(4);
        sumCell.setCellFormula("SUM(E2:E" + rowNum + ")");
        sumCell.setCellStyle(numberStyle);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=DoanhThu.xlsx");

        workbook.write(response.getOutputStream());
        workbook.dispose();
    }
}
