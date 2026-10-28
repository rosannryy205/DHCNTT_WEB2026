package com.example.dhcnttweb2026.model;

/**
 * DTO đại diện cho biểu mẫu quản trị và hiển thị danh mục trong Admin Console.
 * Tuân thủ đầy đủ các trường của bảng `categories` trong CSDL:
 * - id (BIGINT, PK, AUTO_INCREMENT)
 * - name (VARCHAR(255), NOT NULL)
 * - slug (VARCHAR(255), NOT NULL)
 * - parent_id (BIGINT, FK tự tham chiếu categories.id, nullable)
 * - image (VARCHAR(255), NOT NULL)
 * - is_active (BIT(1), NOT NULL, default true)
 */
public class AdminCategoryDto {

    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private String parentName;
    private String image;
    private Boolean isActive = true;
    private long productCount;

    public AdminCategoryDto() {
    }

    public AdminCategoryDto(Long id, String name, String slug, Long parentId, String parentName, String image, Boolean isActive, long productCount) {
        this.id = id;
        this.name = name;
        this.slug = slug != null ? slug : generateSlug(name);
        this.parentId = parentId;
        this.parentName = parentName;
        this.image = image;
        this.isActive = isActive != null ? isActive : true;
        this.productCount = productCount;
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = generateSlug(name);
        }
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public long getProductCount() {
        return productCount;
    }

    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }
}
