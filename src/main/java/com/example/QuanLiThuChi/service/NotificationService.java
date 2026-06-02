package com.example.QuanLiThuChi.service;

import com.example.QuanLiThuChi.entity.Order;

public interface NotificationService {
    NotificationResult notifyOrderCreated(Order order);

    void notifyOrderStatusChanged(Order order);
}
