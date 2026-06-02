package com.example.QuanLiThuChi.repository;

import com.example.QuanLiThuChi.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT DISTINCT oi.variant.product.name FROM OrderItem oi " +
            "WHERE oi.order.createdBy.id = :userId AND oi.order.status <> 'CANCELLED'")
    List<String> findDistinctPurchasedProductNamesByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT oi.variant.product.name FROM OrderItem oi " +
            "WHERE oi.order.id IN :orderIds")
    List<String> findDistinctPurchasedProductNamesByOrderIds(@Param("orderIds") List<Long> orderIds);
}
