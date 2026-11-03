package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.controller.AdminController;
import com.example.dhcnttweb2026.entity.*;
import com.example.dhcnttweb2026.repository.OrderRepository;
import com.example.dhcnttweb2026.service.AdminOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminOrderRenderingTest {
    @Test
    void readsStoredOrderPricesStatusesAndShippingDetails() {
        var repository = mock(OrderRepository.class);
        var order = order();
        when(repository.findAllWithDetails()).thenReturn(List.of(order));

        var dto = new AdminOrderService(repository).findAll().get(0);

        assertEquals("DB-100", dto.code());
        assertEquals("0909999999", dto.phone());
        assertEquals("Địa chỉ giao hàng", dto.address());
        assertEquals("SHIPPED", dto.status());
        assertEquals("PAID", dto.payment());
        assertEquals("VNPay", dto.method());
        assertEquals("2026-10-30T10:42+07:00", dto.date());
        assertEquals(new BigDecimal("20000000"), dto.subtotal());
        assertEquals(new BigDecimal("-500000"), dto.adjustment());
        assertEquals(new BigDecimal("19500000"), dto.total());
        assertEquals(new BigDecimal("10000000"), dto.items().get(0).price());
        assertEquals(2, dto.items().get(0).quantity());
        assertEquals("Đen · 8GB · 256GB", dto.items().get(0).variant());
        verify(repository).findAllWithDetails();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rendersBothRoutesWithSafelySerializedDatabaseContent() throws Exception {
        var repository = mock(OrderRepository.class);
        var order = order();
        order.setNote("</script><script>window.injected=true</script>");
        when(repository.findAllWithDetails()).thenReturn(List.of(order));
        var mvc = mvc(new AdminOrderService(repository));
        for (String route : List.of("/admin/orders", "/admin/orders/")) {
            var response = mvc.perform(get(route).sessionAttr("ADMIN_USER", "Admin")).andExpect(status().isOk())
                    .andExpect(view().name("admin/order-list"))
                    .andExpect(model().attribute("activeMenu", "orders"))
                    .andReturn().getResponse();
            var html = response.getContentAsString(StandardCharsets.UTF_8);
            assertTrue(html.contains("DB-100"));
            assertTrue(html.contains("window.adminOrders = [{"));
            assertFalse(html.contains("</script><script>window.injected"));
            assertFalse(html.contains("Dữ liệu mẫu"));
        }
    }

    @Test
    void emptyDatabaseRendersEmptySnapshot() throws Exception {
        var repository = mock(OrderRepository.class);
        when(repository.findAllWithDetails()).thenReturn(List.of());
        var html = mvc(new AdminOrderService(repository)).perform(get("/admin/orders").sessionAttr("ADMIN_USER", "Admin"))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("window.adminOrders = [];"));
    }

    @Test
    void anonymousRequestsCannotReadOrderData() throws Exception {
        var repository = mock(OrderRepository.class);
        var mvc = mvc(new AdminOrderService(repository));
        for (String route : List.of("/admin/orders", "/admin/orders/")) {
            mvc.perform(get(route)).andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/login"));
        }
        verifyNoInteractions(repository);
    }

    private org.springframework.test.web.servlet.MockMvc mvc(AdminOrderService service) {
        var templates = new FileTemplateResolver();
        templates.setPrefix("src/main/resources/templates/");
        templates.setSuffix(".html");
        templates.setTemplateMode("HTML");
        templates.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        return MockMvcBuilders.standaloneSetup(new AdminController(null, null, null, null, service))
                .setViewResolvers(views).build();
    }

    private Order order() {
        var user = new User();
        user.setName("Khách hàng DB");
        user.setEmail("db@example.com");
        var product = new Product();
        product.setName("Thiết bị DB");
        var variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku("DB-SKU");
        variant.setColor("Đen");
        variant.setRam("8GB");
        variant.setStorage("256GB");
        variant.setPrice(new BigDecimal("99000000"));
        var order = new Order(user, "DB-100", new BigDecimal("19500000"),
                Order.PaymentMethod.vnpay, Order.PaymentStatus.PAID, Order.OrderStatus.Shipped,
                "Địa chỉ giao hàng", "0909999999", "Gọi trước khi giao");
        order.setCreatedAt(LocalDateTime.of(2026, 10, 30, 10, 42));
        order.setOrderItems(List.of(new OrderItem(order, variant, 2,
                new BigDecimal("10000000"), new BigDecimal("20000000"))));
        return order;
    }
}
