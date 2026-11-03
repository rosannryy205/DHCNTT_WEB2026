package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.entity.Category;
import com.example.dhcnttweb2026.dto.AdminCategoryDto;
import com.example.dhcnttweb2026.dto.NavCategoryDto;
import com.example.dhcnttweb2026.repository.CategoryBrandRepository;
import com.example.dhcnttweb2026.repository.CategoryRepository;
import com.example.dhcnttweb2026.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryBrandRepository categoryBrandRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CategoryBrandRepository categoryBrandRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryBrandRepository = categoryBrandRepository;
    }

    /**
     * Lấy toàn bộ danh sách danh mục từ CSDL, hỗ trợ tìm kiếm theo từ khóa.
     * Tự động tính số lượng sản phẩm trực thuộc cho từng danh mục.
     */
    @Transactional(readOnly = true)
    public List<AdminCategoryDto> findAll(String keyword) {
        List<Category> categories;
        if (keyword != null && !keyword.isBlank()) {
            categories = categoryRepository.findByNameContainingIgnoreCaseOrderByIdAsc(keyword.trim());
        } else {
            categories = categoryRepository.findAllByOrderByIdAsc();
        }

        List<AdminCategoryDto> result = new ArrayList<>();
        for (Category c : categories) {
            long pCount = productRepository.countByCategoryId(c.getId());
            String parentName = (c.getParentCategory() != null) ? c.getParentCategory().getName() : "— (Danh mục gốc)";
            Long parentId = (c.getParentCategory() != null) ? c.getParentCategory().getId() : null;

            result.add(new AdminCategoryDto(
                    c.getId(),
                    c.getName(),
                    c.getSlug(),
                    parentId,
                    parentName,
                    c.getImage(),
                    c.getIsActive(),
                    pCount
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<AdminCategoryDto> findDtoById(Long id) {
        if (id == null) return Optional.empty();
        return categoryRepository.findById(id).map(c -> {
            long pCount = productRepository.countByCategoryId(c.getId());
            String parentName = (c.getParentCategory() != null) ? c.getParentCategory().getName() : "— (Danh mục gốc)";
            Long parentId = (c.getParentCategory() != null) ? c.getParentCategory().getId() : null;
            return new AdminCategoryDto(
                    c.getId(),
                    c.getName(),
                    c.getSlug(),
                    parentId,
                    parentName,
                    c.getImage(),
                    c.getIsActive(),
                    pCount
            );
        });
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        if (id == null) return Optional.empty();
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAllRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    @Transactional(readOnly = true)
    public List<Category> findAllActive() {
        return categoryRepository.findByIsActiveTrue();
    }

    /**
     * Lấy cây danh mục cha - con phục vụ hiển thị Menu điều hướng và Flyout Dropdown trên Client.
     */
    @Transactional(readOnly = true)
    public List<NavCategoryDto> getNavCategories() {
        List<Category> roots = categoryRepository.findByParentCategoryIsNullAndIsActiveTrueOrderByIdAsc();
        List<NavCategoryDto> result = new ArrayList<>();

        for (Category root : roots) {
            List<Category> subs = categoryRepository.findByParentCategoryIdAndIsActiveTrueOrderByIdAsc(root.getId());
            List<NavCategoryDto> subDtos = new ArrayList<>();
            for (Category sub : subs) {
                subDtos.add(new NavCategoryDto(
                        sub.getId(),
                        sub.getName(),
                        sub.getSlug(),
                        sub.getImage(),
                        resolveIconClass(sub.getSlug()),
                        List.of()
                ));
            }

            // Nếu danh mục cha chưa có danh mục con trong CSDL, cung cấp danh mục con mặc định chuẩn Apple
            if (subDtos.isEmpty()) {
                subDtos = getDefaultSubCategories(root.getSlug());
            }

            result.add(new NavCategoryDto(
                    root.getId(),
                    root.getName(),
                    root.getSlug(),
                    root.getImage(),
                    resolveIconClass(root.getSlug()),
                    subDtos
            ));
        }

        if (result.isEmpty()) {
            return getDefaultNavTree();
        }

        return result;
    }

    public String resolveIconClass(String slug) {
        if (slug == null) return "bi-tag";
        String s = slug.toLowerCase();
        if (s.contains("iphone") || s.contains("phone")) return "bi-phone";
        if (s.contains("mac")) return "bi-laptop";
        if (s.contains("ipad")) return "bi-tablet";
        if (s.contains("watch")) return "bi-smartwatch";
        if (s.contains("airpod") || s.contains("audio") || s.contains("headphone")) return "bi-headphones";
        if (s.contains("phu-kien") || s.contains("sac") || s.contains("cable") || s.contains("case")) return "bi-plug";
        return "bi-grid";
    }

    private List<NavCategoryDto> getDefaultSubCategories(String parentSlug) {
        if (parentSlug == null) return List.of();
        String s = parentSlug.toLowerCase();
        if (s.contains("watch")) {
            return List.of(
                    new NavCategoryDto(101L, "Apple Watch Ultra 2", "apple-watch-ultra-2", "", "bi-smartwatch", List.of()),
                    new NavCategoryDto(102L, "Apple Watch Series 10", "apple-watch-series-10", "", "bi-smartwatch", List.of()),
                    new NavCategoryDto(103L, "Apple Watch SE", "apple-watch-se", "", "bi-smartwatch", List.of())
            );
        }
        if (s.contains("airpod")) {
            return List.of(
                    new NavCategoryDto(104L, "AirPods Pro (Gen 2)", "airpods-pro-2", "", "bi-headphones", List.of()),
                    new NavCategoryDto(105L, "AirPods 4", "airpods-4", "", "bi-headphones", List.of()),
                    new NavCategoryDto(106L, "AirPods Max USB-C", "airpods-max-usbc", "", "bi-headphones", List.of())
            );
        }
        if (s.contains("phu-kien")) {
            return List.of(
                    new NavCategoryDto(107L, "Sạc & Cáp Nhanh", "cu-sac-nhanh-apple-30w-usb-c", "", "bi-plug", List.of()),
                    new NavCategoryDto(108L, "Ốp lưng Silicone MagSafe", "op-lung-silicone-magsafe-iphone-16-pro", "", "bi-shield-check", List.of()),
                    new NavCategoryDto(109L, "Bộ sạc MagSafe 25W", "magsafe-charger", "", "bi-lightning-charge", List.of())
            );
        }
        return List.of();
    }

    private List<NavCategoryDto> getDefaultNavTree() {
        return List.of(
                new NavCategoryDto(1L, "iPhone", "iphone", "", "bi-phone", List.of(
                        new NavCategoryDto(7L, "iPhone 16 Series", "iphone-16-series", "", "bi-phone", List.of()),
                        new NavCategoryDto(8L, "iPhone 15 Series", "iphone-15-series", "", "bi-phone", List.of())
                )),
                new NavCategoryDto(2L, "Mac", "mac", "", "bi-laptop", List.of(
                        new NavCategoryDto(9L, "MacBook Pro", "macbook-pro", "", "bi-laptop", List.of()),
                        new NavCategoryDto(10L, "MacBook Air", "macbook-air", "", "bi-laptop", List.of())
                )),
                new NavCategoryDto(3L, "iPad", "ipad", "", "bi-tablet", List.of(
                        new NavCategoryDto(11L, "iPad Pro", "ipad-pro", "", "bi-tablet", List.of()),
                        new NavCategoryDto(12L, "iPad Air", "ipad-air", "", "bi-tablet", List.of())
                )),
                new NavCategoryDto(4L, "Apple Watch", "watch", "", "bi-smartwatch", List.of(
                        new NavCategoryDto(101L, "Apple Watch Ultra 2", "apple-watch-ultra-2", "", "bi-smartwatch", List.of()),
                        new NavCategoryDto(102L, "Apple Watch Series 10", "apple-watch-series-10", "", "bi-smartwatch", List.of())
                )),
                new NavCategoryDto(5L, "AirPods", "airpods", "", "bi-headphones", List.of(
                        new NavCategoryDto(104L, "AirPods Pro (Gen 2)", "airpods-pro-2", "", "bi-headphones", List.of()),
                        new NavCategoryDto(106L, "AirPods Max USB-C", "airpods-max-usbc", "", "bi-headphones", List.of())
                )),
                new NavCategoryDto(6L, "Phụ Kiện", "phu-kien", "", "bi-plug", List.of(
                        new NavCategoryDto(107L, "Sạc & Cáp Nhanh", "cu-sac-nhanh-apple-30w-usb-c", "", "bi-plug", List.of()),
                        new NavCategoryDto(108L, "Ốp lưng Silicone MagSafe", "op-lung-silicone-magsafe-iphone-16-pro", "", "bi-shield-check", List.of())
                ))
        );
    }

    /**
     * Thêm mới hoặc cập nhật danh mục vào CSDL MySQL:
     * - Kiểm tra các trường bắt buộc (name, slug)
     * - Kiểm tra tính duy nhất của tên và slug (tránh trùng lặp)
     * - Kiểm tra quan hệ danh mục cha (không cho chọn chính mình làm cha)
     */
    @Transactional
    public Category save(AdminCategoryDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu danh mục không được để trống.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục là bắt buộc.");
        }

        String name = dto.getName().trim();
        String slug = (dto.getSlug() != null && !dto.getSlug().isBlank())
                ? dto.getSlug().trim()
                : AdminCategoryDto.generateSlug(name);

        Long id = dto.getId();

        // Kiểm tra trùng tên
        if (id == null) {
            if (categoryRepository.existsByName(name)) {
                throw new IllegalArgumentException("Tên danh mục '" + name + "' đã tồn tại trong hệ thống.");
            }
            if (categoryRepository.existsBySlug(slug)) {
                slug = slug + "-" + System.currentTimeMillis() % 1000;
            }
        } else {
            if (categoryRepository.existsByNameAndIdNot(name, id)) {
                throw new IllegalArgumentException("Tên danh mục '" + name + "' đã được sử dụng bởi danh mục khác.");
            }
            if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
                slug = slug + "-" + System.currentTimeMillis() % 1000;
            }
        }

        // Xử lý danh mục cha
        Category parentCategory = null;
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            if (id != null && dto.getParentId().equals(id)) {
                throw new IllegalArgumentException("Danh mục không thể chọn chính mình làm danh mục cha.");
            }
            parentCategory = categoryRepository.findById(dto.getParentId()).orElse(null);
            if (parentCategory == null) {
                throw new IllegalArgumentException("Danh mục cha được chọn không tồn tại.");
            }
        }

        String image = (dto.getImage() != null && !dto.getImage().isBlank())
                ? dto.getImage().trim()
                : "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90";
        Boolean isActive = (dto.getIsActive() != null) ? dto.getIsActive() : true;

        Category category;
        if (id == null) {
            category = new Category(name, slug, parentCategory, image, isActive);
        } else {
            category = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục có ID: " + id));
            category.setName(name);
            category.setSlug(slug);
            category.setParentCategory(parentCategory);
            category.setImage(image);
            category.setIsActive(isActive);
        }

        return categoryRepository.save(category);
    }

    /**
     * Xóa danh mục khi thỏa mãn đầy đủ ràng buộc dữ liệu:
     * 1. Không xóa nếu đang có sản phẩm thuộc danh mục này
     * 2. Không xóa nếu đang có danh mục con trực thuộc
     */
    @Transactional
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục để xóa (ID: " + id + ")."));

        // 1. Kiểm tra sản phẩm trực thuộc
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục '" + category.getName() +
                    "' vì đang có " + productCount + " sản phẩm trực thuộc. Vui lòng di chuyển hoặc xóa sản phẩm trước.");
        }

        // 2. Kiểm tra danh mục con
        List<Category> subCategories = categoryRepository.findByParentCategoryId(id);
        if (!subCategories.isEmpty()) {
            throw new IllegalStateException("Không thể xóa danh mục '" + category.getName() +
                    "' vì đang có " + subCategories.size() + " danh mục con trực thuộc. Vui lòng chuyển danh mục con trước.");
        }

        // 3. Xóa các bản ghi liên kết trong bảng trung gian category_brand nếu có
        var categoryBrands = categoryBrandRepository.findByCategoryId(id);
        if (!categoryBrands.isEmpty()) {
            categoryBrandRepository.deleteAll(categoryBrands);
        }

        categoryRepository.delete(category);
    }
}

