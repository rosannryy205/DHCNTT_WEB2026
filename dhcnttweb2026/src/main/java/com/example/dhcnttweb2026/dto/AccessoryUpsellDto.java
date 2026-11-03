package com.example.dhcnttweb2026.dto;

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
