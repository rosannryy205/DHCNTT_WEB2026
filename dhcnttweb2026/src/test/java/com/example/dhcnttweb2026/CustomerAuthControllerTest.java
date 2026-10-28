package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@SuppressWarnings("null")
class CustomerAuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();
    }

    @Test
    void testShowLoginPageRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/auth/login"))
                .andExpect(model().attributeExists("loginDto"));
    }

    @Test
    void testShowRegisterPageRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/auth/register"))
                .andExpect(model().attributeExists("registerDto"));
    }

    @Test
    void testUnauthenticatedBagAccessRedirectsToLogin() throws Exception {
        // Kiểm tra ADR-0004: Bắt buộc đăng nhập để truy cập /bag
        mockMvc.perform(get("/bag"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?returnUrl=%2Fbag"));
    }

    @Test
    void testCustomerRegistrationSuccessAndAutoLogin() throws Exception {
        String testEmail = "test_customer_" + System.currentTimeMillis() + "@gmail.com";

        mockMvc.perform(post("/register")
                        .param("name", "Nguyễn Văn Test")
                        .param("email", testEmail)
                        .param("password", "Apple2026@")
                        .param("confirmPassword", "Apple2026@")
                        .param("phone", "0987111222")
                        .param("returnUrl", "/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("successMessage"));

        // Kiểm tra đã lưu vào DB
        assertTrue(userRepository.findByEmail(testEmail).isPresent(), "User phải được lưu vào cơ sở dữ liệu");
        User user = userRepository.findByEmail(testEmail).orElseThrow();
        assertEquals(User.Role.customer, user.getRole());
        assertNotEquals("Apple2026@", user.getPassword(), "Mật khẩu phải được băm bảo mật BCrypt");
    }

    @Test
    void testCustomerRegistrationFailsWhenPasswordMismatch() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "Lê Văn Lỗi")
                        .param("email", "mismatch@example.com")
                        .param("password", "Apple2026@")
                        .param("confirmPassword", "WrongPass123")
                        .param("phone", "0987111222"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void testCustomerLoginSuccessWithReturnUrl() throws Exception {
        // Đăng ký một user kiểm thử
        String testEmail = "login_test_" + System.currentTimeMillis() + "@gmail.com";
        mockMvc.perform(post("/register")
                        .param("name", "Trần Đăng Nhập")
                        .param("email", testEmail)
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123")
                        .param("phone", "0912000333"))
                .andExpect(status().is3xxRedirection());

        // Thực hiện đăng nhập
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(session)
                        .param("email", testEmail)
                        .param("password", "Secret123")
                        .param("returnUrl", "/bag"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bag"));

        assertNotNull(session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY), "Session phải chứa CUSTOMER_USER");
        CustomerSessionDto sessionDto = (CustomerSessionDto) session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY);
        assertNotNull(sessionDto);
        assertEquals(testEmail, Objects.requireNonNull(sessionDto).getEmail());
    }

    @Test
    void testCustomerLoginFailsWithWrongPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "nonexistent@example.com")
                        .param("password", "wrongpass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/auth/login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void testCustomerLogoutClearsSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY,
                new CustomerSessionDto(999L, "Test User", "test@example.com", "0900000000"));

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));

        assertNull(session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY), "Session CUSTOMER_USER phải bị xóa khi logout");
    }
}
