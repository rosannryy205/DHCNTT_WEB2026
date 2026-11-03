package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/admin/login")
    public String showAdminLoginPage(Model model) {
        model.addAttribute("pageTitle", "Đăng nhập Quản trị & Nhân viên");
        return "admin/auth/login";
    }

    @PostMapping("/admin/login")
    public String handleAdminLogin(@RequestParam(name = "username", defaultValue = "") String email,
                                   @RequestParam(name = "password", defaultValue = "") String password,
                                   HttpServletRequest request, Model model) {
        try {
            var user = adminAuthService.authenticate(email, password);
            var session = request.getSession();
            request.changeSessionId();
            session.setAttribute("ADMIN_USER", user.getName() != null ? user.getName() : user.getEmail());
            session.setAttribute("ADMIN_ROLE", user.getRole());
            return "redirect:/admin";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("username", email);
            return "admin/auth/login";
        }
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("ADMIN_USER");
        session.removeAttribute("ADMIN_ROLE");
        session.removeAttribute("ORDER_CSRF");
        return "redirect:/admin/login";
    }
}
