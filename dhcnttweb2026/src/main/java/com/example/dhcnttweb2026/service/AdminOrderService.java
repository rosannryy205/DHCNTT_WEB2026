package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.entity.OrderItem;
import com.example.dhcnttweb2026.dto.AdminOrderDto;
import com.example.dhcnttweb2026.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class AdminOrderService {
    private final OrderRepository orderRepository;

    public AdminOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminOrderDto> findAll() {
        return orderRepository.findAllWithDetails().stream().map(this::toDto).toList();
    }

    private AdminOrderDto toDto(Order order) {
        var items = order.getOrderItems().stream().map(this::toItem).toList();
        BigDecimal subtotal = items.stream().map(AdminOrderDto.Item::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String method = switch (order.getPaymentMethod()) {
            case cod -> "Thanh toán khi nhận hàng (COD)";
            case vnpay -> "VNPay";
            case momo -> "MoMo";
        };
        return new AdminOrderDto(order.getOrderCode(), order.getUser().getName(),
                Objects.toString(order.getUser().getEmail(), ""),
                Objects.toString(order.getShippingPhone(), ""),
                Objects.toString(order.getShippingAddress(), ""),
                order.getCreatedAt().atOffset(ZoneOffset.ofHours(7)).toString(),
                order.getOrderStatus().name().toUpperCase(Locale.ROOT),
                order.getPaymentStatus().name(), method, order.getNote(), order.getCancellationReason(), items,
                subtotal, order.getTotalAmount().subtract(subtotal), order.getTotalAmount());
    }

    private AdminOrderDto.Item toItem(OrderItem item) {
        var variant = item.getProductVariant();
        String options = Stream.of(variant.getColor(), variant.getRam(), variant.getStorage())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" · "));
        return new AdminOrderDto.Item(variant.getProduct().getName(), options,
                Objects.toString(variant.getSku(), ""), item.getPrice(), item.getQuantity(),
                item.getTotalPrice(), "product");
    }
}
