package com.example.dhcnttweb2026.dto;

import java.util.List;

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
