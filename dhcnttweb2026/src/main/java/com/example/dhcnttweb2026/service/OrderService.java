package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.entity.*;
import com.example.dhcnttweb2026.model.CheckoutFormDto;
import com.example.dhcnttweb2026.model.CheckoutItemDto;
import com.example.dhcnttweb2026.model.OrderDetailDto;
import com.example.dhcnttweb2026.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@SuppressWarnings("null")
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final BagService bagService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        ProductVariantRepository productVariantRepository,
                        UserRepository userRepository,
                        BagService bagService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.bagService = bagService;
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> findByOrderCode(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return Optional.empty();
        }
        return orderRepository.findByOrderCodeWithUser(orderCode)
                .or(() -> orderRepository.findByOrderCode(orderCode));
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Lấy danh sách sản phẩm chuẩn xác đang có trong Túi hàng của User để hiển thị ở trang Thanh toán.
     * Tự động đọc dữ liệu chi tiết kèm ProductVariant và Product bằng JOIN FETCH.
     */
    @Transactional(readOnly = true)
    public List<CheckoutItemDto> getCheckoutItems(String guestToken, Long userId) {
        Cart cart = null;
        if (userId != null) {
            cart = cartRepository.findByUserId(userId).orElse(null);
        }
        if (cart == null && guestToken != null && !guestToken.isBlank()) {
            cart = cartRepository.findByGuestToken(guestToken).orElse(null);
        }
        if (cart == null) {
            return Collections.emptyList();
        }

        List<CartItem> cartItems = cartItemRepository.findByCartIdWithProductDetails(cart.getId());
        if (cartItems == null || cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<CheckoutItemDto> result = new ArrayList<>();
        for (CartItem ci : cartItems) {
            if (ci == null) continue;
            CheckoutItemDto dto = mapCartItemToDto(ci);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    private CheckoutItemDto mapCartItemToDto(CartItem ci) {
        ProductVariant pv = ci.getProductVariant();
        if (pv == null) return null;
        Product p = pv.getProduct();

        String productName = (p != null && p.getName() != null) ? p.getName() : "Sản phẩm Apple";
        String storage = (pv.getStorage() != null && !pv.getStorage().isBlank()) ? pv.getStorage() : "Tiêu chuẩn";
        if (!"Tiêu chuẩn".equals(storage)) {
            productName += " " + storage;
        }

        String color = (pv.getColor() != null && !pv.getColor().isBlank()) ? pv.getColor() : "Tiêu chuẩn";
        long unitPrice = (pv.getPrice() != null) ? pv.getPrice().longValue() : 0L;
        int qty = (ci.getQuantity() != null && ci.getQuantity() > 0) ? ci.getQuantity() : 1;
        long lineTotal = unitPrice * qty;

        return new CheckoutItemDto(
                ci.getId(),
                pv.getSku() != null ? pv.getSku() : "",
                productName,
                color,
                ClientStorefrontService.resolveColorHex(color),
                storage,
                pv.getImage() != null ? pv.getImage() : "",
                unitPrice,
                ClientStorefrontService.formatVND(unitPrice),
                qty,
                lineTotal,
                ClientStorefrontService.formatVND(lineTotal),
                (p != null && p.getSlug() != null) ? "/product/" + p.getSlug() : "#"
        );
    }

    /**
     * Thực hiện thanh toán và lưu đơn hàng vào CSDL:
     * 1. Hợp nhất và lấy đúng các CartItem từ CSDL của người dùng
     * 2. Xử lý thông tin người đặt hàng an toàn chống lỗi dữ liệu quá dài (Data Truncation)
     * 3. Tạo bản ghi Order trong bảng `orders`
     * 4. Tạo các bản ghi OrderItem tương ứng trong bảng `order_items` với đúng variant, giá và số lượng
     * 5. Cập nhật trừ số lượng tồn kho (stock)
     * 6. Xóa sạch các mặt hàng trong giỏ hàng sau khi đặt thành công (tránh cascade ObjectDeletedException)
     */
    @Transactional
    public Order placeOrder(Long userId, String guestToken, CheckoutFormDto form) {
        // 1. Tìm hoặc hợp nhất giỏ hàng hiện tại của user hoặc guest thông qua BagService
        Cart cart = bagService.getOrCreateCart(guestToken, userId);
        if (cart == null) {
            throw new IllegalStateException("Không tìm thấy giỏ hàng của bạn.");
        }

        // 2. Lấy danh sách sản phẩm thực tế trong giỏ hàng từ CSDL
        List<CartItem> cartItems = cartItemRepository.findByCartIdWithProductDetails(cart.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Túi hàng của bạn đang trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
        }

        // 3. Chuẩn bị thông tin người nhận hàng & Cắt chuỗi an toàn chống Data Truncation
        String shippingPhone = (form.getShippingPhone() != null && !form.getShippingPhone().isBlank())
                ? form.getShippingPhone().trim()
                : "0900000000";
        if (shippingPhone.length() > 20) {
            shippingPhone = shippingPhone.substring(0, 20);
        }

        // Xử lý địa chỉ nhận hàng
        String shippingAddress = form.getShippingAddress();
        if (shippingAddress == null || shippingAddress.isBlank()) {
            List<String> addrParts = new ArrayList<>();
            if (form.getStreetAddress() != null && !form.getStreetAddress().isBlank()) addrParts.add(form.getStreetAddress().trim());
            if (form.getWard() != null && !form.getWard().isBlank()) addrParts.add(form.getWard().trim());
            if (form.getDistrict() != null && !form.getDistrict().isBlank()) addrParts.add(form.getDistrict().trim());
            if (form.getProvince() != null && !form.getProvince().isBlank()) addrParts.add(form.getProvince().trim());
            shippingAddress = String.join(", ", addrParts);
        }
        if (shippingAddress == null || shippingAddress.isBlank()) {
            shippingAddress = "Chưa cung cấp địa chỉ";
        }
        if (shippingAddress.length() > 255) {
            shippingAddress = shippingAddress.substring(0, 255);
        }

        // 4. Xác định User đặt hàng (nếu khách vãng lai thì tự động tạo tài khoản hoặc liên kết tài khoản theo email)
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && form.getEmail() != null && !form.getEmail().isBlank()) {
            user = userRepository.findByEmail(form.getEmail().trim()).orElse(null);
        }

        if (user == null && form.getEmail() != null && !form.getEmail().isBlank()) {
            String recipientName = (form.getRecipientName() != null && !form.getRecipientName().isBlank())
                    ? form.getRecipientName().trim() : "Khách hàng";
            if (recipientName.length() > 100) recipientName = recipientName.substring(0, 100);

            String email = form.getEmail().trim();
            if (email.length() > 255) email = email.substring(0, 255);

            User newCustomer = new User(
                    recipientName,
                    email,
                    "123456",
                    shippingPhone,
                    shippingAddress,
                    User.Role.customer
            );
            user = userRepository.save(newCustomer);
        }

        // Fallback: Tìm tài khoản customer có sẵn trong CSDL
        if (user == null) {
            user = userRepository.findById(3L).orElse(null);
            if (user == null) {
                user = userRepository.findAll().stream()
                        .filter(u -> u.getRole() == User.Role.customer)
                        .findFirst()
                        .orElse(null);
            }
        }

        if (user == null) {
            throw new IllegalStateException("Không tìm thấy thông tin khách hàng đặt đơn.");
        }

        // Cập nhật số điện thoại hoặc địa chỉ cho user nếu tài khoản chưa có
        boolean userUpdated = false;
        if ((user.getPhone() == null || user.getPhone().isBlank()) && !shippingPhone.isBlank()) {
            user.setPhone(shippingPhone);
            userUpdated = true;
        }
        if ((user.getAddress() == null || user.getAddress().isBlank()) && !shippingAddress.isBlank()) {
            user.setAddress(shippingAddress);
            userUpdated = true;
        }
        if (userUpdated) {
            userRepository.save(user);
        }

        // 5. Tính toán chính xác tổng tiền từ các sản phẩm trong giỏ hàng
        long subtotal = 0L;
        for (CartItem ci : cartItems) {
            ProductVariant pv = ci.getProductVariant();
            long price = (pv != null && pv.getPrice() != null) ? pv.getPrice().longValue() : 0L;
            int qty = (ci.getQuantity() != null && ci.getQuantity() > 0) ? ci.getQuantity() : 1;
            subtotal += price * qty;
        }

        // Áp dụng khuyến mãi nếu có
        long discount = 0L;
        if ("APPLEVIP".equalsIgnoreCase(form.getCouponCode())) {
            discount = 500000L;
        }
        long finalTotal = Math.max(0L, subtotal - discount);

        // 6. Chuẩn bị thông tin đơn hàng
        String orderCode = generateUniqueOrderCode();

        Order.PaymentMethod paymentMethod;
        try {
            paymentMethod = Order.PaymentMethod.valueOf(form.getPaymentMethod().toLowerCase().trim());
        } catch (Exception e) {
            paymentMethod = Order.PaymentMethod.cod;
        }

        Order.PaymentStatus paymentStatus = Order.PaymentStatus.PENDING;
        Order.OrderStatus orderStatus = Order.OrderStatus.Pending;

        // Ghi chú đơn hàng kèm VAT invoice nếu có (cắt <= 255 ký tự chống lỗi CSDL)
        String note = form.getNote();
        if (form.isNeedVatInvoice()) {
            String vatNote = String.format("[VAT: %s - MST: %s - %s - Email: %s]",
                    form.getCompanyName() != null ? form.getCompanyName().trim() : "",
                    form.getTaxCode() != null ? form.getTaxCode().trim() : "",
                    form.getCompanyAddress() != null ? form.getCompanyAddress().trim() : "",
                    form.getInvoiceEmail() != null ? form.getInvoiceEmail().trim() : "");
            note = (note != null && !note.isBlank()) ? note.trim() + " | " + vatNote : vatNote;
        }
        if (note != null && note.length() > 255) {
            note = note.substring(0, 255);
        }

        // 7. Tạo và lưu bản ghi Order vào bảng `orders`
        Order order = new Order(
                user,
                orderCode,
                BigDecimal.valueOf(finalTotal),
                paymentMethod,
                paymentStatus,
                orderStatus,
                shippingAddress,
                shippingPhone,
                note
        );
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order = orderRepository.save(order);

        // 8. Tạo từng bản ghi OrderItem tương ứng với từng CartItem và lưu vào bảng `order_items`
        for (CartItem ci : cartItems) {
            ProductVariant pv = ci.getProductVariant();
            if (pv == null) continue;

            int qty = (ci.getQuantity() != null && ci.getQuantity() > 0) ? ci.getQuantity() : 1;
            BigDecimal unitPrice = pv.getPrice() != null ? pv.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem orderItem = new OrderItem(order, pv, qty, unitPrice, lineTotal);
            orderItemRepository.save(orderItem);
            order.getOrderItems().add(orderItem);

            // Giảm số lượng tồn kho của biến thể sản phẩm
            if (pv.getStock() != null) {
                pv.setStock(Math.max(0, pv.getStock() - qty));
                productVariantRepository.save(pv);
            }
        }

        // 9. Xóa sạch các mặt hàng trong giỏ hàng sau khi đặt thành công (tránh cascade ObjectDeletedException)
        if (cart.getCartItems() != null) {
            cart.getCartItems().clear();
        }
        cartItemRepository.deleteAll(cartItems);
        cartItemRepository.flush();

        return order;
    }

    /**
     * Lấy thông tin chi tiết hoàn chỉnh của đơn hàng đóng gói trong OrderDetailDto.
     * Chạy toàn bộ trong Session Transaction để tránh LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public Optional<OrderDetailDto> getOrderDetailByCode(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return Optional.empty();
        }
        Optional<Order> orderOpt = orderRepository.findByOrderCodeWithUser(orderCode)
                .or(() -> orderRepository.findByOrderCode(orderCode));
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        List<CheckoutItemDto> items = getOrderItemsByOrderId(order.getId());

        LocalDateTime createdAt = (order.getCreatedAt() != null) ? order.getCreatedAt() : LocalDateTime.now();
        LocalDateTime deliveryDate = createdAt.plusDays(3);
        String formattedDeliveryDate = deliveryDate.format(DateTimeFormatter.ofPattern("dd 'Tháng' MM, yyyy"));
        String formattedOrderDate = createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String recipientName = "Khách hàng";
        String email = "";
        if (order.getUser() != null) {
            if (order.getUser().getName() != null && !order.getUser().getName().isBlank()) {
                recipientName = order.getUser().getName();
            }
            if (order.getUser().getEmail() != null) {
                email = order.getUser().getEmail();
            }
        }

        String paymentMethodCode = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "cod";
        String paymentMethodName = "Thanh toán khi nhận hàng (COD)";
        if (order.getPaymentMethod() != null) {
            paymentMethodName = switch (order.getPaymentMethod()) {
                case cod -> "Thanh toán khi nhận hàng (COD)";
                case vnpay -> "Cổng thanh toán VNPAY-QR & Thẻ ngân hàng";
                case momo -> "Ví điện tử MoMo";
                default -> "Thanh toán khi nhận hàng (COD)";
            };
        }

        long total = (order.getTotalAmount() != null) ? order.getTotalAmount().longValue() : 0L;
        String formattedTotal = ClientStorefrontService.formatVND(total);

        String paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "PENDING";
        String orderStatus = order.getOrderStatus() != null ? order.getOrderStatus().name() : "Pending";

        OrderDetailDto dto = new OrderDetailDto(
                order.getId(),
                order.getOrderCode(),
                formattedOrderDate,
                formattedDeliveryDate,
                recipientName,
                email,
                order.getShippingPhone(),
                order.getShippingAddress(),
                order.getNote(),
                paymentMethodCode,
                paymentMethodName,
                paymentStatus,
                orderStatus,
                total,
                formattedTotal,
                items
        );

        return Optional.of(dto);
    }

    /**
     * Lấy danh sách các dòng sản phẩm của đơn hàng từ CSDL và chuyển thành DTO hiển thị.
     */
    @Transactional(readOnly = true)
    public List<CheckoutItemDto> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderIdWithDetails(orderId);
        List<CheckoutItemDto> result = new ArrayList<>();

        for (OrderItem oi : items) {
            if (oi == null) continue;
            ProductVariant pv = oi.getProductVariant();
            if (pv == null) continue;
            Product p = pv.getProduct();

            String productName = (p != null && p.getName() != null) ? p.getName() : "Sản phẩm Apple";
            String storage = (pv.getStorage() != null && !pv.getStorage().isBlank()) ? pv.getStorage() : "Tiêu chuẩn";
            if (!"Tiêu chuẩn".equals(storage)) {
                productName += " " + storage;
            }

            String color = (pv.getColor() != null && !pv.getColor().isBlank()) ? pv.getColor() : "Tiêu chuẩn";
            long unitPrice = (oi.getPrice() != null) ? oi.getPrice().longValue() : 0L;
            int qty = (oi.getQuantity() != null) ? oi.getQuantity() : 1;
            long lineTotal = (oi.getTotalPrice() != null) ? oi.getTotalPrice().longValue() : unitPrice * qty;

            result.add(new CheckoutItemDto(
                    oi.getId(),
                    pv.getSku() != null ? pv.getSku() : "",
                    productName,
                    color,
                    ClientStorefrontService.resolveColorHex(color),
                    storage,
                    pv.getImage() != null ? pv.getImage() : "",
                    unitPrice,
                    ClientStorefrontService.formatVND(unitPrice),
                    qty,
                    lineTotal,
                    ClientStorefrontService.formatVND(lineTotal),
                    (p != null && p.getSlug() != null) ? "/product/" + p.getSlug() : "#"
            ));
        }

        return result;
    }

    private String generateUniqueOrderCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String code = String.format("ORD-%s-%04d", datePrefix, random.nextInt(10000));
            if (orderRepository.findByOrderCode(code).isEmpty()) {
                return code;
            }
        }
        return "ORD-" + datePrefix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
