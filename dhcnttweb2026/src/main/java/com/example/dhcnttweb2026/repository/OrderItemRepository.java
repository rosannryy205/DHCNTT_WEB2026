package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi " +
           "JOIN FETCH oi.productVariant pv " +
           "JOIN FETCH pv.product p " +
           "WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithDetails(@Param("orderId") Long orderId);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.productVariant.product.id = :productId")
    boolean existsByProductId(@Param("productId") Long productId);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.productVariant.id = :variantId")
    boolean existsByProductVariantId(@Param("variantId") Long variantId);
}
