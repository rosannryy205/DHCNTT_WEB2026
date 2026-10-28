package com.example.dhcnttweb2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dhcnttweb2026.entity.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Category> findByParentCategoryIsNullAndIsActiveTrue();
    List<Category> findByParentCategoryIsNull();
    List<Category> findByParentCategoryId(Long parentId);
    List<Category> findByIsActiveTrue();
    List<Category> findAllByOrderByIdAsc();
    List<Category> findByNameContainingIgnoreCaseOrderByIdAsc(String keyword);
}
