package com.example.QuanLiThuChi.repository;

import com.example.QuanLiThuChi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    List<User> findByRole(String role);
    List<User> findByRoleIn(List<String> roles);
}
