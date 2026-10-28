package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.service.BagService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bag")
public class BagApiController {

    private final BagService bagService;

    public BagApiController(BagService bagService) {
        this.bagService = bagService;
    }

    public record AddToBagRequest(Long variantId, String sku, Integer quantity) {}
    public record UpdateBagRequest(Long cartItemId, Integer quantity) {}
    public record RemoveBagRequest(Long cartItemId) {}

    @PostMapping("/add")
    public ResponseEntity<?> addToBag(@RequestBody AddToBagRequest req,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        Long userId = resolveUserId(request);
        if (userId == null) {
            String returnUrl = "/";
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank()) {
                try {
                    URI uri = URI.create(referer);
                    String path = uri.getPath();
                    String query = uri.getQuery();
                    returnUrl = (query != null && !query.isBlank()) ? path + "?" + query : path;
                } catch (Exception ignored) {
                    returnUrl = "/";
                }
            }
            if (returnUrl == null || returnUrl.isBlank()) {
                returnUrl = "/";
            }
            String encodedReturnUrl = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
            String message = "Vui lòng đăng nhập tài khoản IUH Store để thêm sản phẩm vào túi hàng.";

            // Lưu authWarning vào session để trang login tự động hiển thị flash alert
            request.getSession(true).setAttribute("authWarning", message);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "requireLogin", true,
                    "redirectUrl", "/login?returnUrl=" + encodedReturnUrl,
                    "message", message
            ));
        }

        String guestToken = resolveGuestToken(request, response);
        int qty = (req.quantity() != null && req.quantity() > 0) ? req.quantity() : 1;
        var result = bagService.addToBag(guestToken, userId, req.variantId(), req.sku(), qty);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateBag(@RequestBody UpdateBagRequest req) {
        if (req.cartItemId() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "cartItemId is required"));
        }
        int qty = (req.quantity() != null) ? req.quantity() : 1;
        var result = bagService.updateQuantity(req.cartItemId(), qty);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeBag(@RequestBody RemoveBagRequest req) {
        if (req.cartItemId() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "cartItemId is required"));
        }
        var result = bagService.removeItem(req.cartItemId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCount(HttpServletRequest request, HttpServletResponse response) {
        String guestToken = resolveGuestToken(request, response);
        Long userId = resolveUserId(request);
        int count = bagService.getTotalItemCount(guestToken, userId);
        return ResponseEntity.ok(Map.of("count", count));
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
