package com.example.dhcnttweb2026.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminAuthController {

    @GetMapping("/admin/login")
    public String showAdminLoginPage(Model model) {
        model.addAttribute("pageTitle", "Đăng nhập Quản trị & Nhân viên");
        return "admin/auth/login";
    }

    @PostMapping("/admin/login")
    public String handleAdminLogin() {
        // Mock routing: theo yêu cầu role admin hoặc staff chuyển hướng sang trang admin
        return "redirect:/admin";
    }
}
