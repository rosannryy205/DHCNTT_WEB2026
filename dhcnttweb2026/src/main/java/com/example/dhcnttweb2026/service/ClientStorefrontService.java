package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.controller.ClientProductController.*;
import com.example.dhcnttweb2026.entity.Category;
import com.example.dhcnttweb2026.entity.Product;
import com.example.dhcnttweb2026.entity.ProductVariant;
import com.example.dhcnttweb2026.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ClientStorefrontService {

    private final ProductRepository productRepository;

    public ClientStorefrontService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // DTOs for Home page
    public record HomeTileProductDto(
            Long id,
            String name,
            String slug,
            String tagline,
            String badge,
            String imageUrl,
            String startingPriceFormatted
    ) {}

    public record FeaturedProductCardDto(
            Long id,
            String name,
            String slug,
            String categoryName,
            String description,
            String imageUrl,
            String startingPriceFormatted,
            String detailUrl
    ) {}

    public record ProductDetailResult(
            String productName,
            String tagline,
            String startingPriceFormatted,
            List<ProductColorDto> colors,
            List<ProductStorageDto> storages,
            List<ProductVariantDto> variants,
            List<InTheBoxItemDto> inTheBox,
            List<TechSpecGroupDto> techSpecs,
            List<CardItemDto> compatibleAccessories,
            List<CardItemDto> relatedProducts
    ) {}

    @Transactional(readOnly = true)
    public HomeTileProductDto getHeroProduct() {
        return findTileProductBySlugOrDefault("iphone-16-pro", "Titanium. Vô cùng mạnh mẽ. Vô cùng nhẹ.");
    }

    @Transactional(readOnly = true)
    public HomeTileProductDto getDarkTileProduct() {
        return findTileProductBySlugOrDefault("macbook-pro-14-m3-pro", "Trang bị chip Apple silicon thế hệ mới. Vượt xa mọi giới hạn.");
    }

    @Transactional(readOnly = true)
    public HomeTileProductDto getParchmentTileProduct() {
        HomeTileProductDto dto = findTileProductBySlugOrDefault("apple-watch-series-10", "Mỏng hơn bao giờ hết. Màn hình lớn nhất từng có.");
        if (dto != null) {
            return new HomeTileProductDto(dto.id(), dto.name(), dto.slug(), dto.tagline(), "Mới ra mắt", dto.imageUrl(), dto.startingPriceFormatted());
        }
        return null;
    }

    private HomeTileProductDto findTileProductBySlugOrDefault(String targetSlug, String fallbackTagline) {
        Optional<Product> opt = productRepository.findBySlugWithDetails(targetSlug);
        if (opt.isEmpty()) {
            List<Product> all = productRepository.findAllActiveWithDetails();
            opt = all.stream().findFirst();
        }

        if (opt.isEmpty()) {
            return null;
        }

        Product p = opt.get();
        long minPrice = getMinPrice(p);
        String img = getMainImage(p);
        String tagline = (p.getDescription() != null && !p.getDescription().isBlank()) ? p.getDescription() : fallbackTagline;

        return new HomeTileProductDto(
                p.getId(),
                p.getName(),
                p.getSlug(),
                tagline,
                "Chính hãng Apple",
                img,
                "Từ " + formatVND(minPrice)
        );
    }

    @Transactional(readOnly = true)
    public List<FeaturedProductCardDto> getFeaturedProducts(int limit) {
        List<Product> all = productRepository.findAllActiveWithDetails();
        List<FeaturedProductCardDto> result = new ArrayList<>();

        for (Product p : all) {
            if (result.size() >= limit) break;
            // Tránh lặp lại đúng sản phẩm Hero chính để trang chủ phong phú
            long minPrice = getMinPrice(p);
            String categoryName = (p.getCategory() != null) ? p.getCategory().getName() : "Apple";
            String img = getMainImage(p);

            result.add(new FeaturedProductCardDto(
                    p.getId(),
                    p.getName(),
                    p.getSlug(),
                    categoryName,
                    p.getDescription() != null ? p.getDescription() : "Thiết bị Apple chính hãng",
                    img,
                    "Từ " + formatVND(minPrice),
                    "/product/" + p.getSlug()
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Optional<ProductDetailResult> getProductDetailBySlug(String slug) {
        Optional<Product> opt = productRepository.findBySlugWithDetails(slug);
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        Product product = opt.get();
        List<ProductVariant> variants = product.getVariants();
        if (variants == null || variants.isEmpty()) {
            return Optional.empty();
        }

        long minPrice = getMinPrice(product);

        // 1. Phân nhóm Colors
        Map<String, String> colorPreviewMap = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String c = (v.getColor() != null && !v.getColor().isBlank()) ? v.getColor() : "Tiêu chuẩn";
            if (!colorPreviewMap.containsKey(c)) {
                colorPreviewMap.put(c, (v.getImage() != null && !v.getImage().isBlank()) ? v.getImage() : getMainImage(product));
            }
        }
        List<ProductColorDto> colors = colorPreviewMap.entrySet().stream()
                .map(e -> new ProductColorDto(e.getKey(), resolveColorHex(e.getKey()), e.getValue()))
                .toList();

        // 2. Phân nhóm Storages
        Map<String, Long> storagePriceMap = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String s = (v.getStorage() != null && !v.getStorage().isBlank()) ? v.getStorage() : "Tiêu chuẩn";
            long price = (v.getPrice() != null) ? v.getPrice().longValue() : 0L;
            storagePriceMap.merge(s, price, Math::min);
        }
        List<ProductStorageDto> storages = storagePriceMap.entrySet().stream()
                .map(e -> new ProductStorageDto(e.getKey(), e.getValue(), formatVND(e.getValue())))
                .toList();

        // 3. Mảng Variants Dto
        List<ProductVariantDto> variantDtos = new ArrayList<>();
        for (ProductVariant v : variants) {
            String color = (v.getColor() != null && !v.getColor().isBlank()) ? v.getColor() : "Tiêu chuẩn";
            String storage = (v.getStorage() != null && !v.getStorage().isBlank()) ? v.getStorage() : "Tiêu chuẩn";
            long price = (v.getPrice() != null) ? v.getPrice().longValue() : 0L;
            long origPrice = (v.getOriginalPrice() != null) ? v.getOriginalPrice().longValue() : price;
            int stock = (v.getStock() != null) ? v.getStock() : 0;

            String img = (v.getImage() != null && !v.getImage().isBlank()) ? v.getImage() : getMainImage(product);
            List<String> images = List.of(img);

            variantDtos.add(new ProductVariantDto(
                    v.getId(),
                    v.getSku(),
                    color,
                    resolveColorHex(color),
                    storage,
                    price,
                    formatVND(price),
                    origPrice,
                    formatVND(origPrice),
                    stock,
                    images
            ));
        }

        // 4. Trong hộp có gì (In the box)
        List<InTheBoxItemDto> inTheBox = buildInTheBox(product);

        // 5. Thông số kỹ thuật (Tech Specs)
        List<TechSpecGroupDto> techSpecs = buildTechSpecs(product, variants);

        // 6. Phụ kiện tương thích (Compatible Accessories từ DB)
        List<CardItemDto> compatibleAccessories = getCompatibleAccessoriesFromDb(product.getId());

        // 7. Sản phẩm liên quan (Related Products từ DB)
        List<CardItemDto> relatedProducts = getRelatedProductsFromDb(product);

        return Optional.of(new ProductDetailResult(
                product.getName(),
                product.getDescription() != null ? product.getDescription() : "Sản phẩm công nghệ Apple chính hãng",
                formatVND(minPrice),
                colors,
                storages,
                variantDtos,
                inTheBox,
                techSpecs,
                compatibleAccessories,
                relatedProducts
        ));
    }

    private List<InTheBoxItemDto> buildInTheBox(Product product) {
        String name = product.getName().toLowerCase();
        String mainImg = getMainImage(product);

        if (name.contains("iphone")) {
            return List.of(
                    new InTheBoxItemDto(product.getName() + " với iOS", mainImg, "Thiết bị chính"),
                    new InTheBoxItemDto("Cáp sạc USB-C (1m)", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MQKJ3?wid=300&hei=300&fmt=jpeg&qlt=90", "Thiết kế sợi bện dù chống gập gãy"),
                    new InTheBoxItemDto("Tài liệu hướng dẫn & Que chọc SIM", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-box-cable-202409?wid=300&hei=300&fmt=jpeg&qlt=90", "Tài liệu thân thiện môi trường")
            );
        } else if (name.contains("macbook") || name.contains("mac")) {
            return List.of(
                    new InTheBoxItemDto(product.getName(), mainImg, "Thiết bị chính"),
                    new InTheBoxItemDto("Cáp USB-C sang MagSafe 3 (2m)", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MQKJ3?wid=300&hei=300&fmt=jpeg&qlt=90", "Kết nối nam châm sạc nhanh"),
                    new InTheBoxItemDto("Bộ tiếp hợp nguồn USB-C", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MU7T2_GEO_VN?wid=300&hei=300&fmt=jpeg&qlt=90", "Công suất sạc tối ưu theo máy")
            );
        } else if (name.contains("watch")) {
            return List.of(
                    new InTheBoxItemDto(product.getName(), mainImg, "Thân vỏ đồng hồ Apple Watch"),
                    new InTheBoxItemDto("Dây đeo thể thao", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-1-202409?wid=300&hei=300&fmt=jpeg&qlt=90", "Chất liệu cao cấp co giãn"),
                    new InTheBoxItemDto("Cáp sạc nhanh từ tính USB-C (1m)", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MX6X3?wid=300&hei=300&fmt=jpeg&qlt=90", "Sạc không dây tiện lợi")
            );
        } else if (name.contains("airpods")) {
            return List.of(
                    new InTheBoxItemDto(product.getName(), mainImg, "Tai nghe chính hãng"),
                    new InTheBoxItemDto("Hộp sạc MagSafe (USB-C)", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-pro-2-hero-select-202409?wid=300&hei=300&fmt=jpeg&qlt=90", "Tích hợp loa tìm kiếm và móc dây đeo"),
                    new InTheBoxItemDto("Cáp sạc USB-C", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MQKJ3?wid=300&hei=300&fmt=jpeg&qlt=90", "Sạc nguồn tiêu chuẩn")
            );
        }

        return List.of(
                new InTheBoxItemDto(product.getName(), mainImg, "Sản phẩm chính"),
                new InTheBoxItemDto("Tài liệu hướng dẫn & Bảo hành", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MQKJ3?wid=300&hei=300&fmt=jpeg&qlt=90", "Bảo hành chính hãng 12 tháng")
        );
    }

    private List<TechSpecGroupDto> buildTechSpecs(Product product, List<ProductVariant> variants) {
        String name = product.getName();
        String desc = product.getDescription() != null ? product.getDescription() : "";

        List<TechSpecGroupDto> groups = new ArrayList<>();

        // Group 1: Thiết kế & Tổng quan
        Map<String, String> designSpecs = new LinkedHashMap<>();
        designSpecs.put("Tên thiết bị", name);
        designSpecs.put("Hãng sản xuất", "Apple");
        if (!desc.isBlank()) {
            designSpecs.put("Đặc điểm nổi bật", desc);
        }
        groups.add(new TechSpecGroupDto("Tổng quan & Thiết kế", designSpecs));

        // Group 2: Cấu hình bộ nhớ & Phiên bản
        Map<String, String> configSpecs = new LinkedHashMap<>();
        Set<String> storages = new LinkedHashSet<>();
        Set<String> colors = new LinkedHashSet<>();
        Set<String> rams = new LinkedHashSet<>();

        for (ProductVariant v : variants) {
            if (v.getStorage() != null && !v.getStorage().isBlank()) storages.add(v.getStorage());
            if (v.getColor() != null && !v.getColor().isBlank()) colors.add(v.getColor());
            if (v.getRam() != null && !v.getRam().isBlank()) rams.add(v.getRam());
        }

        if (!storages.isEmpty()) configSpecs.put("Tùy chọn dung lượng", String.join(", ", storages));
        if (!colors.isEmpty()) configSpecs.put("Tùy chọn màu sắc", String.join(", ", colors));
        if (!rams.isEmpty()) configSpecs.put("Bộ nhớ RAM", String.join(", ", rams));

        groups.add(new TechSpecGroupDto("Cấu hình & Tùy chọn", configSpecs));

        // Group 3: Kết nối & Bảo hành
        Map<String, String> supportSpecs = new LinkedHashMap<>();
        supportSpecs.put("Cổng kết nối", "USB-C chuẩn tốc độ cao");
        supportSpecs.put("Bảo hành", "12 tháng chính hãng toàn quốc");
        supportSpecs.put("Đổi trả", "Đổi mới trong 14 ngày nếu có lỗi từ nhà sản xuất");
        groups.add(new TechSpecGroupDto("Bảo hành & Kết nối", supportSpecs));

        return groups;
    }

    private List<CardItemDto> getCompatibleAccessoriesFromDb(Long currentProductId) {
        List<Product> all = productRepository.findAllActiveWithDetails();
        List<CardItemDto> list = new ArrayList<>();

        for (Product p : all) {
            if (Objects.equals(p.getId(), currentProductId)) continue;
            // Ưu tiên phụ kiện
            boolean isAccessory = (p.getCategory() != null && (
                    "phu-kien".equalsIgnoreCase(p.getCategory().getSlug()) ||
                    "Phụ Kiện".equalsIgnoreCase(p.getCategory().getName()) ||
                    p.getName().toLowerCase().contains("sạc") ||
                    p.getName().toLowerCase().contains("ốp") ||
                    p.getName().toLowerCase().contains("airpods")
            ));

            if (isAccessory) {
                list.add(new CardItemDto(
                        p.getName(),
                        p.getCategory() != null ? p.getCategory().getName() : "Phụ kiện",
                        getMainImage(p),
                        formatVND(getMinPrice(p)),
                        "/product/" + p.getSlug()
                ));
            }
            if (list.size() >= 4) break;
        }

        return list;
    }

    private List<CardItemDto> getRelatedProductsFromDb(Product currentProduct) {
        List<Product> all = productRepository.findAllActiveWithDetails();
        List<CardItemDto> list = new ArrayList<>();

        for (Product p : all) {
            if (Objects.equals(p.getId(), currentProduct.getId())) continue;
            list.add(new CardItemDto(
                    p.getName(),
                    p.getCategory() != null ? p.getCategory().getName() : "Apple",
                    getMainImage(p),
                    "Từ " + formatVND(getMinPrice(p)),
                    "/product/" + p.getSlug()
            ));
            if (list.size() >= 4) break;
        }

        return list;
    }

    private long getMinPrice(Product p) {
        if (p.getVariants() == null || p.getVariants().isEmpty()) {
            return 0L;
        }
        return p.getVariants().stream()
                .filter(v -> v.getPrice() != null)
                .mapToLong(v -> v.getPrice().longValue())
                .min()
                .orElse(0L);
    }

    private String getMainImage(Product p) {
        if (p.getVariants() != null) {
            for (ProductVariant v : p.getVariants()) {
                if (v.getImage() != null && !v.getImage().isBlank()) {
                    return v.getImage();
                }
            }
        }
        return "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90";
    }

    public static String resolveColorHex(String colorName) {
        if (colorName == null) return "#333333";
        String lower = colorName.toLowerCase().trim();
        if (lower.contains("sa mạc") || lower.contains("desert")) return "#c5b6a7";
        if (lower.contains("tự nhiên") || lower.contains("natural")) return "#9e9b94";
        if (lower.contains("titan trắng") || lower.contains("white titanium")) return "#f2f1ed";
        if (lower.contains("titan đen") || lower.contains("black titanium")) return "#3b3a3e";
        if (lower.contains("không gian") || lower.contains("space")) return "#2e2c2f";
        if (lower.contains("mòng két") || lower.contains("teal")) return "#478280";
        if (lower.contains("hồng") || lower.contains("pink")) return "#e0a6b2";
        if (lower.contains("lưu ly") || lower.contains("ultramarine")) return "#4a6b82";
        if (lower.contains("xanh dương") || lower.contains("blue")) return "#537895";
        if (lower.contains("xanh đêm") || lower.contains("midnight")) return "#1e2430";
        if (lower.contains("ánh sao") || lower.contains("starlight")) return "#e8e3d6";
        if (lower.contains("bạc") || lower.contains("silver")) return "#e2e3e5";
        if (lower.contains("tím") || lower.contains("purple")) return "#9a8db4";
        if (lower.contains("jet black") || lower.contains("đen bóng")) return "#111112";
        if (lower.contains("đen") || lower.contains("black")) return "#1f2022";
        if (lower.contains("trắng") || lower.contains("white")) return "#f7f7f7";
        if (lower.contains("vàng") || lower.contains("gold")) return "#fad7a0";
        if (lower.contains("xanh denim") || lower.contains("denim")) return "#4a5e6d";
        return "#555555";
    }

    public static String formatVND(long amount) {
        return String.format(Locale.GERMAN, "%,d", amount) + "đ";
    }
}
