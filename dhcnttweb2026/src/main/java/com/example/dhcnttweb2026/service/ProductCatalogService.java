package com.example.dhcnttweb2026.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductCatalogService {

    public record ColorSwatchDto(
            String name,
            String hex,
            String previewImage
    ) {}

    public record CatalogProductDto(
            String id,
            String name,
            String slug,
            String category,
            String categoryName,
            String tagline,
            long startingPrice,
            String formattedStartingPrice,
            String badge,
            String defaultImage,
            List<ColorSwatchDto> colors,
            List<String> storages,
            boolean inStock,
            int releaseOrder,
            int salesOrder
    ) {}

    public record ActiveFilterChipDto(
            String type,
            String label,
            String value
    ) {}

    public record CatalogPageResult(
            List<CatalogProductDto> products,
            int totalItems,
            int currentPage,
            int totalPages,
            int pageSize,
            boolean hasPrev,
            boolean hasNext,
            List<ActiveFilterChipDto> activeChips,
            String activeCategoryTitle
    ) {}

    public record CategoryOptionDto(
            String code,
            String name,
            String iconClass,
            int count,
            List<CategoryOptionDto> subCategories
    ) {
        public CategoryOptionDto(String code, String name, String iconClass, int count) {
            this(code, name, iconClass, count, List.of());
        }

        public boolean hasSubCategories() {
            return subCategories != null && !subCategories.isEmpty();
        }
    }

    public record PriceRangeOptionDto(
            String code,
            String label,
            Long min,
            Long max
    ) {}

    public record StorageOptionDto(
            String code,
            String label,
            int count
    ) {}

    public record ColorOptionDto(
            String name,
            String hex,
            int count
    ) {}

    public record FilterOptionsDto(
            List<CategoryOptionDto> categories,
            List<PriceRangeOptionDto> priceRanges,
            List<StorageOptionDto> storages,
            List<ColorOptionDto> colors
    ) {}

    private final List<CatalogProductDto> allProducts = new ArrayList<>();

    public ProductCatalogService() {
        initProductsData();
    }

    private void initProductsData() {
        // 1. iPhone 16 Pro
        allProducts.add(new CatalogProductDto(
                "prod-ip16p",
                "iPhone 16 Pro",
                "iphone-16-pro",
                "iphone",
                "iPhone",
                "Được tiện từ titan. Sức mạnh bứt phá cùng chip A18 Pro.",
                28990000L,
                formatVND(28990000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Titan Sa Mạc", "#c5b6a7", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Titan Tự Nhiên", "#9e9b94", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Titan Trắng", "#f2f1ed", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-whitetitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Titan Đen", "#3b3a3e", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-blacktitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("128GB", "256GB", "512GB", "1TB"),
                true,
                1,
                1
        ));

        // 2. iPhone 16 Pro Max
        allProducts.add(new CatalogProductDto(
                "prod-ip16pm",
                "iPhone 16 Pro Max",
                "iphone-16-pro-max",
                "iphone",
                "iPhone",
                "Màn hình 6.9 inch cực đại. Thời lượng pin tốt nhất từng có trên iPhone.",
                34990000L,
                formatVND(34990000L),
                "Bán chạy",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Titan Sa Mạc", "#c5b6a7", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Titan Tự Nhiên", "#9e9b94", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Titan Đen", "#3b3a3e", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-blacktitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("256GB", "512GB", "1TB"),
                true,
                2,
                2
        ));

        // 3. iPhone 16
        allProducts.add(new CatalogProductDto(
                "prod-ip16",
                "iPhone 16",
                "iphone-16",
                "iphone",
                "iPhone",
                "Thiết kế màu sắc rực rỡ. Nút Điều Khiển Camera hoàn toàn mới.",
                22990000L,
                formatVND(22990000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-teal?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Xanh Mòng Két", "#4b8782", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-teal?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Hồng", "#e3a8b2", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-pink?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Xanh Lưu Ly", "#49698a", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-ultramarine?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Trắng", "#f2f1ed", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-white?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Đen", "#3c3d3e", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-black?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("128GB", "256GB", "512GB"),
                true,
                3,
                3
        ));

        // 4. iPhone 15
        allProducts.add(new CatalogProductDto(
                "prod-ip15",
                "iPhone 15",
                "iphone-15",
                "iphone",
                "iPhone",
                "Dynamic Island, Camera chính 48MP và cổng kết nối USB-C tiện lợi.",
                19490000L,
                formatVND(19490000L),
                "Giá tốt",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-15-finish-select-202309-6-1inch-pink?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Hồng", "#f2d3d1", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-15-finish-select-202309-6-1inch-pink?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Xanh Lam", "#cfdcde", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-15-finish-select-202309-6-1inch-blue?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Đen", "#3b3b3c", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-15-finish-select-202309-6-1inch-black?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("128GB", "256GB", "512GB"),
                true,
                10,
                4
        ));

        // 5. MacBook Pro 14" M4
        allProducts.add(new CatalogProductDto(
                "prod-mbp14",
                "MacBook Pro 14\" M4",
                "macbook-pro-14-m4",
                "mac",
                "Mac",
                "Sức mạnh phi thường từ chip M4 thế hệ mới. Màn hình Liquid Retina XDR.",
                39990000L,
                formatVND(39990000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-spaceblack-select-202410?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Đen Không Gian", "#2e2f33", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-spaceblack-select-202410?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Bạc", "#e3e4e5", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-silver-select-202410?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("512GB", "1TB", "2TB"),
                true,
                4,
                5
        ));

        // 6. MacBook Air 13" M3
        allProducts.add(new CatalogProductDto(
                "prod-mba13",
                "MacBook Air 13\" M3",
                "macbook-air-13-m3",
                "mac",
                "Mac",
                "Mỏng nhẹ kinh ngạc. Tốc độ vút bay với chip M3.",
                27990000L,
                formatVND(27990000L),
                "Bán chạy",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mba13-midnight-select-202402?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Đen Midnight", "#1e2430", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mba13-midnight-select-202402?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Ánh Sao Starlight", "#f0e5d3", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mba13-starlight-select-202402?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Xám Không Gian", "#7d7e80", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mba13-spacegray-select-202402?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("256GB", "512GB"),
                true,
                8,
                6
        ));

        // 7. Mac mini M4
        allProducts.add(new CatalogProductDto(
                "prod-macmini",
                "Mac mini M4",
                "mac-mini-m4",
                "mac",
                "Mac",
                "Kích thước siêu nhỏ 5x5 inch. Uy lực khổng lồ từ chip M4 và M4 Pro.",
                14990000L,
                formatVND(14990000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mac-mini-202410-gallery-1?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Bạc", "#e3e4e5", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mac-mini-202410-gallery-1?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("256GB", "512GB"),
                true,
                5,
                7
        ));

        // 8. iMac 24" M4
        allProducts.add(new CatalogProductDto(
                "prod-imac24",
                "iMac 24\" M4",
                "imac-24-m4",
                "mac",
                "Mac",
                "Máy tính tất cả trong một tuyệt mỹ. Màn hình Retina 4.5K sống động.",
                34990000L,
                formatVND(34990000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/imac-24-blue-selection-hero-202410?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Xanh Dương", "#809bb8", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/imac-24-blue-selection-hero-202410?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Xanh Lá", "#a0baa8", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/imac-24-green-selection-hero-202410?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Bạc", "#e3e4e5", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/imac-24-silver-selection-hero-202410?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("256GB", "512GB"),
                true,
                6,
                8
        ));

        // 9. iPad Pro 13" M4
        allProducts.add(new CatalogProductDto(
                "prod-ipadpro13",
                "iPad Pro 13\" M4",
                "ipad-pro-13-m4",
                "ipad",
                "iPad",
                "Mỏng đến không tưởng. Màn hình Ultra Retina XDR tân tiến nhất thế giới.",
                37990000L,
                formatVND(37990000L),
                "Đỉnh cao",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-pro-finish-unselect-gallery-1-202405_GEO_US?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Đen Không Gian", "#2e2f33", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-pro-finish-unselect-gallery-1-202405_GEO_US?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Bạc", "#e3e4e5", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-pro-finish-unselect-gallery-2-202405_GEO_US?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("256GB", "512GB", "1TB", "2TB"),
                true,
                7,
                9
        ));

        // 10. iPad Air 11" M2
        allProducts.add(new CatalogProductDto(
                "prod-ipadair11",
                "iPad Air 11\" M2",
                "ipad-air-11-m2",
                "ipad",
                "iPad",
                "Mới mẻ từ trong ra ngoài. Sức mạnh chip M2 cho mọi tác vụ sáng tạo.",
                16990000L,
                formatVND(16990000L),
                "Bán chạy",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-air-finish-unselect-gallery-1-202405?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Xanh Lam", "#ccd7dc", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-air-finish-unselect-gallery-1-202405?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Tím", "#d5d1df", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-air-finish-unselect-gallery-2-202405?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Xám Không Gian", "#7d7e80", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-air-finish-unselect-gallery-3-202405?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("128GB", "256GB", "512GB"),
                true,
                9,
                10
        ));

        // 11. iPad Gen 10
        allProducts.add(new CatalogProductDto(
                "prod-ipad10",
                "iPad Gen 10",
                "ipad-10th-gen",
                "ipad",
                "iPad",
                "Màn hình Liquid Retina 10.9 inch tràn viền với 4 sắc màu cá tính.",
                9990000L,
                formatVND(9990000L),
                "Giá tốt",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-10th-gen-finish-select-202212-blue?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Xanh Lam", "#6b96c3", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-10th-gen-finish-select-202212-blue?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Vàng", "#f2cf59", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-10th-gen-finish-select-202212-yellow?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Bạc", "#e3e4e5", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-10th-gen-finish-select-202212-silver?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of("64GB", "256GB"),
                true,
                14,
                11
        ));

        // 12. Apple Watch Ultra 2
        allProducts.add(new CatalogProductDto(
                "prod-awultra2",
                "Apple Watch Ultra 2",
                "apple-watch-ultra-2",
                "watch",
                "Apple Watch",
                "Phiên bản titan đen mới tuyệt đẹp. Thiết kế cho vận động viên và nhà thám hiểm.",
                21990000L,
                formatVND(21990000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/watch-ultra-2-black-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Titan Đen", "#2e2e30", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/watch-ultra-2-black-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Titan Tự Nhiên", "#b7b2a8", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/watch-ultra-2-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of(),
                true,
                11,
                12
        ));

        // 13. Apple Watch Series 10
        allProducts.add(new CatalogProductDto(
                "prod-aws10",
                "Apple Watch Series 10",
                "apple-watch-series-10",
                "watch",
                "Apple Watch",
                "Mỏng nhất từng có. Màn hình lớn nhất cùng cảm biến đo ngưng thở khi ngủ.",
                10990000L,
                formatVND(10990000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-1-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Đen Bóng Jet Black", "#1c1c1e", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-1-202409?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Vàng Hồng Rose Gold", "#e8c9bd", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Bạc Silver", "#e3e4e5", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-3-202409?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of(),
                true,
                12,
                13
        ));

        // 14. AirPods 4 với Khử Tiếng Ồn
        allProducts.add(new CatalogProductDto(
                "prod-airpods4",
                "AirPods 4 với Tính Năng Khử Tiếng Ồn",
                "airpods-4-anc",
                "airpods",
                "AirPods",
                "Thiết kế vừa vặn được nâng cấp. Tính năng Khử tiếng ồn chủ động xuất sắc.",
                4799000L,
                formatVND(4799000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-4-anc-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Trắng", "#ffffff", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-4-anc-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of(),
                true,
                13,
                14
        ));

        // 15. AirPods Pro 2
        allProducts.add(new CatalogProductDto(
                "prod-airpodspro2",
                "AirPods Pro 2 (USB-C)",
                "airpods-pro-2",
                "airpods",
                "AirPods",
                "Chất lượng âm thanh Pro. Khử tiếng ồn vượt trội gấp 2 lần cùng chip H2.",
                6199000L,
                formatVND(6199000L),
                "Bán chạy",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-pro-2-hero-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Trắng", "#ffffff", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-pro-2-hero-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of(),
                true,
                15,
                15
        ));

        // 16. AirPods Max
        allProducts.add(new CatalogProductDto(
                "prod-airpodsmax",
                "AirPods Max",
                "airpods-max",
                "airpods",
                "AirPods",
                "Âm thanh độ trung thực cao. 5 màu sắc mới lộng lẫy và cổng USB-C tiện dụng.",
                13990000L,
                formatVND(13990000L),
                "Màu mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-max-select-202409-midnight?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Đen Midnight", "#21252d", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-max-select-202409-midnight?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Xanh Lam Blue", "#8798aa", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-max-select-202409-blue?wid=800&hei=600&fmt=jpeg&qlt=90"),
                        new ColorSwatchDto("Ánh Sao Starlight", "#ede7de", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-max-select-202409-starlight?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of(),
                true,
                16,
                16
        ));

        // 17. Củ sạc nhanh USB-C 30W
        allProducts.add(new CatalogProductDto(
                "prod-adapter30w",
                "Củ sạc nhanh USB-C 30W",
                "30w-usb-c-power-adapter",
                "phu-kien",
                "Phụ kiện",
                "Sạc nhanh an toàn cho iPhone, iPad và MacBook Air mọi lúc mọi nơi.",
                1090000L,
                formatVND(1090000L),
                null,
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MY1W2?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Trắng", "#ffffff", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MY1W2?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of(),
                true,
                17,
                17
        ));

        // 18. Bộ sạc MagSafe thế hệ mới (1m)
        allProducts.add(new CatalogProductDto(
                "prod-magsafe1m",
                "Bộ sạc MagSafe thế hệ mới (1m)",
                "magsafe-charger",
                "phu-kien",
                "Phụ kiện",
                "Tốc độ sạc không dây nhanh hơn lên đến 25W khi ghép nối với bộ tiếp hợp 30W.",
                1199000L,
                formatVND(1199000L),
                "Mới",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MX6X3?wid=800&hei=600&fmt=jpeg&qlt=90",
                List.of(
                        new ColorSwatchDto("Bạc", "#e3e4e5", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MX6X3?wid=800&hei=600&fmt=jpeg&qlt=90")
                ),
                List.of(),
                true,
                18,
                18
        ));
    }

    public List<CatalogProductDto> getAllProducts() {
        return Collections.unmodifiableList(allProducts);
    }

    public FilterOptionsDto getFilterOptions() {
        // Danh mục
        Map<String, String> catNames = Map.of(
                "iphone", "iPhone",
                "mac", "Mac",
                "ipad", "iPad",
                "watch", "Apple Watch",
                "airpods", "AirPods",
                "phu-kien", "Phụ kiện"
        );
        Map<String, String> catIcons = Map.of(
                "iphone", "bi-phone",
                "mac", "bi-laptop",
                "ipad", "bi-tablet",
                "watch", "bi-smartwatch",
                "airpods", "bi-headphones",
                "phu-kien", "bi-plug"
        );

        List<CategoryOptionDto> categories = new ArrayList<>();
        for (String catKey : List.of("iphone", "mac", "ipad", "watch", "airpods", "phu-kien")) {
            long count = allProducts.stream().filter(p -> p.category().equalsIgnoreCase(catKey)).count();
            List<CategoryOptionDto> subs = new ArrayList<>();
            switch (catKey) {
                case "iphone" -> {
                    long c16 = allProducts.stream().filter(p -> p.slug().startsWith("iphone-16")).count();
                    long c15 = allProducts.stream().filter(p -> p.slug().startsWith("iphone-15")).count();
                    subs.add(new CategoryOptionDto("iphone-16-series", "iPhone 16 Series", "bi-phone", (int) c16));
                    subs.add(new CategoryOptionDto("iphone-15-series", "iPhone 15 Series", "bi-phone", (int) c15));
                }
                case "mac" -> {
                    long cPro = allProducts.stream().filter(p -> p.slug().contains("macbook-pro")).count();
                    long cAir = allProducts.stream().filter(p -> p.slug().contains("macbook-air")).count();
                    subs.add(new CategoryOptionDto("macbook-pro", "MacBook Pro", "bi-laptop", (int) cPro));
                    subs.add(new CategoryOptionDto("macbook-air", "MacBook Air", "bi-laptop", (int) cAir));
                }
                case "ipad" -> {
                    long cPro = allProducts.stream().filter(p -> p.slug().contains("ipad-pro")).count();
                    long cAir = allProducts.stream().filter(p -> p.slug().contains("ipad-air")).count();
                    subs.add(new CategoryOptionDto("ipad-pro", "iPad Pro", "bi-tablet", (int) cPro));
                    subs.add(new CategoryOptionDto("ipad-air", "iPad Air", "bi-tablet", (int) cAir));
                }
                case "watch" -> {
                    long cUltra = allProducts.stream().filter(p -> p.slug().contains("ultra")).count();
                    long cS10 = allProducts.stream().filter(p -> p.slug().contains("series-10")).count();
                    subs.add(new CategoryOptionDto("apple-watch-ultra-2", "Apple Watch Ultra 2", "bi-smartwatch", (int) cUltra));
                    subs.add(new CategoryOptionDto("apple-watch-series-10", "Apple Watch Series 10", "bi-smartwatch", (int) cS10));
                }
                case "airpods" -> {
                    long cPro = allProducts.stream().filter(p -> p.slug().contains("pro")).count();
                    long cMax = allProducts.stream().filter(p -> p.slug().contains("max")).count();
                    subs.add(new CategoryOptionDto("airpods-pro-2", "AirPods Pro (Gen 2)", "bi-headphones", (int) cPro));
                    subs.add(new CategoryOptionDto("airpods-max-usbc", "AirPods Max", "bi-headphones", (int) cMax));
                }
                case "phu-kien" -> {
                    long cSac = allProducts.stream().filter(p -> p.slug().contains("sac") || p.slug().contains("magsafe")).count();
                    long cCase = allProducts.stream().filter(p -> p.slug().contains("op-lung")).count();
                    subs.add(new CategoryOptionDto("cu-sac-nhanh-apple-30w-usb-c", "Sạc & Cáp Nhanh", "bi-plug", (int) cSac));
                    subs.add(new CategoryOptionDto("op-lung-silicone-magsafe-iphone-16-pro", "Ốp lưng & Bao da", "bi-shield-check", (int) cCase));
                }
            }
            categories.add(new CategoryOptionDto(catKey, catNames.get(catKey), catIcons.get(catKey), (int) count, subs));
        }

        // Khoảng giá
        List<PriceRangeOptionDto> priceRanges = List.of(
                new PriceRangeOptionDto("all", "Tất cả mức giá", null, null),
                new PriceRangeOptionDto("under-15m", "Dưới 15 triệu", 0L, 15000000L),
                new PriceRangeOptionDto("15m-25m", "Từ 15 - 25 triệu", 15000000L, 25000000L),
                new PriceRangeOptionDto("25m-35m", "Từ 25 - 35 triệu", 25000000L, 35000000L),
                new PriceRangeOptionDto("above-35m", "Trên 35 triệu", 35000000L, 200000000L)
        );

        // Dung lượng lưu trữ
        List<String> storageList = List.of("64GB", "128GB", "256GB", "512GB", "1TB", "2TB");
        List<StorageOptionDto> storages = new ArrayList<>();
        for (String st : storageList) {
            long count = allProducts.stream().filter(p -> p.storages().contains(st)).count();
            storages.add(new StorageOptionDto(st, st, (int) count));
        }

        // Màu sắc
        Map<String, String> colorHexMap = new LinkedHashMap<>();
        colorHexMap.put("Titan Sa Mạc", "#c5b6a7");
        colorHexMap.put("Titan Tự Nhiên", "#9e9b94");
        colorHexMap.put("Titan Đen", "#3b3a3e");
        colorHexMap.put("Titan Trắng", "#f2f1ed");
        colorHexMap.put("Đen Không Gian", "#2e2f33");
        colorHexMap.put("Bạc", "#e3e4e5");
        colorHexMap.put("Trắng", "#ffffff");
        colorHexMap.put("Xanh Lam", "#6b96c3");
        colorHexMap.put("Hồng", "#e3a8b2");

        List<ColorOptionDto> colors = new ArrayList<>();
        for (Map.Entry<String, String> entry : colorHexMap.entrySet()) {
            String colName = entry.getKey();
            long count = allProducts.stream().filter(p -> p.colors().stream().anyMatch(c -> c.name().equalsIgnoreCase(colName))).count();
            colors.add(new ColorOptionDto(colName, entry.getValue(), (int) count));
        }

        return new FilterOptionsDto(categories, priceRanges, storages, colors);
    }

    public CatalogPageResult filterProducts(
            List<String> selectedCategories,
            Long minPrice,
            Long maxPrice,
            List<String> selectedStorages,
            List<String> selectedColors,
            String sort,
            int page,
            int pageSize
    ) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 12;

        List<CatalogProductDto> stream = new ArrayList<>(allProducts);

        // 1. Lọc Danh mục (OR trong nhóm)
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            Set<String> catSet = selectedCategories.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            stream.removeIf(p -> {
                String pCat = p.category().toLowerCase();
                String pSlug = p.slug().toLowerCase();
                return catSet.stream().noneMatch(c ->
                    pCat.equals(c) ||
                    pSlug.contains(c) ||
                    (c.equals("iphone-16-series") && pSlug.startsWith("iphone-16")) ||
                    (c.equals("iphone-15-series") && pSlug.startsWith("iphone-15")) ||
                    (c.equals("macbook-pro") && pSlug.contains("macbook-pro")) ||
                    (c.equals("macbook-air") && pSlug.contains("macbook-air")) ||
                    (c.equals("ipad-pro") && pSlug.contains("ipad-pro")) ||
                    (c.equals("ipad-air") && pSlug.contains("ipad-air")) ||
                    (c.contains("watch") && (pCat.contains("watch") || pSlug.contains("watch"))) ||
                    (c.contains("airpod") && (pCat.contains("airpod") || pSlug.contains("airpod"))) ||
                    (c.contains("phu-kien") && (pCat.contains("phu-kien") || pSlug.contains("sac") || pSlug.contains("magsafe") || pSlug.contains("op-lung"))) ||
                    (c.contains("sac") && (pSlug.contains("sac") || pSlug.contains("magsafe"))) ||
                    (c.contains("op-lung") && pSlug.contains("op-lung"))
                );
            });
        }

        // 2. Lọc Khoảng giá
        if (minPrice != null && minPrice > 0) {
            stream.removeIf(p -> p.startingPrice() < minPrice);
        }
        if (maxPrice != null && maxPrice > 0) {
            stream.removeIf(p -> p.startingPrice() > maxPrice);
        }

        // 3. Lọc Dung lượng (OR trong nhóm)
        if (selectedStorages != null && !selectedStorages.isEmpty()) {
            Set<String> stSet = new HashSet<>(selectedStorages);
            stream.removeIf(p -> p.storages().stream().noneMatch(stSet::contains));
        }

        // 4. Lọc Màu sắc (OR trong nhóm)
        if (selectedColors != null && !selectedColors.isEmpty()) {
            Set<String> colSet = selectedColors.stream()
                    .map(col -> col.toLowerCase())
                    .collect(Collectors.toSet());
            stream.removeIf(p -> p.colors().stream().noneMatch(c -> colSet.contains(c.name().toLowerCase())));
        }

        // 5. Sắp xếp (Sort)
        if (sort == null || sort.isBlank()) sort = "newest";
        switch (sort.toLowerCase()) {
            case "price-asc" -> stream.sort(Comparator.comparingLong(p -> p.startingPrice()));
            case "price-desc" -> stream.sort(Comparator.comparingLong((CatalogProductDto p) -> p.startingPrice()).reversed());
            case "popular" -> stream.sort(Comparator.comparingInt(p -> p.salesOrder()));
            case "newest" -> stream.sort(Comparator.comparingInt(p -> p.releaseOrder()));
            default -> stream.sort(Comparator.comparingInt(p -> p.releaseOrder()));
        }

        int totalItems = stream.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<CatalogProductDto> pagedProducts = (fromIndex < totalItems) ? stream.subList(fromIndex, toIndex) : List.of();

        // 6. Xây dựng danh sách Active Filter Chips
        List<ActiveFilterChipDto> activeChips = new ArrayList<>();
        Map<String, String> catMap = new HashMap<>();
        catMap.put("iphone", "iPhone");
        catMap.put("mac", "Mac");
        catMap.put("ipad", "iPad");
        catMap.put("watch", "Apple Watch");
        catMap.put("airpods", "AirPods");
        catMap.put("phu-kien", "Phụ kiện");
        catMap.put("iphone-16-series", "iPhone 16 Series");
        catMap.put("iphone-15-series", "iPhone 15 Series");
        catMap.put("macbook-pro", "MacBook Pro");
        catMap.put("macbook-air", "MacBook Air");
        catMap.put("ipad-pro", "iPad Pro");
        catMap.put("ipad-air", "iPad Air");
        catMap.put("apple-watch-ultra-2", "Apple Watch Ultra 2");
        catMap.put("apple-watch-series-10", "Apple Watch Series 10");
        catMap.put("airpods-pro-2", "AirPods Pro (Gen 2)");
        catMap.put("airpods-max-usbc", "AirPods Max");
        catMap.put("cu-sac-nhanh-apple-30w-usb-c", "Sạc & Cáp Nhanh");
        catMap.put("op-lung-silicone-magsafe-iphone-16-pro", "Ốp lưng Silicone MagSafe");
        if (selectedCategories != null) {
            for (String cat : selectedCategories) {
                activeChips.add(new ActiveFilterChipDto("category", catMap.getOrDefault(cat.toLowerCase(), cat), cat));
            }
        }
        if (minPrice != null && minPrice > 0 && (maxPrice == null || maxPrice <= 0)) {
            activeChips.add(new ActiveFilterChipDto("price", "Từ " + formatVND(minPrice), "min:" + minPrice));
        } else if (maxPrice != null && maxPrice > 0 && (minPrice == null || minPrice <= 0)) {
            activeChips.add(new ActiveFilterChipDto("price", "Dưới " + formatVND(maxPrice), "max:" + maxPrice));
        } else if (minPrice != null && minPrice > 0 && maxPrice != null && maxPrice > 0) {
            activeChips.add(new ActiveFilterChipDto("price", formatVND(minPrice) + " - " + formatVND(maxPrice), "range:" + minPrice + "-" + maxPrice));
        }

        if (selectedStorages != null) {
            for (String st : selectedStorages) {
                activeChips.add(new ActiveFilterChipDto("storage", st, st));
            }
        }

        if (selectedColors != null) {
            for (String col : selectedColors) {
                activeChips.add(new ActiveFilterChipDto("color", col, col));
            }
        }

        String activeCategoryTitle = "Tất cả sản phẩm Apple";
        if (selectedCategories != null && selectedCategories.size() == 1) {
            activeCategoryTitle = catMap.getOrDefault(selectedCategories.get(0).toLowerCase(), "Sản phẩm Apple");
        }

        return new CatalogPageResult(
                pagedProducts,
                totalItems,
                page,
                totalPages,
                pageSize,
                page > 1,
                page < totalPages,
                activeChips,
                activeCategoryTitle
        );
    }

    public static String formatVND(long amount) {
        return String.format(Locale.GERMAN, "%,d", amount) + "đ";
    }
}
