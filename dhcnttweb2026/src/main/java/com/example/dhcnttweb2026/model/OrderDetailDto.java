package com.example.dhcnttweb2026.model;

import java.util.List;

/**
 * DTO chứa toàn bộ thông tin chi tiết của một đơn hàng để hiển thị
 * trên trang thanh toán thành công (checkout-success) hoặc tra cứu chi tiết đơn hàng.
 * Tách biệt hoàn toàn khỏi Hibernate Entity để tránh triệt để lỗi LazyInitializationException.
 */
public record OrderDetailDto(
        Long orderId,
        String orderCode,
        String orderDate,
        String estimatedDeliveryDate,
        String recipientName,
        String email,
        String shippingPhone,
        String shippingAddress,
        String note,
        String paymentMethodCode,
        String paymentMethodName,
        String paymentStatus,
        String orderStatus,
        long totalAmount,
        String formattedTotal,
        List<CheckoutItemDto> items
) {}

