package com.example.QuanLiThuChi.repository;

import com.example.QuanLiThuChi.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findByProductIdAndSize(Long productId, String size);

    @Query("SELECT COALESCE(SUM(v.stockQuantity), 0) FROM ProductVariant v WHERE v.product.id = :productId")
    Integer sumStockByProductId(@Param("productId") Long productId);

    @Query("SELECT v.product.id, COALESCE(SUM(v.stockQuantity), 0) FROM ProductVariant v WHERE v.product.id IN :productIds GROUP BY v.product.id")
    List<Object[]> sumStockByProductIds(@Param("productIds") List<Long> productIds);
}
