package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.service.BagService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class ClientBagController {

    private final BagService bagService;

    public ClientBagController(BagService bagService) {
        this.bagService = bagService;
    }

    public record BagItemDto(
            Long id,
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
            Long id,
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
        return "redirect:/bag";
    }

    @GetMapping("/bag")
    public String showBag(HttpServletRequest request, HttpServletResponse response, Model model) {
        String guestToken = resolveGuestToken(request, response);
        Long userId = resolveUserId(request);

        BagService.BagViewDto bagView = bagService.getBagView(guestToken, userId);

        model.addAttribute("pageTitle", "Túi hàng của bạn - IUH Store");
        model.addAttribute("bagItems", bagView.items());
        model.addAttribute("subtotal", bagView.subtotal());
        model.addAttribute("formattedSubtotal", bagView.formattedSubtotal());
        model.addAttribute("shippingFee", bagView.shippingFee());
        model.addAttribute("formattedShippingFee", bagView.formattedShippingFee());
        model.addAttribute("total", bagView.total());
        model.addAttribute("formattedTotal", bagView.formattedTotal());
        model.addAttribute("totalItemsCount", bagView.totalItemsCount());
        model.addAttribute("compatibleAccessories", bagView.compatibleAccessories());
        model.addAttribute("isBagEmpty", bagView.isBagEmpty());

        return "client/bag";
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
