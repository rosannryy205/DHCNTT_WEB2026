package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.ProductVariant;

@Repository
public interface productVariantsReponsitory extends JpaRepository<ProductVariant, Long> {
    
}
