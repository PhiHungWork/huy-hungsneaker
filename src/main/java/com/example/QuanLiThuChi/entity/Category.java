package com.example.QuanLiThuChi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 120)
    private String slug;

    @Column(length = 255)
    private String description;

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE
}
