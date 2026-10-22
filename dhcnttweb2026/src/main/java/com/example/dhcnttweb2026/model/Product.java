package com.example.dhcnttweb2026.model;

public class Product {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private long price;
    private int stock;
    private String status;
    private String imageUrl;
    private String tagline;

    public Product() {}

    public Product(Long id, String name, String sku, String category, long price, int stock, String status, String imageUrl, String tagline) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.imageUrl = imageUrl;
        this.tagline = tagline;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
}