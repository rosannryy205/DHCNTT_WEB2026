package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.CategoryBrand;

@Repository
public interface categoryBrandReponsitory extends JpaRepository<CategoryBrand, Long> {
    
}
