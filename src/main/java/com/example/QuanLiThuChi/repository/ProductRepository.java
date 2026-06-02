package com.example.QuanLiThuChi.repository;

import com.example.QuanLiThuChi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    long countByStatus(String status);
    Optional<Product> findByCode(String code);
    List<Product> findByBrandIgnoreCase(String brand);
    boolean existsByCodeIgnoreCase(String code);
    List<Product> findByStatusIn(List<String> statuses);
}
