package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.CategoryBrand;

import java.util.List;

@Repository
public interface CategoryBrandRepository extends JpaRepository<CategoryBrand, Long> {
    List<CategoryBrand> findByCategoryId(Long categoryId);
    List<CategoryBrand> findByBrandId(Long brandId);
}
