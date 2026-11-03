package com.example.dhcnttweb2026.dto;

public record BagItemDto(
        Long id,
        String sku,
        String productName,
        String colorName,
        String colorHex,
        String storageCapacity,
        String imageUrl,
        long unitPrice,
        String formattedUnitPrice,
        int quantity,
        int stockQuantity,
        long lineTotal,
        String formattedLineTotal,
        String productDetailUrl
) {}
