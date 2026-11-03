package com.example.dhcnttweb2026.dto;

import java.math.BigDecimal;
import java.util.List;

/** Detached, read-only order data for the admin list and detail panel. */
public record AdminOrderDto(
        String code, String name, String email, String phone, String address,
        String date, String status, String payment, String method, String note, String cancellationReason,
        List<Item> items, BigDecimal subtotal, BigDecimal adjustment, BigDecimal total) {

    public record Item(String name, String variant, String sku, BigDecimal price,
                       int quantity, BigDecimal total, String icon) {}
}

