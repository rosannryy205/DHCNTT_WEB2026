package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.dhcnttweb2026.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);

    List<CartItem> findByProductVariantId(Long productVariantId);

    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.productVariant pv " +
           "JOIN FETCH pv.product p " +
           "WHERE ci.cart.id = :cartId ORDER BY ci.createdAt DESC")
    List<CartItem> findByCartIdWithProductDetails(@Param("cartId") Long cartId);

    @Query("SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END FROM CartItem ci WHERE ci.productVariant.product.id = :productId")
    boolean existsByProductId(@Param("productId") Long productId);

    @Query("SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END FROM CartItem ci WHERE ci.productVariant.id = :variantId")
    boolean existsByProductVariantId(@Param("variantId") Long variantId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CartItem ci WHERE ci.productVariant.id = :variantId")
    void deleteByProductVariantId(@Param("variantId") Long variantId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
