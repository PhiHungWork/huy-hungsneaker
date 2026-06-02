package com.example.QuanLiThuChi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String role; // ROLE_ADMIN, ROLE_STAFF

    @Column(unique = true, length = 20)
    private String phone;

    @Column(columnDefinition = "boolean default true")
    private Boolean enabled = true;
}
