package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.repository.CartItemRepository;
import com.example.dhcnttweb2026.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@SuppressWarnings("null")
class ClientStorefrontAndBagIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();
    }

    @Test
    void testHomePageLoadsWithRealDatabaseProducts() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("iPhone"), "Home page should render products from database");
        assertTrue(content.contains("Sản phẩm nổi bật"), "Home page should contain featured section");
    }

    @Test
    void testProductDetailPageLoadsDynamicVariantsFromDatabase() throws Exception {
        MvcResult result = mockMvc.perform(get("/product/iphone-16-pro"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("iPhone 16 Pro"), "Page title should contain product name");
        assertTrue(content.contains("product-detail.js"), "Page should include product-detail.js");
        assertTrue(content.contains("variants"), "Inline variant JSON should be injected");
    }

    @Test
    void testProductDetailRedirectsForUnknownSlug() throws Exception {
        mockMvc.perform(get("/product/non-existent-device-slug"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    void testBagPageRendersSuccessfully() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(3L, "Nguyễn Minh Quân", "quan.nguyen@gmail.com", "0987654321"));

        MvcResult result = mockMvc.perform(get("/bag").session(session))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Túi hàng của bạn"), "Bag page should render header");
        assertTrue(content.contains("bag.css"), "Bag page should link bag.css");
    }

    @Test
    void testAddToBagUnauthenticatedReturns401AndSetsWarning() throws Exception {
        String addPayload = objectMapper.writeValueAsString(Map.of(
                "sku", "IP16P-128-NATURAL",
                "quantity", 1
        ));

        // 1. Unauthenticated request to /api/bag/add should return 401 Unauthorized
        MvcResult addResult = mockMvc.perform(post("/api/bag/add")
                        .header("Referer", "http://localhost:8080/product/iphone-16-pro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.requireLogin").value(true))
                .andExpect(jsonPath("$.redirectUrl").value("/login?returnUrl=%2Fproduct%2Fiphone-16-pro"))
                .andExpect(jsonPath("$.message").value("Vui lòng đăng nhập tài khoản IUH Store để thêm sản phẩm vào túi hàng."))
                .andReturn();

        // 2. The session should contain the warning message
        MockHttpSession session = (MockHttpSession) addResult.getRequest().getSession(false);
        assertNotNull(session, "Session should be created to store auth warning");
        Objects.requireNonNull(session);

        // 3. Visiting /login with this session renders the Apple alert warning
        MvcResult loginResult = mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andReturn();

        String loginHtml = loginResult.getResponse().getContentAsString();
        assertTrue(loginHtml.contains("Vui lòng đăng nhập tài khoản IUH Store để thêm sản phẩm vào túi hàng."),
                "Login page should display the auth warning banner");
    }

    @Test
    void testBagApiLifecycle() throws Exception {
        cartRepository.findByUserId(3L).ifPresent(cart -> {
            cartItemRepository.deleteAll(cartItemRepository.findByCartId(cart.getId()));
        });

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(3L, "Nguyễn Minh Quân", "quan.nguyen@gmail.com", "0987654321"));

        String guestToken = "test-guest-token-" + System.currentTimeMillis();
        Cookie cookie = new Cookie("guest_token", guestToken);

        // 1. Add variant to bag
        String addPayload = objectMapper.writeValueAsString(Map.of(
                "sku", "IP16P-128-NATURAL",
                "quantity", 2
        ));

        mockMvc.perform(post("/api/bag/add")
                        .session(session)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalItems").isNumber());

        // 2. Check count
        mockMvc.perform(get("/api/bag/count")
                        .session(session)
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));

        // 3. Render /bag with session to verify bag items appear
        MvcResult bagResult = mockMvc.perform(get("/bag").session(session).cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        String html = bagResult.getResponse().getContentAsString();
        assertTrue(html.contains("iPhone 16 Pro"), "Bag should show added product");
    }
}
