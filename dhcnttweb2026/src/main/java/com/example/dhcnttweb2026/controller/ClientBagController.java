package com.example.dhcnttweb2026.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class ClientBagController {

    public record BagItemDto(
            String id,
            String sku,
            String productName,
            String colorName,
            String colorHex,
            String storageCapacity,
            String imageUrl,
            long unitPrice,
            String formattedUnitPrice,
            int quantity,
            int stockQuantity,
            long lineTotal,
            String formattedLineTotal,
            String productDetailUrl
    ) {}

    public record AccessoryUpsellDto(
            String id,
            String sku,
            String name,
            String category,
            String imageUrl,
            long price,
            String formattedPrice,
            String productDetailUrl
    ) {}

    @GetMapping("/cart")
    public String cartRedirect() {
        // Chuyển hướng /cart về /bag để đảm bảo tính đồng bộ theo GLOSSARY.md
        return "redirect:/bag";
    }

    @GetMapping("/bag")
    public String showBag(Model model) {
        // Dữ liệu mẫu khởi tạo cho 2 sản phẩm theo thỏa thuận Vòng 4
        List<BagItemDto> bagItems = new ArrayList<>();

        // 1. iPhone 16 Pro 256GB - Titan Sa Mạc
        long price1 = 31990000L;
        int qty1 = 1;
        bagItems.add(new BagItemDto(
                "item-1",
                "IP16P-256GB-TITANSAMAC",
                "iPhone 16 Pro 256GB",
                "Titan Sa Mạc",
                "#c5b6a7",
                "256GB",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90",
                price1,
                formatVND(price1),
                qty1,
                25,
                price1 * qty1,
                formatVND(price1 * qty1),
                "/product/iphone-16-pro"
        ));

        // 2. Ốp lưng Silicon MagSafe cho iPhone 16 Pro
        long price2 = 1429000L;
        int qty2 = 1;
        bagItems.add(new BagItemDto(
                "item-2",
                "ACC-CASE-IP16P-SILICONE",
                "Ốp lưng Silicon MagSafe cho iPhone 16 Pro",
                "Xanh Denim",
                "#4a5e6d",
                "Tiêu chuẩn",
                "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MA7E4?wid=400&hei=400&fmt=jpeg&qlt=90",
                price2,
                formatVND(price2),
                qty2,
                50,
                price2 * qty2,
                formatVND(price2 * qty2),
                "/product/iphone-16-pro-silicone-case"
        ));

        // Tính toán tổng tiền
        long subtotal = 0;
        int totalItemsCount = 0;
        for (BagItemDto item : bagItems) {
            subtotal += item.lineTotal();
            totalItemsCount += item.quantity();
        }
        long shippingFee = 0; // Miễn phí giao hàng tiêu chuẩn
        long total = subtotal + shippingFee;

        // Danh sách Phụ kiện tương thích gợi ý (Compatible Accessories)
        List<AccessoryUpsellDto> compatibleAccessories = List.of(
                new AccessoryUpsellDto(
                        "acc-1",
                        "ACC-CHARGER-30W",
                        "Củ sạc nhanh USB-C 30W",
                        "Nguồn điện",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MY1W2?wid=400&hei=400&fmt=jpeg&qlt=90",
                        1090000L,
                        formatVND(1090000L),
                        "/product/30w-usb-c-power-adapter"
                ),
                new AccessoryUpsellDto(
                        "acc-2",
                        "ACC-MAGSAFE-1M",
                        "Bộ sạc MagSafe thế hệ mới (1m)",
                        "Sạc không dây",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MX6X3?wid=400&hei=400&fmt=jpeg&qlt=90",
                        1199000L,
                        formatVND(1199000L),
                        "/product/magsafe-charger"
                ),
                new AccessoryUpsellDto(
                        "acc-3",
                        "ACC-AIRPODS-4-ANC",
                        "AirPods 4 với Tính Năng Khử Tiếng Ồn",
                        "Âm thanh",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-4-anc-select-202409?wid=400&hei=400&fmt=jpeg&qlt=90",
                        4799000L,
                        formatVND(4799000L),
                        "/product/airpods-4-anc"
                ),
                new AccessoryUpsellDto(
                        "acc-4",
                        "ACC-CABLE-USBC-1M",
                        "Cáp sạc USB-C dệt dù chống rối (1m)",
                        "Cáp kết nối",
                        "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MQKJ3?wid=400&hei=400&fmt=jpeg&qlt=90",
                        539000L,
                        formatVND(539000L),
                        "/product/usb-c-woven-cable"
                )
        );

        // Truyền dữ liệu vào Model
        model.addAttribute("pageTitle", "Túi hàng của bạn");
        model.addAttribute("bagItems", bagItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("formattedSubtotal", formatVND(subtotal));
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("formattedShippingFee", "Miễn phí");
        model.addAttribute("total", total);
        model.addAttribute("formattedTotal", formatVND(total));
        model.addAttribute("totalItemsCount", totalItemsCount);
        model.addAttribute("compatibleAccessories", compatibleAccessories);
        model.addAttribute("isBagEmpty", bagItems.isEmpty());

        return "client/bag";
    }

    private String formatVND(long amount) {
        return String.format(Locale.GERMAN, "%,d", amount) + "đ";
    }
}
