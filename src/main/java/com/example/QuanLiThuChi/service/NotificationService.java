package com.example.QuanLiThuChi.service;

import com.example.QuanLiThuChi.entity.Order;

public interface NotificationService {
    void notifyOrderCreated(Order order);

    void notifyOrderStatusChanged(Order order);
}
