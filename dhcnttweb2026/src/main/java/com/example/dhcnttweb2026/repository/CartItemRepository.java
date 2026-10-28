package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);

    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.productVariant pv " +
           "JOIN FETCH pv.product p " +
           "WHERE ci.cart.id = :cartId ORDER BY ci.createdAt DESC")
    List<CartItem> findByCartIdWithProductDetails(@Param("cartId") Long cartId);
}
