package com.example.dhcnttweb2026.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ClientAuthController {

    @GetMapping("/login")
    public String showCustomerLoginPage(Model model) {
        model.addAttribute("pageTitle", "Đăng nhập Khách hàng");
        return "client/auth/login";
    }

    @PostMapping("/login")
    public String handleCustomerLogin() {
        // Mock routing: theo yêu cầu tài khoản Customer chuyển hướng về trang chủ
        return "redirect:/";
    }

    @GetMapping("/register")
    public String showCustomerRegisterPage(Model model) {
        model.addAttribute("pageTitle", "Đăng ký Tài khoản DH Store");
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String handleCustomerRegister() {
        // Mock routing: đăng ký thành công chuyển hướng về trang chủ
        return "redirect:/";
    }
}
