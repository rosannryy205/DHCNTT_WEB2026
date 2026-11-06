package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.*;
import com.example.dhcnttweb2026.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
public class AppleNotificationWorkflowTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private User testCustomer;
    private MockHttpSession customerSession;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testCustomer = new User(
                "Khách Hàng Notification Test " + suffix,
                "notify_test_" + suffix + "@iuhstore.com",
                passwordEncoder.encode("TestPass123!"),
                "0909123456",
                "12 Nguyễn Văn Bảo, Phường 4, Gò Vấp",
                User.Role.customer
        );
        testCustomer = userRepository.save(testCustomer);

        customerSession = new MockHttpSession();
        customerSession.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(testCustomer.getId(), testCustomer.getName(), testCustomer.getEmail(), testCustomer.getPhone()));
    }

    @Test
    @DisplayName("1. Apple Notification Assets are served properly with status 200")
    void testNotificationStaticAssetsAvailable() throws Exception {
        mockMvc.perform(get("/css/apple-notification.css"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/js/apple-notification.js"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. Storefront Layout and Templates include Apple Notification Assets & Flash Hooks")
    void testClientTemplatesIncludeNotificationAssets() throws Exception {
        MvcResult result = mockMvc.perform(get("/").session(customerSession))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("apple-notification.css"), "Client layout should link apple-notification.css");
        assertTrue(html.contains("apple-notification.js"), "Client layout should link apple-notification.js");
        assertTrue(html.contains("appleFlashSuccess"), "Client layout should declare appleFlashSuccess hook");
        assertTrue(html.contains("appleFlashError"), "Client layout should declare appleFlashError hook");
    }

    @Test
    @DisplayName("3. Admin Header includes Apple Notification Assets & Flash Hooks")
    void testAdminTemplatesIncludeNotificationAssets() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/products").sessionAttr("ADMIN_USER", "Admin Test"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("apple-notification.css"), "Admin header should link apple-notification.css");
        assertTrue(html.contains("apple-notification.js"), "Admin header should link apple-notification.js");
        assertTrue(html.contains("appleFlashSuccess"), "Admin header should declare appleFlashSuccess hook");
        assertTrue(html.contains("appleFlashError"), "Admin header should declare appleFlashError hook");
    }

    @Test
    @DisplayName("4. Workflow Add to Bag: Triggers Failure (401 unauth) vs Success (200 logged in)")
    void testAddToBagWorkflowNotificationPayloads() throws Exception {
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();

        // 4a. Chưa đăng nhập -> 401 requireLogin (Popup Thông báo Thất bại / Yêu cầu đăng nhập)
        mockMvc.perform(post("/api/bag/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "variantId", variant.getId(),
                                "sku", variant.getSku(),
                                "quantity", 1
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.requireLogin").value(true))
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 4b. Đã đăng nhập -> 200 success (Popup Thông báo Thành công)
        mockMvc.perform(post("/api/bag/add")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "variantId", variant.getId(),
                                "sku", variant.getSku(),
                                "quantity", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalItems").isNumber());
    }

    @Test
    @DisplayName("5. Workflow Bag Stepper & Remove: Triggers Failure vs Success")
    void testBagUpdateAndRemoveWorkflowNotificationPayloads() throws Exception {
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();

        Cart cart = new Cart();
        cart.setUser(testCustomer);
        cart = cartRepository.save(cart);

        CartItem item = new CartItem(null, cart, variant, 2);
        item = cartItemRepository.save(item);

        // 5a. Cập nhật số lượng thất bại (cartItemId không tồn tại)
        mockMvc.perform(post("/api/bag/update")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cartItemId", 999999L, "quantity", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 5b. Cập nhật số lượng thành công -> Popup Thành công
        mockMvc.perform(post("/api/bag/update")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cartItemId", item.getId(), "quantity", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5c. Xóa sản phẩm thất bại (thiếu cartItemId)
        mockMvc.perform(post("/api/bag/remove")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 5d. Xóa sản phẩm thành công -> Popup Thành công
        mockMvc.perform(post("/api/bag/remove")
                        .session(customerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cartItemId", item.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("6. Workflow Customer Profile: Success and Failure Flash Messages")
    void testProfileAndChangePasswordWorkflowNotifications() throws Exception {
        // 6a. Cập nhật hồ sơ thành công -> flash successMessage
        mockMvc.perform(post("/profile")
                        .session(customerSession)
                        .param("name", "Tên Mới Được Cập Nhật")
                        .param("phone", "0912345678")
                        .param("address", "100 Lê Lai, Quận 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        // 6b. Đổi mật khẩu thất bại (sai mật khẩu hiện tại) -> flash passwordErrorMessage
        mockMvc.perform(post("/profile/change-password")
                        .session(customerSession)
                        .param("currentPassword", "SaiMatKhau123!")
                        .param("newPassword", "MatKhauMoi456!")
                        .param("confirmPassword", "MatKhauMoi456!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("passwordErrorMessage"));

        // 6c. Đổi mật khẩu thành công -> flash passwordSuccessMessage
        mockMvc.perform(post("/profile/change-password")
                        .session(customerSession)
                        .param("currentPassword", "TestPass123!")
                        .param("newPassword", "MatKhauMoi456!")
                        .param("confirmPassword", "MatKhauMoi456!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("passwordSuccessMessage"));
    }

    @Test
    @DisplayName("7. Workflow Order Cancellation: Success and Failure States")
    void testOrderCancellationWorkflowNotifications() throws Exception {
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();

        // Tạo đơn hàng Pending có thể hủy
        Order order = new Order(
                testCustomer,
                "ORD-NOTIFY-" + UUID.randomUUID().toString().substring(0, 6),
                new BigDecimal("15000000"),
                Order.PaymentMethod.cod,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.Pending,
                "Địa chỉ nhận", "0909123456", "Ghi chú đơn"
        );
        order = orderRepository.save(order);
        OrderItem item = new OrderItem(order, variant, 1, new BigDecimal("15000000"), new BigDecimal("15000000"));
        order.getOrderItems().add(item);
        orderItemRepository.save(item);

        // 7a. Hủy đơn thành công -> flash successMessage
        mockMvc.perform(post("/orders/" + order.getOrderCode() + "/cancel")
                        .session(customerSession)
                        .param("cancellationReason", "Đổi ý không mua nữa")
                        .param("redirectSource", "list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attributeExists("successMessage"));

        // 7b. Hủy đơn đã bị hủy lần nữa -> flash errorMessage
        mockMvc.perform(post("/orders/" + order.getOrderCode() + "/cancel")
                        .session(customerSession)
                        .param("cancellationReason", "Hủy tiếp lần 2")
                        .param("redirectSource", "list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("8. Workflow Admin: Category Add Success and Delete Constraint Failure")
    void testAdminCategoryWorkflowNotifications() throws Exception {
        String catName = "Danh Mục Test " + System.currentTimeMillis();
        String slug = "danh-muc-test-" + System.currentTimeMillis();

        // 8a. Thêm danh mục thành công -> flash successMessage
        mockMvc.perform(post("/admin/categories/add")
                        .sessionAttr("ADMIN_USER", "Admin")
                        .param("name", catName)
                        .param("slug", slug)
                        .param("description", "Mô tả danh mục test")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));

        Category savedCat = categoryRepository.findBySlug(slug).orElseThrow();
        Brand brand = brandRepository.findAll().stream().findFirst().orElseThrow();

        // Gán 1 sản phẩm vào danh mục để kích hoạt ràng buộc không cho xóa
        Product product = new Product();
        product.setName("Sản phẩm thuộc danh mục");
        product.setSlug("san-pham-thuoc-danh-muc-" + System.currentTimeMillis());
        product.setCategory(savedCat);
        product.setBrand(brand);
        product.setIsActive(true);
        productRepository.save(product);

        // 8b. Xóa danh mục khi có sản phẩm ràng buộc -> flash errorMessage
        mockMvc.perform(post("/admin/categories/delete/" + savedCat.getId())
                        .sessionAttr("ADMIN_USER", "Admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
