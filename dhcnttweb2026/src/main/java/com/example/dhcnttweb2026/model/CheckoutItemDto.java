package com.example.dhcnttweb2026.model;

/**
 * DTO đại diện cho một sản phẩm trong giỏ/túi hàng hiển thị tại trang thanh toán (Checkout).
 */
public record CheckoutItemDto(
        String id,
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

