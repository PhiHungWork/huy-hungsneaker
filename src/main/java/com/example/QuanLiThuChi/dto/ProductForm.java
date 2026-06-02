package com.example.QuanLiThuChi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductForm {
    private Long id;

    @NotBlank(message = "Mã sản phẩm không được trống")
    private String code;

    @NotBlank(message = "Tên sản phẩm không được trống")
    private String name;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    @NotBlank(message = "Thương hiệu không được trống")
    private String brand;

    @NotNull(message = "Giá không được trống")
    @Min(value = 0, message = "Giá không hợp lệ")
    private BigDecimal price;

    private String imageUrl;
    private String status;
}
