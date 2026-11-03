package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.entity.OrderItem;
import com.example.dhcnttweb2026.entity.ProductVariant;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.repository.OrderItemRepository;
import com.example.dhcnttweb2026.repository.OrderRepository;
import com.example.dhcnttweb2026.repository.ProductVariantRepository;
import com.example.dhcnttweb2026.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class CustomerProfileAndOrderTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private User testCustomer;
    private User otherCustomer;
    private MockHttpSession customerSession;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();

        // 1. Tạo customer chính cho test
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        testCustomer = new User(
                "Khách Hàng Test " + uniqueSuffix,
                "testcustomer_" + uniqueSuffix + "@iuhstore.com",
                passwordEncoder.encode("Password123!"),
                "0987654321",
                "12 Nguyễn Văn Bảo, Phường 4, Gò Vấp",
                User.Role.customer
        );
        testCustomer = userRepository.save(testCustomer);

        // 2. Tạo customer thứ 2 để kiểm thử chống IDOR
        otherCustomer = new User(
                "Khách Hàng Khác " + uniqueSuffix,
                "othercustomer_" + uniqueSuffix + "@iuhstore.com",
                passwordEncoder.encode("Password123!"),
                "0911223344",
                "15 Lê Duẩn, Hà Nội",
                User.Role.customer
        );
        otherCustomer = userRepository.save(otherCustomer);

        customerSession = new MockHttpSession();
        customerSession.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(testCustomer.getId(), testCustomer.getName(), testCustomer.getEmail(), testCustomer.getPhone()));
    }

    @Test
    void testUnauthenticatedAccessRedirectsToLogin() throws Exception {
        // Kiểm tra truy cập /profile khi chưa đăng nhập
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?returnUrl=%2Fprofile"));

        // Kiểm tra truy cập /orders khi chưa đăng nhập
        mockMvc.perform(get("/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?returnUrl=%2Forders"));
    }

    @Test
    void testShowProfilePageAuthenticatedRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/profile").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeExists("profileDto"))
                .andExpect(model().attributeExists("passwordDto"))
                .andExpect(model().attribute("pageTitle", "Hồ sơ của tôi - IUH Store"));
    }

    @Test
    void testUpdateProfileSuccess() throws Exception {
        mockMvc.perform(post("/profile")
                        .session(customerSession)
                        .param("name", "Nguyễn Văn Cập Nhật")
                        .param("phone", "0999888777")
                        .param("address", "99 Đường Mới, Quận 1, TP.HCM"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        User updatedUser = userRepository.findById(testCustomer.getId()).orElseThrow();
        assertEquals("Nguyễn Văn Cập Nhật", updatedUser.getName());
        assertEquals("0999888777", updatedUser.getPhone());
        assertEquals("99 Đường Mới, Quận 1, TP.HCM", updatedUser.getAddress());

        // Session cũng phải được cập nhật tên mới
        CustomerSessionDto sessionDto = (CustomerSessionDto) customerSession.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY);
        assertNotNull(sessionDto);
        assertEquals("Nguyễn Văn Cập Nhật", sessionDto.getName());
    }

    @Test
    void testChangePasswordSuccess() throws Exception {
        mockMvc.perform(post("/profile/change-password")
                        .session(customerSession)
                        .param("currentPassword", "Password123!")
                        .param("newPassword", "NewSecurePassword456!")
                        .param("confirmPassword", "NewSecurePassword456!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("passwordSuccessMessage"));

        User updatedUser = userRepository.findById(testCustomer.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewSecurePassword456!", updatedUser.getPassword()));
    }

    @Test
    void testChangePasswordFailsWhenWrongCurrentPassword() throws Exception {
        mockMvc.perform(post("/profile/change-password")
                        .session(customerSession)
                        .param("currentPassword", "WrongPassword!")
                        .param("newPassword", "NewSecurePassword456!")
                        .param("confirmPassword", "NewSecurePassword456!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("passwordErrorMessage", "Mật khẩu hiện tại không chính xác."));

        User updatedUser = userRepository.findById(testCustomer.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("Password123!", updatedUser.getPassword()));
    }

    @Test
    void testShowOrdersWithEmptyStateAndFilteredOrders() throws Exception {
        // Ban đầu chưa có đơn
        mockMvc.perform(get("/orders").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("client/orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("currentFilter", "all"));

        // Tạo 1 đơn Pending và 1 đơn Shipped
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();

        Order orderPending = new Order(
                testCustomer,
                "ORD-TEST-PENDING-" + UUID.randomUUID().toString().substring(0, 6),
                new BigDecimal("30000000"),
                Order.PaymentMethod.cod,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.Pending,
                "Địa chỉ 1", "0987654321", "Ghi chú"
        );
        orderPending = orderRepository.save(orderPending);
        OrderItem item1 = new OrderItem(orderPending, variant, 1, new BigDecimal("30000000"), new BigDecimal("30000000"));
        orderPending.getOrderItems().add(item1);
        orderItemRepository.save(item1);

        Order orderShipped = new Order(
                testCustomer,
                "ORD-TEST-SHIPPED-" + UUID.randomUUID().toString().substring(0, 6),
                new BigDecimal("20000000"),
                Order.PaymentMethod.cod,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.Shipped,
                "Địa chỉ 2", "0987654321", "Ghi chú"
        );
        orderShipped = orderRepository.save(orderShipped);
        OrderItem item2 = new OrderItem(orderShipped, variant, 1, new BigDecimal("20000000"), new BigDecimal("20000000"));
        orderShipped.getOrderItems().add(item2);
        orderItemRepository.save(item2);

        // Lọc processing -> Chỉ chứa orderPending
        mockMvc.perform(get("/orders").param("status", "processing").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentFilter", "processing"));

        // Lọc shipped -> Chỉ chứa orderShipped
        mockMvc.perform(get("/orders").param("status", "shipped").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentFilter", "shipped"));
    }

    @Test
    void testShowOrderDetailSuccess() throws Exception {
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();
        String orderCode = "ORD-TEST-DETAIL-" + UUID.randomUUID().toString().substring(0, 6);

        Order order = new Order(
                testCustomer,
                orderCode,
                new BigDecimal("35000000"),
                Order.PaymentMethod.cod,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.Pending,
                "Địa chỉ nhận", "0987654321", "Giao ban ngày"
        );
        order = orderRepository.save(order);
        OrderItem item = new OrderItem(order, variant, 1, new BigDecimal("35000000"), new BigDecimal("35000000"));
        order.getOrderItems().add(item);
        orderItemRepository.save(item);

        mockMvc.perform(get("/orders/" + orderCode).session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("client/order-detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    void testShowOrderDetailUnauthorizedIDORReturns404() throws Exception {
        // Tạo đơn của otherCustomer
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();
        String otherOrderCode = "ORD-OTHER-" + UUID.randomUUID().toString().substring(0, 6);

        Order otherOrder = new Order(
                otherCustomer,
                otherOrderCode,
                new BigDecimal("25000000"),
                Order.PaymentMethod.cod,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.Pending,
                "Địa chỉ người khác", "0911223344", "Ghi chú"
        );
        otherOrder = orderRepository.save(otherOrder);
        OrderItem item = new OrderItem(otherOrder, variant, 1, new BigDecimal("25000000"), new BigDecimal("25000000"));
        otherOrder.getOrderItems().add(item);
        orderItemRepository.save(item);

        // testCustomer cố tình truy cập đơn của otherCustomer -> Phải trả về 404 Not Found
        mockMvc.perform(get("/orders/" + otherOrderCode).session(customerSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelOrderSuccessAndRestoresStock() throws Exception {
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();
        int initialStock = variant.getStock();
        int orderQty = 2;

        String orderCode = "ORD-CANCEL-" + UUID.randomUUID().toString().substring(0, 6);
        Order order = new Order(
                testCustomer,
                orderCode,
                new BigDecimal("40000000"),
                Order.PaymentMethod.cod,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.Pending,
                "Địa chỉ", "0987654321", "Ghi chú"
        );
        order = orderRepository.save(order);
        OrderItem item = new OrderItem(order, variant, orderQty, new BigDecimal("20000000"), new BigDecimal("40000000"));
        order.getOrderItems().add(item);
        orderItemRepository.save(item);

        // Thực hiện tự hủy đơn
        mockMvc.perform(post("/orders/" + orderCode + "/cancel")
                        .session(customerSession)
                        .param("cancellationReason", "Đổi ý, không muốn mua nữa")
                        .param("redirectSource", "list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attributeExists("successMessage"));

        // Kiểm tra trạng thái đơn đã sang Cancelled
        Order cancelledOrder = orderRepository.findByOrderCode(orderCode).orElseThrow();
        assertEquals(Order.OrderStatus.Cancelled, cancelledOrder.getOrderStatus());
        assertEquals("Đổi ý, không muốn mua nữa", cancelledOrder.getCancellationReason());

        // Xóa persistence context cache để đọc giá trị stock mới nhất từ database
        entityManager.clear();
        ProductVariant refreshedVariant = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertEquals(initialStock + orderQty, refreshedVariant.getStock());
    }

    @Test
    void testCancelOrderFailsWhenAlreadyShipped() throws Exception {
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();
        String orderCode = "ORD-SHIPPED-" + UUID.randomUUID().toString().substring(0, 6);

        Order order = new Order(
                testCustomer,
                orderCode,
                new BigDecimal("30000000"),
                Order.PaymentMethod.cod,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.Shipped, // Đã giao vận
                "Địa chỉ", "0987654321", "Ghi chú"
        );
        order = orderRepository.save(order);
        OrderItem item = new OrderItem(order, variant, 1, new BigDecimal("30000000"), new BigDecimal("30000000"));
        order.getOrderItems().add(item);
        orderItemRepository.save(item);

        // Cố hủy đơn đã Shipped -> Báo lỗi flash message
        mockMvc.perform(post("/orders/" + orderCode + "/cancel")
                        .session(customerSession)
                        .param("cancellationReason", "Hủy đơn")
                        .param("redirectSource", "detail"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + orderCode))
                .andExpect(flash().attributeExists("errorMessage"));

        Order unchangedOrder = orderRepository.findByOrderCode(orderCode).orElseThrow();
        assertEquals(Order.OrderStatus.Shipped, unchangedOrder.getOrderStatus());
    }
}
