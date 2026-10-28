package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.ProductVariant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);
    List<ProductVariant> findByProductId(Long productId);

    @Query("SELECT pv FROM ProductVariant pv LEFT JOIN FETCH pv.product WHERE pv.sku = :sku")
    Optional<ProductVariant> findBySkuWithProduct(@Param("sku") String sku);

    @Query("SELECT pv FROM ProductVariant pv LEFT JOIN FETCH pv.product WHERE pv.id = :id")
    Optional<ProductVariant> findByIdWithProduct(@Param("id") Long id);
}
