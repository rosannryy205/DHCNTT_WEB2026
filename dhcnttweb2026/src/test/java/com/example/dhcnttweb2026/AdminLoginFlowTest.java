package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.config.*;
import com.example.dhcnttweb2026.controller.AdminAuthController;
import com.example.dhcnttweb2026.controller.AdminController;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.repository.UserRepository;
import com.example.dhcnttweb2026.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminLoginFlowTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AdminOrderService orders = mock(AdminOrderService.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        var products = mock(ProductService.class);
        var categories = mock(CategoryService.class);
        when(products.findAll(any(), any())).thenReturn(List.of());
        when(orders.findAll()).thenReturn(List.of());
        var registry = new AccessibleRegistry();
        new WebConfig(mock(CustomerAuthInterceptor.class), new AdminAuthInterceptor()).addInterceptors(registry);
        mvc = MockMvcBuilders.standaloneSetup(
                        new AdminAuthController(new AdminAuthService(users, encoder)),
                        new AdminController(products, categories, null, null, orders))
                .addInterceptors(registry.interceptors()).build();
    }

    @Test
    void successfulAdminLoginMustCreateSessionAcceptedByOrders() throws Exception {
        var session = new MockHttpSession();
        String previousId = session.getId();
        when(users.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(user(User.Role.admin, encoder.encode("test-password"))));
        mvc.perform(post("/admin/login").session(session)
                        .param("username", " ADMIN@example.com ").param("password", "test-password"))
                .andExpect(redirectedUrl("/admin"));
        assertEquals("Quản trị viên", session.getAttribute("ADMIN_USER"));
        assertEquals(User.Role.admin, session.getAttribute("ADMIN_ROLE"));
        assertNotEquals(previousId, session.getId());
        for (String path : List.of("/admin/products", "/admin/categories", "/admin/orders", "/admin/orders/")) {
            mvc.perform(get(path).session(session)).andExpect(status().isOk());
        }
    }

    @Test
    void legacyStaffCredentialsAlsoCreateAdminSession() throws Exception {
        var session = new MockHttpSession();
        when(users.findByEmail("staff@example.com"))
                .thenReturn(Optional.of(user(User.Role.staff, "legacy-password")));
        mvc.perform(post("/admin/login").session(session)
                        .param("username", "staff@example.com").param("password", "legacy-password"))
                .andExpect(redirectedUrl("/admin"));
        mvc.perform(get("/admin/orders").session(session)).andExpect(status().isOk());
        assertEquals(User.Role.staff, session.getAttribute("ADMIN_ROLE"));
    }

    @Test
    void wrongPasswordAndCustomerRoleCannotCreateAdminSession() throws Exception {
        when(users.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(user(User.Role.admin, encoder.encode("correct-password"))));
        when(users.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(user(User.Role.customer, "correct-password")));
        for (String email : List.of("admin@example.com", "customer@example.com", "missing@example.com")) {
            var session = new MockHttpSession();
            mvc.perform(post("/admin/login").session(session).param("username", email)
                            .param("password", email.startsWith("admin") ? "wrong-password" : "correct-password"))
                    .andExpect(view().name("admin/auth/login"))
                    .andExpect(model().attributeExists("errorMessage"));
            assertNull(session.getAttribute("ADMIN_USER"));
            mvc.perform(get("/admin/orders").session(session)).andExpect(redirectedUrl("/admin/login"));
        }
        verifyNoInteractions(orders);
    }

    @Test
    void missingCredentialsCannotBypassLogin() throws Exception {
        mvc.perform(post("/admin/login")).andExpect(view().name("admin/auth/login"))
                .andExpect(model().attributeExists("errorMessage"));
        verifyNoInteractions(users);
    }

    @Test
    void allAdminPagesRequireLoginButLoginPageRemainsAccessible() throws Exception {
        for (String path : List.of("/admin", "/admin/", "/admin/products", "/admin/categories", "/admin/orders", "/admin/orders/")) {
            mvc.perform(get(path)).andExpect(redirectedUrl("/admin/login"));
        }
        mvc.perform(get("/admin/login")).andExpect(status().isOk());
        verifyNoInteractions(orders);
    }

    @Test
    void logoutRemovesAdminAccess() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("ADMIN_USER", "Quản trị viên");
        session.setAttribute("ADMIN_ROLE", User.Role.admin);
        session.setAttribute("ORDER_CSRF", "token");
        mvc.perform(get("/admin/logout").session(session)).andExpect(redirectedUrl("/admin/login"));
        assertNull(session.getAttribute("ADMIN_USER"));
        assertNull(session.getAttribute("ADMIN_ROLE"));
        assertNull(session.getAttribute("ORDER_CSRF"));
        mvc.perform(get("/admin/orders").session(session)).andExpect(redirectedUrl("/admin/login"));
    }

    private User user(User.Role role, String password) {
        return new User("Quản trị viên", "admin@example.com", password, null, null, role);
    }

    private static class AccessibleRegistry extends InterceptorRegistry {
        HandlerInterceptor[] interceptors() {
            return getInterceptors().toArray(HandlerInterceptor[]::new);
        }
    }
}
