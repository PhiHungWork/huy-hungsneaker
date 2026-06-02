package com.example.QuanLiThuChi.service;

import com.example.QuanLiThuChi.entity.Order;
import java.util.List;

public interface OrderService {
    Order placeOrder(String customerName, String phone, String address, Long variantId, int quantity);
    List<Order> getAllOrders();
    void confirmOrder(Long orderId);
}
