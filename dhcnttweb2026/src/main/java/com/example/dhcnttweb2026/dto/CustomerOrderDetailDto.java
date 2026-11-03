package com.example.dhcnttweb2026.dto;

import java.util.List;

public record CustomerOrderDetailDto(
        Long id,
        String orderCode,
        long totalAmount,
        String formattedTotal,
        String orderStatusCode,
        String orderStatusName,
        String paymentStatusCode,
        String paymentStatusName,
        String paymentMethodName,
        String recipientName,
        String email,
        String shippingPhone,
        String shippingAddress,
        String note,
        String orderDate,
        String estimatedDeliveryDate,
        int totalItems,
        List<CustomerOrderItemDto> items,
        boolean canCancel,
        String cancellationReason,
        int currentStep
) {}
