package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySlugAndIsActiveTrue(String slug);
    List<Product> findByIsActiveTrue();
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);
    List<Product> findByBrandIdAndIsActiveTrue(Long brandId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.category WHERE p.slug = :slug AND p.isActive = true")
    Optional<Product> findBySlugWithDetails(@Param("slug") String slug);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.category WHERE p.isActive = true")
    List<Product> findAllActiveWithDetails();
}
