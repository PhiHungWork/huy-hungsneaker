package com.example.QuanLiThuChi.repository;

import com.example.QuanLiThuChi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.status = 'COMPLETED' AND o.orderDate >= :startDate AND o.orderDate <= :endDate")
    List<Order> findCompletedOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' AND o.orderDate >= :startDate")
    java.math.BigDecimal sumCompletedRevenueAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o FROM Order o WHERE o.status = 'COMPLETED' AND o.orderDate >= :startDate")
    List<Order> findCompletedOrdersAfter(@Param("startDate") LocalDateTime startDate);

    long countByStatus(String status);
    long countByStatusIn(Collection<String> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdBy.id = :userId AND o.status <> 'CANCELLED'")
    long countPurchasesByUserId(@Param("userId") Long userId);

    List<Order> findByCreatedByIdAndStatusNot(Long userId, String status);

    List<Order> findByStatusNot(String status);

    Optional<Order> findTopByCreatedByIdOrderByOrderDateDesc(Long userId);

    Optional<Order> findTopByOrderCodeIgnoreCaseOrderByOrderDateDesc(String orderCode);
}
