package com.example.QuanLiThuChi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderDTO {
    @NotBlank(message = "Tên không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotNull(message = "Chưa chọn biến thể")
    private Long variantId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
}
