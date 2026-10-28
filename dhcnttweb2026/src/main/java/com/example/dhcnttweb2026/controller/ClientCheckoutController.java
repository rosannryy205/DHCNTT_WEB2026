package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.model.CheckoutFormDto;
import com.example.dhcnttweb2026.model.CheckoutItemDto;
import com.example.dhcnttweb2026.model.CheckoutSummaryDto;
import com.example.dhcnttweb2026.model.OrderDetailDto;
import com.example.dhcnttweb2026.repository.UserRepository;
import com.example.dhcnttweb2026.service.BagService;
import com.example.dhcnttweb2026.service.ClientStorefrontService;
import com.example.dhcnttweb2026.service.OrderService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@SuppressWarnings("null")
public class ClientCheckoutController {

    private final OrderService orderService;
    private final BagService bagService;
    private final UserRepository userRepository;

    public ClientCheckoutController(OrderService orderService,
                                    BagService bagService,
                                    UserRepository userRepository) {
        this.orderService = orderService;
        this.bagService = bagService;
        this.userRepository = userRepository;
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Model model,
                                   RedirectAttributes ra) {
        String guestToken = resolveGuestToken(request, response);
        Long userId = resolveUserId(request);

        // 1. Lấy giỏ hàng thực tế từ BagService
        BagService.BagViewDto bagView = bagService.getBagView(guestToken, userId);

        // Nếu giỏ hàng trống, điều hướng về /bag với thông báo
        if (bagView == null || bagView.isBagEmpty() || bagView.items() == null || bagView.items().isEmpty()) {
            ra.addFlashAttribute("warningMessage", "Túi hàng của bạn đang trống. Vui lòng chọn sản phẩm trước khi thanh toán.");
            return "redirect:/bag";
        }

        // 2. Lấy danh sách CheckoutItemDto thực tế từ OrderService
        List<CheckoutItemDto> checkoutItems = orderService.getCheckoutItems(guestToken, userId);
        if (checkoutItems.isEmpty()) {
            ra.addFlashAttribute("warningMessage", "Túi hàng của bạn đang trống. Vui lòng chọn sản phẩm trước khi thanh toán.");
            return "redirect:/bag";
        }

        // 3. Chuẩn bị thông tin tóm tắt đơn hàng
        long shippingFee = 0L; // Miễn phí giao hàng tiêu chuẩn Apple
        long discount = 0L;
        long total = bagView.subtotal() + shippingFee - discount;

        CheckoutSummaryDto summary = new CheckoutSummaryDto(
                bagView.subtotal(),
                bagView.formattedSubtotal(),
                shippingFee,
                "Miễn phí",
                discount,
                "0đ",
                total,
                ClientStorefrontService.formatVND(total),
                bagView.totalItemsCount()
        );

        // 4. Khởi tạo CheckoutFormDto tự động điền từ thông tin tài khoản người dùng đăng nhập
        CheckoutFormDto form = new CheckoutFormDto();
        form.setPaymentMethod("cod");
        form.setShippingMethod("standard");
        form.setTotalAmount(total);

        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getName() != null && !user.getName().isBlank()) {
                    form.setRecipientName(user.getName());
                }
                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                    form.setEmail(user.getEmail());
                }
                if (user.getPhone() != null && !user.getPhone().isBlank()) {
                    form.setShippingPhone(user.getPhone());
                }
                if (user.getAddress() != null && !user.getAddress().isBlank()) {
                    form.setShippingAddress(user.getAddress());
                    form.setStreetAddress(user.getAddress());
                }
            }
        }

        // Giá trị địa chỉ mặc định nếu người dùng chưa có
        if (form.getProvince() == null || form.getProvince().isBlank()) {
            form.setProvince("Thành phố Hà Nội");
        }
        if (form.getDistrict() == null || form.getDistrict().isBlank()) {
            form.setDistrict("Quận Hoàn Kiếm");
        }
        if (form.getWard() == null || form.getWard().isBlank()) {
            form.setWard("Phường Tràng Tiền");
        }

        // 5. Đưa dữ liệu thực vào Model
        model.addAttribute("pageTitle", "Thanh toán an toàn - IUH Store");
        model.addAttribute("checkoutItems", checkoutItems);
        model.addAttribute("summary", summary);
        model.addAttribute("checkoutForm", form);

        return "client/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@ModelAttribute("checkoutForm") CheckoutFormDto form,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  RedirectAttributes ra) {
        String guestToken = resolveGuestToken(request, response);
        Long userId = resolveUserId(request);

        try {
            // Thực hiện lưu đơn hàng và các order_items vào CSDL
            Order order = orderService.placeOrder(userId, guestToken, form);

            // Điều hướng người dùng về đúng trang hoàn thành với mã đơn hàng
            return "redirect:/checkout/success?orderCode=" + order.getOrderCode();
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bag";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình thanh toán: " + ex.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/checkout/success")
    public String showCheckoutSuccess(@RequestParam(value = "orderCode", required = false) String orderCode,
                                      HttpServletRequest request,
                                      Model model) {
        if (orderCode == null || orderCode.isBlank()) {
            return "redirect:/";
        }

        // 1. Tìm đơn hàng thực tế trong CSDL thông qua DTO an toàn không lo LazyInitializationException
        Optional<OrderDetailDto> detailOpt = orderService.getOrderDetailByCode(orderCode);
        if (detailOpt.isEmpty()) {
            return "redirect:/";
        }

        OrderDetailDto detail = detailOpt.get();

        // 2. Truyền toàn bộ dữ liệu chính xác sang trang hoàn tất
        model.addAttribute("pageTitle", "Đặt hàng thành công - IUH Store");
        model.addAttribute("orderDetail", detail);
        model.addAttribute("orderCode", detail.orderCode());
        model.addAttribute("orderDate", detail.orderDate());
        model.addAttribute("estimatedDeliveryDate", detail.estimatedDeliveryDate());
        model.addAttribute("recipientName", detail.recipientName());
        model.addAttribute("shippingPhone", detail.shippingPhone());
        model.addAttribute("shippingAddress", detail.shippingAddress());
        model.addAttribute("note", detail.note());
        model.addAttribute("paymentMethodName", detail.paymentMethodName());
        model.addAttribute("paymentStatus", detail.paymentStatus());
        model.addAttribute("orderStatus", detail.orderStatus());
        model.addAttribute("formattedTotal", detail.formattedTotal());
        model.addAttribute("orderItems", detail.items());

        return "client/checkout-success";
    }

    private String resolveGuestToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("guest_token".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        String newToken = UUID.randomUUID().toString();
        Cookie cookie = new Cookie("guest_token", newToken);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 3600);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return newToken;
    }

    private Long resolveUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY);
            if (userObj instanceof CustomerSessionDto dto) {
                return dto.getId();
            }
            if (userObj instanceof User u) {
                return u.getId();
            }
            Object userId = session.getAttribute("userId");
            if (userId instanceof Long l) {
                return l;
            }
        }
        return null;
    }
}
