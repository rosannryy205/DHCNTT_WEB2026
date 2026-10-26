package com.example.dhcnttweb2026.model;

public class Product {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String category;
    private String brand;
    private String color;
    private String storage;
    private String ram;
    private long originalPrice;
    private long price;
    private int stock;
    private String status;
    private String imageUrl;
    private String tagline;

    public Product() {}

    public Product(Long id, String name, String sku, String category, long price, int stock, String status, String imageUrl, String tagline) {
        this(id, name, null, sku, category, "Apple", null, null, null, price, price, stock, status, imageUrl, tagline);
    }

    public Product(Long id, String name, String slug, String sku, String category, String brand, 
                   String color, String storage, String ram, long originalPrice, long price, 
                   int stock, String status, String imageUrl, String tagline) {
        this.id = id;
        this.name = name;
        this.slug = slug != null ? slug : generateSlug(name);
        this.sku = sku;
        this.category = category;
        this.brand = brand != null ? brand : "Apple";
        this.color = color;
        this.storage = storage;
        this.ram = ram;
        this.originalPrice = originalPrice;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.imageUrl = imageUrl;
        this.tagline = tagline;
    }

    public static String generateSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = generateSlug(name);
        }
    }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }
    public long getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(long originalPrice) { this.originalPrice = originalPrice; }
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
