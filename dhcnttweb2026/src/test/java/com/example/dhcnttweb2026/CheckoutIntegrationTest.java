package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.*;
import com.example.dhcnttweb2026.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class CheckoutIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();
    }

    @Test
    void testCheckoutPageLoadsWithExactProductsFromUserCart() throws Exception {
        // Setup User 3 (Nguyễn Minh Quân)
        User user = userRepository.findById(3L).orElseThrow();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(user.getId(), user.getName(), user.getEmail(), user.getPhone()));

        // Đảm bảo user có giỏ hàng với 1 sản phẩm thực tế
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> cartRepository.save(new Cart(user)));
        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();
        cartItemRepository.save(new CartItem(null, cart, variant, 2));

        // GET /checkout
        MvcResult result = mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("client/checkout"))
                .andExpect(model().attributeExists("checkoutItems"))
                .andExpect(model().attributeExists("summary"))
                .andExpect(model().attributeExists("checkoutForm"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("Thanh toán."), "Checkout page should render main title");
        assertTrue(html.contains(variant.getProduct().getName()), "Checkout summary should render the exact product name from cart");
    }

    @Test
    void testCheckoutSubmissionCreatesOrderAndClearsCart() throws Exception {
        User user = userRepository.findById(3L).orElseThrow();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(user.getId(), user.getName(), user.getEmail(), user.getPhone()));

        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> cartRepository.save(new Cart(user)));
        cartItemRepository.deleteAll(cartItemRepository.findByCartId(cart.getId()));

        ProductVariant variant = productVariantRepository.findAll().stream().findFirst().orElseThrow();
        cartItemRepository.save(new CartItem(null, cart, variant, 1));

        // POST /checkout
        MvcResult postResult = mockMvc.perform(post("/checkout")
                        .session(session)
                        .param("recipientName", "Nguyễn Minh Quân")
                        .param("shippingPhone", "0987654321")
                        .param("email", "quan.nguyen@gmail.com")
                        .param("streetAddress", "Số 15 Lê Duẩn")
                        .param("ward", "Phường Tràng Tiền")
                        .param("district", "Quận Hoàn Kiếm")
                        .param("province", "Thành phố Hà Nội")
                        .param("shippingAddress", "Số 15 Lê Duẩn, Phường Tràng Tiền, Quận Hoàn Kiếm, Thành phố Hà Nội")
                        .param("paymentMethod", "cod")
                        .param("note", "Gọi điện trước khi giao"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = postResult.getResponse().getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertTrue(redirectedUrl.startsWith("/checkout/success?orderCode=ORD-"), "Should redirect to success page with generated order code");

        String orderCode = redirectedUrl.substring(redirectedUrl.indexOf("orderCode=") + 10);

        // Kiểm tra Order trong CSDL
        Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
        assertTrue(orderOpt.isPresent(), "Order must be persisted in database");
        Order order = orderOpt.get();
        assertEquals("0987654321", order.getShippingPhone());
        assertEquals(Order.PaymentMethod.cod, order.getPaymentMethod());
        assertEquals(Order.PaymentStatus.PENDING, order.getPaymentStatus());

        // Kiểm tra OrderItem trong CSDL phải ra ĐÚNG sản phẩm
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        assertEquals(1, items.size(), "Order must contain exactly 1 order item");
        assertEquals(variant.getId(), items.get(0).getProductVariant().getId(), "OrderItem must match the exact product variant from cart");
        assertEquals(1, items.get(0).getQuantity());

        // Kiểm tra Cart phải được xóa sạch sau khi đặt hàng
        List<CartItem> remainingCartItems = cartItemRepository.findByCartId(cart.getId());
        assertTrue(remainingCartItems.isEmpty(), "Cart items must be cleared after successful checkout");

        // GET /checkout/success?orderCode=...
        MvcResult successResult = mockMvc.perform(get("/checkout/success").param("orderCode", orderCode).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("client/checkout-success"))
                .andExpect(model().attribute("orderCode", orderCode))
                .andExpect(model().attributeExists("orderItems"))
                .andReturn();

        String successHtml = successResult.getResponse().getContentAsString();
        assertTrue(successHtml.contains(orderCode), "Success page must display the real order code");
        assertTrue(successHtml.contains(variant.getProduct().getName()), "Success page must display the exact purchased product name");
    }

    @Test
    void testCheckoutRedirectsToBagWhenCartIsEmpty() throws Exception {
        User user = userRepository.findById(3L).orElseThrow();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(user.getId(), user.getName(), user.getEmail(), user.getPhone()));

        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> cartRepository.save(new Cart(user)));
        cartItemRepository.deleteAll(cartItemRepository.findByCartId(cart.getId()));

        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bag"))
                .andExpect(flash().attributeExists("warningMessage"));
    }
}

