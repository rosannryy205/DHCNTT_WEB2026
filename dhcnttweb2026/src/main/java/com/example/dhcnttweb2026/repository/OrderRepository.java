package com.example.dhcnttweb2026.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByUser(User user);
    List<Order> findByUserId(Long userId);
}
