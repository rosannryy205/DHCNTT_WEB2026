package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.dto.CustomerOrderDetailDto;
import com.example.dhcnttweb2026.dto.CustomerOrderItemDto;
import com.example.dhcnttweb2026.dto.CustomerOrderSummaryDto;
import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.entity.OrderItem;
import com.example.dhcnttweb2026.entity.Product;
import com.example.dhcnttweb2026.entity.ProductVariant;
import com.example.dhcnttweb2026.repository.OrderItemRepository;
import com.example.dhcnttweb2026.repository.OrderRepository;
import com.example.dhcnttweb2026.repository.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@SuppressWarnings("null")
public class CustomerOrderService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public CustomerOrderService(OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                ProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderSummaryDto> getCustomerOrders(Long userId, String statusFilter) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Order> orders = orderRepository.findByUserIdWithDetails(userId);
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<CustomerOrderSummaryDto> result = new ArrayList<>();
        for (Order order : orders) {
            if (order == null) continue;

            if (!matchesFilter(order.getOrderStatus(), statusFilter)) {
                continue;
            }

            List<CustomerOrderItemDto> items = mapOrderItems(order.getOrderItems());
            int totalItems = items.stream().mapToInt(CustomerOrderItemDto::quantity).sum();
            long totalAmount = order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0L;
            String formattedDate = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMATTER) : "";

            result.add(new CustomerOrderSummaryDto(
                    order.getId(),
                    order.getOrderCode(),
                    totalAmount,
                    ClientStorefrontService.formatVND(totalAmount),
                    order.getOrderStatus() != null ? order.getOrderStatus().name() : "",
                    translateOrderStatus(order.getOrderStatus()),
                    order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "",
                    translatePaymentStatus(order.getPaymentStatus()),
                    translatePaymentMethod(order.getPaymentMethod()),
                    formattedDate,
                    totalItems,
                    items,
                    order.canCancel(),
                    order.getCancellationReason()
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public CustomerOrderDetailDto getCustomerOrderDetail(String orderCode, Long userId) {
        if (orderCode == null || orderCode.isBlank() || userId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng hoặc bạn không có quyền truy cập.");
        }

        Order order = orderRepository.findByOrderCodeAndUserIdWithDetails(orderCode, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng hoặc bạn không có quyền truy cập."));

        List<CustomerOrderItemDto> items = mapOrderItems(order.getOrderItems());
        int totalItems = items.stream().mapToInt(CustomerOrderItemDto::quantity).sum();
        long totalAmount = order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0L;
        String formattedDate = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMATTER) : "";
        String estimatedDelivery = order.getCreatedAt() != null ? order.getCreatedAt().plusDays(2).format(DATE_ONLY_FORMATTER) : "";

        int currentStep = resolveStepperStep(order.getOrderStatus());
        String recipientName = order.getUser() != null ? order.getUser().getName() : "Khách hàng";
        String email = order.getUser() != null ? order.getUser().getEmail() : "";

        return new CustomerOrderDetailDto(
                order.getId(),
                order.getOrderCode(),
                totalAmount,
                ClientStorefrontService.formatVND(totalAmount),
                order.getOrderStatus() != null ? order.getOrderStatus().name() : "",
                translateOrderStatus(order.getOrderStatus()),
                order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "",
                translatePaymentStatus(order.getPaymentStatus()),
                translatePaymentMethod(order.getPaymentMethod()),
                recipientName,
                email,
                order.getShippingPhone(),
                order.getShippingAddress(),
                order.getNote(),
                formattedDate,
                estimatedDelivery,
                totalItems,
                items,
                order.canCancel(),
                order.getCancellationReason(),
                currentStep
        );
    }

    @Transactional
    public void cancelCustomerOrder(String orderCode, Long userId, String cancellationReason) {
        if (orderCode == null || orderCode.isBlank() || userId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng.");
        }

        Order order = orderRepository.findByOrderCodeForUpdate(orderCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng."));

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng hoặc bạn không có quyền truy cập.");
        }

        if (!order.canCancel()) {
            throw new IllegalStateException("Đơn hàng này không thể hủy vì đã được giao hoặc đã hoàn tất thanh toán.");
        }

        String reason = (cancellationReason != null && !cancellationReason.isBlank())
                ? cancellationReason.strip()
                : "Khách hàng yêu cầu hủy đơn";

        order.cancel(reason);

        // Atomic inventory rollback via orderItemRepository to ensure items are fresh
        List<OrderItem> items = orderItemRepository.findByOrderIdWithDetails(order.getId());
        if (items != null && !items.isEmpty()) {
            items.stream()
                    .sorted(Comparator.comparing(item -> item.getProductVariant().getId()))
                    .forEach(item -> {
                        int updated = productVariantRepository.restoreStock(item.getProductVariant().getId(), item.getQuantity());
                        if (updated != 1) {
                            throw new IllegalStateException("Không thể hoàn tồn kho cho sản phẩm " + item.getProductVariant().getSku());
                        }
                    });
        }

        orderRepository.save(order);
    }

    private boolean matchesFilter(Order.OrderStatus status, String filter) {
        if (filter == null || filter.isBlank() || "all".equalsIgnoreCase(filter)) {
            return true;
        }
        if (status == null) {
            return false;
        }

        return switch (filter.toLowerCase()) {
            case "processing" -> status == Order.OrderStatus.Pending || status == Order.OrderStatus.Processing;
            case "shipped" -> status == Order.OrderStatus.Shipped;
            case "delivered" -> status == Order.OrderStatus.Delivered;
            case "cancelled" -> status == Order.OrderStatus.Cancelled;
            default -> true;
        };
    }

    private List<CustomerOrderItemDto> mapOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<CustomerOrderItemDto> dtos = new ArrayList<>();
        for (OrderItem oi : orderItems) {
            if (oi == null) continue;
            ProductVariant pv = oi.getProductVariant();
            if (pv == null) continue;
            Product p = pv.getProduct();

            String productName = (p != null && p.getName() != null) ? p.getName() : "Sản phẩm Apple";
            String storage = (pv.getStorage() != null && !pv.getStorage().isBlank()) ? pv.getStorage() : "Tiêu chuẩn";
            String color = (pv.getColor() != null && !pv.getColor().isBlank()) ? pv.getColor() : "Tiêu chuẩn";
            String colorHex = ClientStorefrontService.resolveColorHex(color);
            String imageUrl = pv.getImage() != null ? pv.getImage() : "";
            long unitPrice = oi.getPrice() != null ? oi.getPrice().longValue() : 0L;
            int quantity = oi.getQuantity() != null ? oi.getQuantity() : 1;
            long lineTotal = oi.getTotalPrice() != null ? oi.getTotalPrice().longValue() : (unitPrice * quantity);
            String productDetailUrl = (p != null && p.getSlug() != null) ? "/products/" + p.getSlug() : "/products";

            dtos.add(new CustomerOrderItemDto(
                    oi.getId(),
                    pv.getSku(),
                    productName,
                    color,
                    colorHex,
                    storage,
                    imageUrl,
                    unitPrice,
                    ClientStorefrontService.formatVND(unitPrice),
                    quantity,
                    lineTotal,
                    ClientStorefrontService.formatVND(lineTotal),
                    productDetailUrl
            ));
        }
        return dtos;
    }

    private int resolveStepperStep(Order.OrderStatus status) {
        if (status == null) return 1;
        return switch (status) {
            case Pending -> 1;
            case Processing -> 2;
            case Shipped -> 3;
            case Delivered -> 4;
            case Cancelled -> 0;
        };
    }

    public static String translateOrderStatus(Order.OrderStatus status) {
        if (status == null) return "Chờ xác nhận";
        return switch (status) {
            case Pending -> "Chờ xác nhận";
            case Processing -> "Đang xử lý";
            case Shipped -> "Đang vận chuyển";
            case Delivered -> "Đã giao hàng";
            case Cancelled -> "Đã hủy";
        };
    }

    public static String translatePaymentStatus(Order.PaymentStatus status) {
        if (status == null) return "Chờ thanh toán";
        return switch (status) {
            case PENDING -> "Chờ thanh toán";
            case PAID -> "Đã thanh toán";
            case FAILED -> "Thanh toán thất bại";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }

    public static String translatePaymentMethod(Order.PaymentMethod method) {
        if (method == null) return "Thanh toán khi nhận hàng (COD)";
        return switch (method) {
            case cod -> "Thanh toán khi nhận hàng (COD)";
            case vnpay -> "Cổng thanh toán VNPAY";
            case momo -> "Ví điện tử MoMo";
        };
    }
}
