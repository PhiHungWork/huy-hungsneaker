package com.example.QuanLiThuChi.repository;

import com.example.QuanLiThuChi.entity.ProductApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductApprovalRepository extends JpaRepository<ProductApproval, Long> {
    List<ProductApproval> findAllByOrderByRequestedAtDesc();
}
