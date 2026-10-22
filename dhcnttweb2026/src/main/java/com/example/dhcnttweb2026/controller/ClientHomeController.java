package com.example.dhcnttweb2026.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientHomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "Trang Chủ");
        return "client/index";
    }
}
