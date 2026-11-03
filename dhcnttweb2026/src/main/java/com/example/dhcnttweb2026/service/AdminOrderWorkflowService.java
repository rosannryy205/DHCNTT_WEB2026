package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.repository.OrderRepository;
import com.example.dhcnttweb2026.repository.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminOrderWorkflowService {
    private final OrderRepository orders;
    private final ProductVariantRepository variants;

    public AdminOrderWorkflowService(OrderRepository orders, ProductVariantRepository variants) {
        this.orders = orders;
        this.variants = variants;
    }

    public record StatusChange(Order.OrderStatus expectedStatus, Order.OrderStatus status, String reason) {}
    public record Result(String code, Order.OrderStatus status, Order.PaymentStatus payment, String cancellationReason) {}

    @Transactional
    public Result changeStatus(String code, StatusChange change) {
        if (change == null || change.expectedStatus() == null || change.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phải cung cấp trạng thái hiện tại và trạng thái đích.");
        }
        var order = orders.findByOrderCodeForUpdate(code).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng."));
        if (order.getOrderStatus() != change.expectedStatus()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Đơn hàng đã thay đổi. Vui lòng tải lại trang trước khi thao tác.");
        }
        try {
            if (change.status() == Order.OrderStatus.Cancelled) {
                order.cancel(change.reason());
            } else {
                order.advanceToNextStatus(change.status());
            }
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
        if (change.status() == Order.OrderStatus.Cancelled) {
            // Atomic increments avoid lost stock updates between cancellations of different orders.
            order.getOrderItems().stream()
                    .sorted(java.util.Comparator.comparing(item -> item.getProductVariant().getId()))
                    .forEach(item -> {
                        if (variants.restoreStock(item.getProductVariant().getId(), item.getQuantity()) != 1) {
                            throw new IllegalStateException("Không thể hoàn tồn kho cho đơn hàng.");
                        }
                    });
        }
        return new Result(code, order.getOrderStatus(), order.getPaymentStatus(), order.getCancellationReason());
    }
}