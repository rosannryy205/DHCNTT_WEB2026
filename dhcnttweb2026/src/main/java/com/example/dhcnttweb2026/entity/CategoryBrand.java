package com.example.dhcnttweb2026.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "category_brand", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"category_id", "brand_id"})
})
public class CategoryBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    public CategoryBrand() {
    }

    public CategoryBrand(Category category, Brand brand) {
        this.category = category;
        this.brand = brand;
    }

    public CategoryBrand(Long id, Category category, Brand brand) {
        this.id = id;
        this.category = category;
        this.brand = brand;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
