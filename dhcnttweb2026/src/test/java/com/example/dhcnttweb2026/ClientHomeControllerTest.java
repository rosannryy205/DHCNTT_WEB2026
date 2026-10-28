package com.example.dhcnttweb2026;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

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
    void testAdminAccessibleDirectly() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminProductsAccessibleDirectly() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk());
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
    void testAdminLoginSubmitRedirectsToAdmin() throws Exception {
        mockMvc.perform(post("/admin/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }
}
