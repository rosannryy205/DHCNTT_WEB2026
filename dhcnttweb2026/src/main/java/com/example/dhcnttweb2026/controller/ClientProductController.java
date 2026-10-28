package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.service.ClientStorefrontService;
import com.example.dhcnttweb2026.service.ProductCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ClientProductController {

    private final ProductCatalogService catalogService;
    private final ClientStorefrontService storefrontService;

    public ClientProductController(ProductCatalogService catalogService, ClientStorefrontService storefrontService) {
        this.catalogService = catalogService;
        this.storefrontService = storefrontService;
    }

    public record ProductVariantDto(
            Long id,
            String sku,
            String colorName,
            String colorHex,
            String storageCapacity,
            long price,
            String formattedPrice,
            long originalPrice,
            String formattedOriginalPrice,
            int stockQuantity,
            List<String> images
    ) {}

    public record ProductColorDto(
            String name,
            String hex,
            String previewImage
    ) {}

    public record ProductStorageDto(
            String capacity,
            long startingPrice,
            String formattedStartingPrice
    ) {}

    public record InTheBoxItemDto(
            String name,
            String imageUrl,
            String description
    ) {}

    public record TechSpecGroupDto(
            String groupName,
            Map<String, String> specs
    ) {}

    public record CardItemDto(
            String name,
            String category,
            String imageUrl,
            String priceText,
            String linkUrl
    ) {}

    @GetMapping("/category/{category}")
    public String categoryRedirect(@PathVariable("category") String category) {
        return "redirect:/products?category=" + category;
    }

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(value = "category", required = false) List<String> categories,
            @RequestParam(value = "minPrice", required = false) Long minPrice,
            @RequestParam(value = "maxPrice", required = false) Long maxPrice,
            @RequestParam(value = "storage", required = false) List<String> storages,
            @RequestParam(value = "color", required = false) List<String> colors,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        ProductCatalogService.CatalogPageResult catalogResult = catalogService.filterProducts(
                categories, minPrice, maxPrice, storages, colors, sort, page, 12
        );

        model.addAttribute("pageTitle", "Cửa hàng sản phẩm Apple - DH Store");
        model.addAttribute("isProductsCatalogPage", true);
        model.addAttribute("filterOptions", catalogService.getFilterOptions());
        model.addAttribute("catalogResult", catalogResult);
        model.addAttribute("selectedCategories", categories != null ? categories : Collections.emptyList());
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedStorages", storages != null ? storages : Collections.emptyList());
        model.addAttribute("selectedColors", colors != null ? colors : Collections.emptyList());
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);

        return "client/products";
    }

    @GetMapping("/products/filter")
    public String filterProductsFragment(
            @RequestParam(value = "category", required = false) List<String> categories,
            @RequestParam(value = "minPrice", required = false) Long minPrice,
            @RequestParam(value = "maxPrice", required = false) Long maxPrice,
            @RequestParam(value = "storage", required = false) List<String> storages,
            @RequestParam(value = "color", required = false) List<String> colors,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        ProductCatalogService.CatalogPageResult catalogResult = catalogService.filterProducts(
                categories, minPrice, maxPrice, storages, colors, sort, page, 12
        );

        model.addAttribute("catalogResult", catalogResult);
        model.addAttribute("currentSort", sort);

        return "client/fragments/product-grid :: product-grid";
    }

    @GetMapping("/product/{slug}")
    public String productDetail(@PathVariable("slug") String slug, Model model) {
        var opt = storefrontService.getProductDetailBySlug(slug);
        if (opt.isEmpty()) {
            return "redirect:/products";
        }

        var detail = opt.get();
        model.addAttribute("pageTitle", "Mua " + detail.productName() + " - IUH Store");
        model.addAttribute("isProductDetailPage", true);
        model.addAttribute("productName", detail.productName());
        model.addAttribute("tagline", detail.tagline());
        model.addAttribute("startingPriceFormatted", detail.startingPriceFormatted());
        model.addAttribute("colors", detail.colors());
        model.addAttribute("storages", detail.storages());
        model.addAttribute("inTheBox", detail.inTheBox());
        model.addAttribute("techSpecs", detail.techSpecs());
        model.addAttribute("compatibleAccessories", detail.compatibleAccessories());
        model.addAttribute("relatedProducts", detail.relatedProducts());
        model.addAttribute("variants", detail.variants());

        return "client/product-detail";
    }
}
