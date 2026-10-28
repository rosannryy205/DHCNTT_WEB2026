package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.service.ClientStorefrontService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientHomeController {

    private final ClientStorefrontService storefrontService;

    public ClientHomeController(ClientStorefrontService storefrontService) {
        this.storefrontService = storefrontService;
    }

    @GetMapping({"/", "/home"})
    public String home(@NonNull Model model) {
        model.addAttribute("pageTitle", "IUH Store - Cửa hàng Apple chính hãng");
        model.addAttribute("heroProduct", storefrontService.getHeroProduct());
        model.addAttribute("darkProduct", storefrontService.getDarkTileProduct());
        model.addAttribute("parchmentProduct", storefrontService.getParchmentTileProduct());
        model.addAttribute("featuredProducts", storefrontService.getFeaturedProducts(8));
        return "client/index";
    }
}
