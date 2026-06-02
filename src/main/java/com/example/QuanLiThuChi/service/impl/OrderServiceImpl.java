package com.example.QuanLiThuChi.service.impl;

import com.example.QuanLiThuChi.entity.Order;
import com.example.QuanLiThuChi.entity.OrderItem;
import com.example.QuanLiThuChi.entity.ProductVariant;
import com.example.QuanLiThuChi.repository.OrderItemRepository;
import com.example.QuanLiThuChi.repository.OrderRepository;
import com.example.QuanLiThuChi.repository.ProductVariantRepository;
import com.example.QuanLiThuChi.service.NotificationService;
import com.example.QuanLiThuChi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Order placeOrder(String customerName, String phone, String address, Long variantId, int quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (variant.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for size " + variant.getSize());
        }

        // Deduct stock
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        if (variant.getStockQuantity() == 0) {
            variant.getProduct().setStatus("OUT_OF_STOCK");
        }
        productVariantRepository.save(variant);

        // Create Order
        Order order = new Order();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(customerName);
        order.setCustomerPhone(phone);
        order.setCustomerAddress(address);
        order.setStatus("NEW");

        BigDecimal unitPrice = variant.getProduct().getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        order.setTotalAmount(lineTotal);
        
        Order savedOrder = orderRepository.save(order);

        // Create Order Item
        OrderItem item = new OrderItem();
        item.setOrder(savedOrder);
        item.setVariant(variant);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setLineTotal(lineTotal);
        
        orderItemRepository.save(item);

        return savedOrder;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("COMPLETED");
        orderRepository.save(order);
        notificationService.notifyOrderStatusChanged(order);
    }
}
