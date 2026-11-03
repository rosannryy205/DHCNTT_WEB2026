package com.example.dhcnttweb2026.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeForUpdate(@Param("orderCode") String orderCode);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user "
            + "LEFT JOIN FETCH o.orderItems i LEFT JOIN FETCH i.productVariant v "
            + "LEFT JOIN FETCH v.product ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findAllWithDetails();

    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByUser(User user);
    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeWithUser(@Param("orderCode") String orderCode);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user u "
            + "LEFT JOIN FETCH o.orderItems i LEFT JOIN FETCH i.productVariant v "
            + "LEFT JOIN FETCH v.product WHERE u.id = :userId ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.user u "
            + "LEFT JOIN FETCH o.orderItems i LEFT JOIN FETCH i.productVariant v "
            + "LEFT JOIN FETCH v.product WHERE o.orderCode = :orderCode AND u.id = :userId")
    Optional<Order> findByOrderCodeAndUserIdWithDetails(@Param("orderCode") String orderCode, @Param("userId") Long userId);
}
