package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerLoginDto;
import com.example.dhcnttweb2026.dto.CustomerRegisterDto;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.service.CustomerAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    public CustomerAuthController(CustomerAuthService customerAuthService) {
        this.customerAuthService = customerAuthService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String returnUrl,
                                HttpSession session,
                                Model model) {
        if (session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY) != null) {
            return "redirect:" + sanitizeReturnUrl(returnUrl);
        }

        // Kiểm tra xem có thông báo chặn truy cập từ Interceptor không
        Object authWarning = session.getAttribute("authWarning");
        if (authWarning != null) {
            model.addAttribute("warningMessage", authWarning.toString());
            session.removeAttribute("authWarning");
        }

        model.addAttribute("loginDto", new CustomerLoginDto());
        model.addAttribute("returnUrl", returnUrl);
        return "client/auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute CustomerLoginDto loginDto,
                               @RequestParam(value = "username", required = false) String username,
                               @RequestParam(required = false) String returnUrl,
                               HttpSession session,
                               Model model,
                               RedirectAttributes ra) {
        // Hỗ trợ cả trường username hoặc email từ form
        if ((loginDto.getEmail() == null || loginDto.getEmail().isBlank()) && username != null && !username.isBlank()) {
            loginDto.setEmail(username);
        }

        // Mock compatibility fallback nếu form rỗng hoàn toàn
        if ((loginDto.getEmail() == null || loginDto.getEmail().isBlank()) &&
            (loginDto.getPassword() == null || loginDto.getPassword().isBlank())) {
            return "redirect:/";
        }

        try {
            CustomerSessionDto customer = customerAuthService.login(loginDto);
            session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY, customer);
            ra.addFlashAttribute("successMessage", "Đăng nhập thành công! Chào mừng " + customer.getName() + " quay trở lại IUH Store.");
            return "redirect:" + sanitizeReturnUrl(returnUrl);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("loginDto", loginDto);
            model.addAttribute("returnUrl", returnUrl);
            return "client/auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(@RequestParam(required = false) String returnUrl,
                                   HttpSession session,
                                   Model model) {
        if (session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY) != null) {
            return "redirect:" + sanitizeReturnUrl(returnUrl);
        }

        model.addAttribute("registerDto", new CustomerRegisterDto());
        model.addAttribute("returnUrl", returnUrl);
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute CustomerRegisterDto registerDto,
                                  @RequestParam(required = false) String returnUrl,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes ra) {
        // Mock compatibility fallback nếu form rỗng hoàn toàn
        if ((registerDto.getEmail() == null || registerDto.getEmail().isBlank()) &&
            (registerDto.getName() == null || registerDto.getName().isBlank())) {
            return "redirect:/";
        }

        try {
            // Đăng ký và tự động đăng nhập (Auto-login)
            CustomerSessionDto customer = customerAuthService.register(registerDto);
            session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY, customer);
            ra.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Chào mừng " + customer.getName() + " đến với IUH Store.");
            return "redirect:" + sanitizeReturnUrl(returnUrl);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            // Xóa mật khẩu khi render lại form để đảm bảo an toàn
            registerDto.setPassword("");
            registerDto.setConfirmPassword("");
            model.addAttribute("registerDto", registerDto);
            model.addAttribute("returnUrl", returnUrl);
            return "client/auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY);
        ra.addFlashAttribute("successMessage", "Bạn đã đăng xuất tài khoản IUH Store thành công.");
        return "redirect:/";
    }

    /**
     * Ngăn chặn Open Redirect Vulnerability: chỉ cho phép URL nội bộ hợp lệ
     */
    private String sanitizeReturnUrl(String url) {
        if (url != null && url.startsWith("/") && !url.startsWith("//") && !url.contains("\\")) {
            return url;
        }
        return "/";
    }
}
