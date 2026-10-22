package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.service.ProductCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ClientProductController {

    private final ProductCatalogService catalogService;

    public ClientProductController(ProductCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public record ProductVariantDto(
            String sku,
            String colorName,
            String colorHex,
            String storageCapacity,
            long price,
            String formattedPrice,
            long originalPrice,
            String formattedOriginalPrice,
            int stockQuantity,
            List<String> images
    ) {}

    public record ProductColorDto(
            String name,
            String hex,
            String previewImage
    ) {}

    public record ProductStorageDto(
            String capacity,
            long startingPrice,
            String formattedStartingPrice
    ) {}

    public record InTheBoxItemDto(
            String name,
            String imageUrl,
            String description
    ) {}

    public record TechSpecGroupDto(
            String groupName,
            Map<String, String> specs
    ) {}

    public record CardItemDto(
            String name,
            String category,
            String imageUrl,
            String priceText,
            String linkUrl
    ) {}

    @GetMapping("/category/{category}")
    public String categoryRedirect(@PathVariable("category") String category) {
        return "redirect:/products?category=" + category;
    }

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(value = "category", required = false) List<String> categories,
            @RequestParam(value = "minPrice", required = false) Long minPrice,
            @RequestParam(value = "maxPrice", required = false) Long maxPrice,
            @RequestParam(value = "storage", required = false) List<String> storages,
            @RequestParam(value = "color", required = false) List<String> colors,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        ProductCatalogService.CatalogPageResult catalogResult = catalogService.filterProducts(
                categories, minPrice, maxPrice, storages, colors, sort, page, 12
        );

        model.addAttribute("pageTitle", "Cửa hàng sản phẩm Apple - DH Store");
        model.addAttribute("isProductsCatalogPage", true);
        model.addAttribute("filterOptions", catalogService.getFilterOptions());
        model.addAttribute("catalogResult", catalogResult);
        model.addAttribute("selectedCategories", categories != null ? categories : Collections.emptyList());
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedStorages", storages != null ? storages : Collections.emptyList());
        model.addAttribute("selectedColors", colors != null ? colors : Collections.emptyList());
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);

        return "client/products";
    }

    @GetMapping("/products/filter")
    public String filterProductsFragment(
            @RequestParam(value = "category", required = false) List<String> categories,
            @RequestParam(value = "minPrice", required = false) Long minPrice,
            @RequestParam(value = "maxPrice", required = false) Long maxPrice,
            @RequestParam(value = "storage", required = false) List<String> storages,
            @RequestParam(value = "color", required = false) List<String> colors,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        ProductCatalogService.CatalogPageResult catalogResult = catalogService.filterProducts(
                categories, minPrice, maxPrice, storages, colors, sort, page, 12
        );

        model.addAttribute("catalogResult", catalogResult);
        model.addAttribute("currentSort", sort);

        return "client/fragments/product-grid :: product-grid";
    }

    @GetMapping("/product/{slug}")
    public String productDetail(@PathVariable("slug") String slug, Model model) {
        // Mặc định hỗ trợ hiển thị chi tiết iPhone 16 Pro với đầy đủ dữ liệu cấu hình
        String productName = "iPhone 16 Pro";
        String tagline = "Được tiện từ titan. Sức mạnh bứt phá cùng chip A18 Pro.";

        // 1. Danh sách màu sắc
        List<ProductColorDto> colors = List.of(
                new ProductColorDto("Titan Sa Mạc", "#c5b6a7",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90"),
                new ProductColorDto("Titan Tự Nhiên", "#9e9b94",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90"),
                new ProductColorDto("Titan Trắng", "#f2f1ed",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-whitetitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90"),
                new ProductColorDto("Titan Đen", "#3b3a3e",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-blacktitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90")
        );

        // 2. Danh sách tùy chọn dung lượng
        List<ProductStorageDto> storages = List.of(
                new ProductStorageDto("128GB", 28990000L, formatVND(28990000L)),
                new ProductStorageDto("256GB", 31990000L, formatVND(31990000L)),
                new ProductStorageDto("512GB", 37990000L, formatVND(37990000L)),
                new ProductStorageDto("1TB", 43990000L, formatVND(43990000L))
        );

        // 3. Toàn bộ các biến thể chi tiết (SKUs)
        List<ProductVariantDto> variants = new ArrayList<>();
        Map<String, String> colorImages = Map.of(
                "Titan Sa Mạc", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                "Titan Tự Nhiên", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90",
                "Titan Trắng", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-whitetitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90",
                "Titan Đen", "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-blacktitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90"
        );

        for (ProductColorDto color : colors) {
            for (ProductStorageDto storage : storages) {
                long price = storage.startingPrice();
                long originalPrice = price + 2000000L;
                String sku = "IP16P-" + storage.capacity() + "-" + color.name().replace(" ", "").toUpperCase();

                List<String> gallery = List.of(
                        colorImages.get(color.name()),
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-finish-select-202409-6-3inch?wid=800&hei=600&fmt=jpeg&qlt=90",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-camera-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90"
                );

                variants.add(new ProductVariantDto(
                        sku,
                        color.name(),
                        color.hex(),
                        storage.capacity(),
                        price,
                        formatVND(price),
                        originalPrice,
                        formatVND(originalPrice),
                        25,
                        gallery
                ));
            }
        }

        // 4. Trong hộp có gì (What's in the Box)
        List<InTheBoxItemDto> inTheBox = List.of(
                new InTheBoxItemDto("iPhone 16 Pro với iOS 18",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=300&hei=300&fmt=jpeg&qlt=90",
                        "Thiết bị chính"),
                new InTheBoxItemDto("Cáp sạc USB-C (1m)",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MQKJ3?wid=300&hei=300&fmt=jpeg&qlt=90",
                        "Thiết kế sợi bện dù chống gập gãy"),
                new InTheBoxItemDto("Tài liệu hướng dẫn & Que lấy SIM",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-box-cable-202409?wid=300&hei=300&fmt=jpeg&qlt=90",
                        "Tài liệu thân thiện môi trường")
        );

        // 5. Thông số kỹ thuật (Tech Specs)
        List<TechSpecGroupDto> techSpecs = List.of(
                new TechSpecGroupDto("Màn hình & Thiết kế", Map.of(
                        "Kích thước", "Super Retina XDR 6.3 inch",
                        "Công nghệ", "OLED toàn màn hình, ProMotion 120Hz, Dynamic Island",
                        "Độ phân giải", "2622 x 1206 pixel với mật độ điểm ảnh 460 ppi",
                        "Chất liệu khung", "Titan Cấp 5 chuẩn hàng không vũ trụ với mặt lưng kính nhám"
                )),
                new TechSpecGroupDto("Hiệu năng & Vi xử lý", Map.of(
                        "Chipset", "Apple A18 Pro (tiến trình 3nm thế hệ thứ 2)",
                        "CPU", "6 lõi mới (2 lõi hiệu năng cao + 4 lõi tiết kiệm điện)",
                        "GPU", "6 lõi mới với công nghệ dò tia phần cứng (Ray Tracing)",
                        "Neural Engine", "16 lõi mới hỗ trợ xử lý Apple Intelligence trên thiết bị"
                )),
                new TechSpecGroupDto("Camera & Quay phim", Map.of(
                        "Camera chính", "Fusion 48MP, khẩu độ f/1.78, OIS dịch chuyển cảm biến thế hệ 2",
                        "Camera góc siêu rộng", "48MP Ultra Wide với góc chụp 120°, lấy nét tự động Macro",
                        "Camera Telephoto", "12MP Telephoto 5x zoom quang học (120mm)",
                        "Quay video", "4K Dolby Vision ở tốc độ 120 fps (chuẩn điện ảnh)"
                )),
                new TechSpecGroupDto("Pin & Cổng kết nối", Map.of(
                        "Thời lượng xem video", "Lên đến 27 giờ",
                        "Cổng kết nối", "USB-C chuẩn USB 3 (tốc độ truyền dữ liệu lên đến 10Gb/s)",
                        "Sạc không dây", "MagSafe lên đến 25W với bộ tiếp hợp nguồn 30W"
                ))
        );

        // 6. Phụ kiện tương thích (Compatible Accessories)
        List<CardItemDto> compatibleAccessories = List.of(
                new CardItemDto(
                        "Ốp lưng Silicon MagSafe cho iPhone 16 Pro",
                        "Phụ kiện chính hãng",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MA7E4?wid=400&hei=400&fmt=jpeg&qlt=90",
                        "1.429.000đ",
                        "/product/iphone-16-pro-silicone-case"
                ),
                new CardItemDto(
                        "Bộ sạc MagSafe thế hệ mới (1m)",
                        "Sạc không dây",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MX6X3?wid=400&hei=400&fmt=jpeg&qlt=90",
                        "1.199.000đ",
                        "/product/magsafe-charger"
                ),
                new CardItemDto(
                        "Củ sạc nhanh USB-C 30W",
                        "Nguồn điện",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MY1W2?wid=400&hei=400&fmt=jpeg&qlt=90",
                        "1.090.000đ",
                        "/product/30w-usb-c-power-adapter"
                ),
                new CardItemDto(
                        "AirPods 4 với Tính Năng Chủ Động Khử Tiếng Ồn",
                        "Âm thanh",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-4-anc-select-202409?wid=400&hei=400&fmt=jpeg&qlt=90",
                        "4.799.000đ",
                        "/product/airpods-4-anc"
                )
        );

        // 7. Sản phẩm liên quan (Related Products)
        List<CardItemDto> relatedProducts = List.of(
                new CardItemDto(
                        "iPhone 16",
                        "iPhone",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-ultramarine?wid=400&hei=400&fmt=jpeg&qlt=90",
                        "Từ 22.999.000đ",
                        "/product/iphone-16"
                ),
                new CardItemDto(
                        "iPhone 16 Pro Max",
                        "iPhone",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=400&hei=600&fmt=jpeg&qlt=90",
                        "Từ 34.999.000đ",
                        "/product/iphone-16-pro-max"
                ),
                new CardItemDto(
                        "Apple Watch Series 10",
                        "Apple Watch",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-1-202409?wid=400&hei=400&fmt=jpeg&qlt=90",
                        "Từ 10.999.000đ",
                        "/product/apple-watch-series-10"
                ),
                new CardItemDto(
                        "MacBook Pro 14\" M4",
                        "Mac",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-spaceblack-select-202410?wid=400&hei=400&fmt=jpeg&qlt=90",
                        "Từ 39.999.000đ",
                        "/product/macbook-pro-14-m4"
                )
        );

        // Đẩy toàn bộ dữ liệu vào Model
        model.addAttribute("pageTitle", "Mua " + productName);
        model.addAttribute("isProductDetailPage", true);
        model.addAttribute("productName", productName);
        model.addAttribute("tagline", tagline);
        model.addAttribute("startingPriceFormatted", formatVND(28990000L));
        model.addAttribute("colors", colors);
        model.addAttribute("storages", storages);
        model.addAttribute("inTheBox", inTheBox);
        model.addAttribute("techSpecs", techSpecs);
        model.addAttribute("compatibleAccessories", compatibleAccessories);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("variants", variants);

        return "client/product-detail";
    }

    private String formatVND(long amount) {
        return String.format(Locale.GERMAN, "%,d", amount) + "đ";
    }
}
