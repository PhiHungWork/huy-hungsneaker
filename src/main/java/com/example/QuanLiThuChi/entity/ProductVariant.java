package com.example.QuanLiThuChi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "size"})
})
@Data
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 10)
    private String size;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
}
