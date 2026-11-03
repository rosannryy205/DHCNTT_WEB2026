package com.example.dhcnttweb2026.dto;

import java.util.List;

public record CustomerOrderSummaryDto(
        Long id,
        String orderCode,
        long totalAmount,
        String formattedTotal,
        String orderStatusCode,
        String orderStatusName,
        String paymentStatusCode,
        String paymentStatusName,
        String paymentMethodName,
        String orderDate,
        int totalItems,
        List<CustomerOrderItemDto> items,
        boolean canCancel,
        String cancellationReason
) {}
