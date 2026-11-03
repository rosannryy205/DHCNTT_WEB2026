package com.example.dhcnttweb2026;

import jakarta.servlet.SessionTrackingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ClientHomeControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();
    }

    @Test
    void testHomePageRendersSuccessfully() throws Exception {
        String content = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("client.css"), "Home page must include client.css");
        assertTrue(content.contains("apple-design-tokens.css"), "Home page must include apple-design-tokens.css");
    }

    @Test
    void testGlobalNavRendersSubCategoriesUnderParent() throws Exception {
        String content = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("apple-nav-flyout"), "Navigation must render apple-nav-flyout dropdowns for subcategories");
        assertTrue(content.contains("Khám phá"), "Navigation flyout must include 'Khám phá' section");
        assertTrue(content.contains("iPhone 16 Series") || content.contains("MacBook Pro"), "Flyout must include subcategories under parent category");
    }

    @Test
    void testFlyoutControllerScriptAndSubcategoryNavigation() throws Exception {
        String homeContent = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Must include client.js which now contains the hover intent controller
        assertTrue(homeContent.contains("client.js"), "Page must load client.js for hover intent flyout interaction");

        // Subcategory navigation to filtered products page must return 200 OK
        mockMvc.perform(get("/products").param("category", "iphone-16-series"))
                .andExpect(status().isOk());
    }

    @Test
    void testSessionTrackingModeIsCookieOnlyAndNoJsessionIdInUrls() throws Exception {
        // 1. Verify tracking mode config: URL session tracking must NOT be present
        Set<SessionTrackingMode> modes = webApplicationContext.getServletContext().getEffectiveSessionTrackingModes();
        assertFalse(modes.contains(SessionTrackingMode.URL), "Session tracking mode must NOT include URL to prevent ;jsessionid in paths");

        // 2. Verify rendered content on /products has no ;jsessionid
        String productsContent = mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(productsContent.contains(";jsessionid="), "Rendered /products page must never contain ;jsessionid=");
    }

    @Test
    void testCategoryNavigationLayoutIntegrity() throws Exception {
        String productsContent = mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Global nav container must not constrain flyout with position-relative
        assertFalse(productsContent.contains("apple-global-nav p-0\">\n    <div class=\"container-xl d-flex align-items-center justify-content-between h-100 position-relative\">"),
                "Global nav container-xl must not have position-relative which confines flyout");

        // Sub-nav on products catalog page must not contain clumsy wrapping dropdown menus that break 52px frosted bar
        assertFalse(productsContent.contains("subnav-category-dropdown"),
                "Sub-nav on products page must not have clumsy wrapping dropdown menus");
    }

    @Test
    void testProductsCatalogPageRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void testProductDetailPageRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/product/iphone-16-pro"))
                .andExpect(status().isOk());
    }

    @Test
    void testAnonymousAdminAccessRequiresLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void testAnonymousAdminProductsAccessRequiresLogin() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void testCustomerLoginPageRendersAppleDesign() throws Exception {
        String content = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("auth.css"), "Customer login must include auth.css");
        assertTrue(content.contains("Đăng nhập IUH ID"), "Customer login must include IUH ID title");
    }

    @Test
    void testCustomerLoginSubmitRedirectsToHome() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "quan.nguyen@gmail.com")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testCustomerRegisterPageRendersAppleDesign() throws Exception {
        String content = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("auth.css"), "Customer register must include auth.css");
        assertTrue(content.contains("Tạo tài khoản IUH Store"), "Customer register must include title");
    }

    @Test
    void testCustomerRegisterSubmitRedirectsToHome() throws Exception {
        String testEmail = "test_home_reg_" + System.currentTimeMillis() + "@gmail.com";
        mockMvc.perform(post("/register")
                        .param("name", "Nguyễn Minh Test")
                        .param("email", testEmail)
                        .param("password", "Apple2026@")
                        .param("confirmPassword", "Apple2026@")
                        .param("phone", "0987654321"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testAdminLoginPageRendersAppleConsoleDesign() throws Exception {
        String content = mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("auth.css"), "Admin login must include auth.css");
        assertTrue(content.contains("IUH Store Admin Workspace"), "Admin login must include Workspace header");
    }

    @Test
    void testEmptyAdminLoginShowsError() throws Exception {
        mockMvc.perform(post("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("admin/auth/login"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("errorMessage"));
    }
}
