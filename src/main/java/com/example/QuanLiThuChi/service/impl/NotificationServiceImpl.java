package com.example.QuanLiThuChi.service.impl;

import com.example.QuanLiThuChi.entity.Order;
import com.example.QuanLiThuChi.service.NotificationResult;
import com.example.QuanLiThuChi.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Value("${app.web-base-url:http://localhost:8080}")
    private String webBaseUrl;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${notification.email.admin-email:admin@huyhung-sneaker.com}")
    private String adminEmail;

    @Override
    @Async
    public void notifyOrderCreated(Order order) {
        String trackingUrl = webBaseUrl + "/orders/track?orderCode=" + urlEncode(order.getOrderCode()) + "&phone=" + urlEncode(order.getCustomerPhone());

        // 1. Admin Email Content (Plain text)
        String adminSubject = "[ĐƠN HÀNG MỚI] Mã đơn: " + order.getOrderCode();
        String adminContent = String.format(
                "Thông báo có đơn hàng mới từ website Huy & Hưng Sneaker.\n\n" +
                "Chi tiết đơn hàng:\n" +
                "- Mã đơn hàng: %s\n" +
                "- Tên khách hàng: %s\n" +
                "- Số điện thoại: %s\n" +
                "- Địa chỉ nhận hàng: %s\n" +
                "- Tổng số tiền thanh toán: %,.0f ₫\n" +
                "- Trạng thái: %s\n",
                order.getOrderCode(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                safeText(order.getCustomerAddress()),
                order.getTotalAmount(),
                order.getStatus()
        );

        // 2. Customer Email Content (HTML Template)
        String customerSubject = "[Huy & Hưng Sneaker] Xác nhận đặt hàng thành công - Mã đơn: " + order.getOrderCode();
        String customerHtmlContent = "";
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("trackingUrl", trackingUrl);
            customerHtmlContent = templateEngine.process("emails/order-created-email", context);
        } catch (Exception ex) {
            log.error("Failed to render order-created-email template: {}", ex.getMessage(), ex);
        }

        dispatch(order, "ORDER_CREATED", adminSubject, adminContent, false, customerSubject, customerHtmlContent, true);
    }

    @Override
    @Async
    public void notifyOrderStatusChanged(Order order) {
        String trackingUrl = webBaseUrl + "/orders/track?orderCode=" + urlEncode(order.getOrderCode()) + "&phone=" + urlEncode(order.getCustomerPhone());

        // 1. Admin Email Content (Plain text)
        String adminSubject = "[CẬP NHẬT ĐƠN HÀNG] Mã đơn: " + order.getOrderCode();
        String adminContent = String.format(
                "Đơn hàng %s đã được cập nhật trạng thái mới: %s\n" +
                "Khách hàng: %s\n" +
                "Số điện thoại: %s\n",
                order.getOrderCode(),
                order.getStatus(),
                order.getCustomerName(),
                order.getCustomerPhone()
        );

        // 2. Customer Email Content (HTML Template)
        String customerSubject = "[Huy & Hưng Sneaker] Cập nhật trạng thái đơn hàng - Mã đơn: " + order.getOrderCode();
        String customerHtmlContent = "";
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("trackingUrl", trackingUrl);
            customerHtmlContent = templateEngine.process("emails/order-status-email", context);
        } catch (Exception ex) {
            log.error("Failed to render order-status-email template: {}", ex.getMessage(), ex);
        }

        dispatch(order, "ORDER_STATUS_CHANGED", adminSubject, adminContent, false, customerSubject, customerHtmlContent, true);
    }

    private NotificationResult dispatch(Order order, String event, 
                                         String adminSubject, String adminContent, boolean adminIsHtml, 
                                         String customerSubject, String customerContent, boolean customerIsHtml) {
        boolean adminSent = false;
        boolean customerAttempted = false;
        boolean customerSent = false;

        // 1. Send to admin
        if (adminEmail != null && !adminEmail.isBlank()) {
            adminSent = sendEmail(adminEmail, adminSubject, adminContent, adminIsHtml);
        }

        // 2. Send to customer
        String customerEmailAddress = order.getCustomerEmail();
        if (customerEmailAddress != null && !customerEmailAddress.isBlank()) {
            customerAttempted = true;
            customerSent = sendEmail(customerEmailAddress, customerSubject, customerContent, customerIsHtml);
        }

        if (!customerSent && !adminSent) {
            log.info("[NOTIFY-DRYRUN] {} -> Customer email: {}, Admin email: {}", event, customerEmailAddress, adminEmail);
        }
        return new NotificationResult(adminSent, customerAttempted, customerSent);
    }

    private boolean sendEmail(String to, String subject, String content, boolean isHtml) {
        if (mailSender == null) {
            log.warn("[EMAIL] JavaMailSender is not configured (null). Skip sending.");
            return false;
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("[EMAIL] spring.mail.username is not configured. Skip sending.");
            return false;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            mailSender.send(mimeMessage);
            log.info("[EMAIL] Successfully sent email to {}", to);
            return true;
        } catch (Exception ex) {
            log.error("[EMAIL] Failed to send email to {}: {}", to, ex.getMessage(), ex);
            return false;
        }
    }

    private String safeText(String value) {
        return Objects.requireNonNullElse(value, "");
    }

    private String urlEncode(String raw) {
        return URLEncoder.encode(safeText(raw), StandardCharsets.UTF_8);
    }
}
