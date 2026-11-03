package com.example.dhcnttweb2026.dto;

public record CustomerOrderItemDto(
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
        long lineTotal,
        String formattedLineTotal,
        String productDetailUrl
) {}
