package com.example.dhcnttweb2026.model;

/**
 * DTO đại diện cho khối Tóm tắt đơn hàng (Order Summary) trên trang Checkout.
 */
public record CheckoutSummaryDto(
        long subtotal,
        String formattedSubtotal,
        long shippingFee,
        String formattedShippingFee,
        long discount,
        String formattedDiscount,
        long total,
        String formattedTotal,
        int totalItemsCount
) {}

