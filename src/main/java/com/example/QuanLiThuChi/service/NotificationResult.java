package com.example.QuanLiThuChi.service;

public class NotificationResult {
    private final boolean adminSent;
    private final boolean customerAttempted;
    private final boolean customerSent;

    public NotificationResult(boolean adminSent, boolean customerAttempted, boolean customerSent) {
        this.adminSent = adminSent;
        this.customerAttempted = customerAttempted;
        this.customerSent = customerSent;
    }

    public boolean isAdminSent() {
        return adminSent;
    }

    public boolean isCustomerAttempted() {
        return customerAttempted;
    }

    public boolean isCustomerSent() {
        return customerSent;
    }
}

