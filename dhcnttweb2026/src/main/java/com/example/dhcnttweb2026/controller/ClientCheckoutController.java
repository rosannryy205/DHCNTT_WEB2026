package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.model.CheckoutFormDto;
import com.example.dhcnttweb2026.model.CheckoutItemDto;
import com.example.dhcnttweb2026.model.CheckoutSummaryDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class ClientCheckoutController {

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model) {
        // 1. Chuẩn bị danh sách sản phẩm mẫu đồng bộ từ túi hàng (Bag)
        List<CheckoutItemDto> checkoutItems = new ArrayList<>();

        long price1 = 31990000L;
        int qty1 = 1;
        checkoutItems.add(new CheckoutItemDto(
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
                price1 * qty1,
                formatVND(price1 * qty1),
                "/product/iphone-16-pro"
        ));

        long price2 = 1429000L;
        int qty2 = 1;
        checkoutItems.add(new CheckoutItemDto(
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
                price2 * qty2,
                formatVND(price2 * qty2),
                "/product/iphone-16-pro-silicone-case"
        ));

        // 2. Tính toán tổng tiền
        long subtotal = 0;
        int totalItemsCount = 0;
        for (CheckoutItemDto item : checkoutItems) {
            subtotal += item.lineTotal();
            totalItemsCount += item.quantity();
        }

        long shippingFee = 0; // Miễn phí giao hàng tiêu chuẩn chuẩn Apple
        long discount = 0;
        long total = subtotal + shippingFee - discount;

        CheckoutSummaryDto summary = new CheckoutSummaryDto(
                subtotal,
                formatVND(subtotal),
                shippingFee,
                "Miễn phí",
                discount,
                "0đ",
                total,
                formatVND(total),
                totalItemsCount
        );

        // 3. Khởi tạo CheckoutFormDto với dữ liệu mặc định gợi ý
        CheckoutFormDto form = new CheckoutFormDto();
        form.setRecipientName("Nguyễn Minh Quân");
        form.setEmail("quan.nguyen@gmail.com");
        form.setShippingPhone("0987654321");
        form.setProvince("Thành phố Hà Nội");
        form.setDistrict("Quận Hoàn Kiếm");
        form.setWard("Phường Tràng Tiền");
        form.setStreetAddress("Số 15 Lê Duẩn");
        form.setShippingAddress("Số 15 Lê Duẩn, Phường Tràng Tiền, Quận Hoàn Kiếm, Thành phố Hà Nội");
        form.setPaymentMethod("cod");
        form.setNote("Giao giờ hành chính, gọi trước khi giao 15 phút.");
        form.setTotalAmount(total);

        // 4. Truyền vào View
        model.addAttribute("pageTitle", "Thanh toán an toàn - IUH Store");
        model.addAttribute("checkoutItems", checkoutItems);
        model.addAttribute("summary", summary);
        model.addAttribute("checkoutForm", form);

        return "client/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@ModelAttribute("checkoutForm") CheckoutFormDto form) {
        // Mô phỏng tạo mã đơn hàng chuẩn database IUH Store: ORD-YYYYMMDD-XXXX
        String generatedOrderCode = "ORD-20261027-" + (1000 + (int)(Math.random() * 9000));
        return "redirect:/checkout/success?orderCode=" + generatedOrderCode;
    }

    @GetMapping("/checkout/success")
    public String showCheckoutSuccess(@RequestParam(value = "orderCode", defaultValue = "ORD-20261027-9045") String orderCode,
                                      Model model) {
        model.addAttribute("pageTitle", "Đặt hàng thành công - IUH Store");
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("estimatedDeliveryDate", "30 Tháng 10, 2026");
        model.addAttribute("recipientName", "Nguyễn Minh Quân");
        model.addAttribute("shippingPhone", "0987654321");
        model.addAttribute("shippingAddress", "Số 15 Lê Duẩn, Phường Tràng Tiền, Quận Hoàn Kiếm, Hà Nội");
        model.addAttribute("paymentMethodName", "Thanh toán khi nhận hàng (COD)");
        model.addAttribute("formattedTotal", "33.419.000đ");
        return "client/checkout-success";
    }

    private String formatVND(long amount) {
        return String.format(Locale.GERMAN, "%,d", amount) + "đ";
    }
}

