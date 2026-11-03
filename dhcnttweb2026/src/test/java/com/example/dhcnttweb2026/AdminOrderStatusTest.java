package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.controller.AdminOrderStatusController;
import com.example.dhcnttweb2026.entity.Order;
import com.example.dhcnttweb2026.repository.OrderRepository;
import com.example.dhcnttweb2026.repository.ProductVariantRepository;
import com.example.dhcnttweb2026.service.AdminOrderWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminOrderStatusTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "cod,PENDING,200", "cod,PAID,200", "cod,FAILED,409", "cod,REFUNDED,409",
        "vnpay,PENDING,409", "vnpay,PAID,200", "vnpay,FAILED,409", "vnpay,REFUNDED,409",
        "momo,PENDING,409", "momo,PAID,200", "momo,FAILED,409", "momo,REFUNDED,409"
    })
    void enforcesPaymentMethodAtEveryApprovalStep(Order.PaymentMethod method,
            Order.PaymentStatus payment, int responseStatus) throws Exception {
        for (String[] step : new String[][] {
                {"PENDING", "PROCESSING"}, {"PROCESSING", "SHIPPED"}, {"SHIPPED", "DELIVERED"}}) {
            var orders = mock(OrderRepository.class);
            var variants = mock(ProductVariantRepository.class);
            var order = new Order();
            var initial = switch (step[0]) {
                case "PENDING" -> Order.OrderStatus.Pending;
                case "PROCESSING" -> Order.OrderStatus.Processing;
                default -> Order.OrderStatus.Shipped;
            };
            order.setOrderStatus(initial);
            order.setPaymentMethod(method);
            order.setPaymentStatus(payment);
            when(orders.findByOrderCodeForUpdate("ORDER-1")).thenReturn(Optional.of(order));
            var mvc = MockMvcBuilders.standaloneSetup(new AdminOrderStatusController(
                    new AdminOrderWorkflowService(orders, variants))).build();
            var response = mvc.perform(post("/admin/orders/ORDER-1/status")
                    .sessionAttr("ADMIN_USER", "Staff")
                    .sessionAttr("ADMIN_ROLE", com.example.dhcnttweb2026.entity.User.Role.staff)
                    .sessionAttr("ORDER_CSRF", "token").header("X-Order-CSRF", "token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"expectedStatus\":\"" + step[0] + "\",\"status\":\"" + step[1] + "\"}"))
                    .andExpect(status().is(responseStatus));
            if (responseStatus == 200) {
                response.andExpect(jsonPath("$.status").value(step[1]));
            } else {
                org.junit.jupiter.api.Assertions.assertEquals(initial, order.getOrderStatus());
            }
            org.junit.jupiter.api.Assertions.assertEquals(payment, order.getPaymentStatus());
            verifyNoInteractions(variants);
        }
    }

    @Test
    void rejectsMissingCustomerOrUntypedRoleBeforeReadingOrder() throws Exception {
        var orders = mock(OrderRepository.class);
        var variants = mock(ProductVariantRepository.class);
        var mvc = MockMvcBuilders.standaloneSetup(new AdminOrderStatusController(
                new AdminOrderWorkflowService(orders, variants))).build();
        for (Object role : new Object[] {null, com.example.dhcnttweb2026.entity.User.Role.customer, "admin"}) {
            var session = new org.springframework.mock.web.MockHttpSession();
            session.setAttribute("ADMIN_USER", "Admin");
            session.setAttribute("ADMIN_ROLE", role);
            session.setAttribute("ORDER_CSRF", "token");
            for (String target : new String[] {"PROCESSING", "CANCELLED"}) {
                mvc.perform(post("/admin/orders/ORDER-1/status").session(session)
                        .header("X-Order-CSRF", "token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expectedStatus\":\"PENDING\",\"status\":\"" + target + "\"}"))
                        .andExpect(status().isForbidden());
            }
        }
        verifyNoInteractions(orders, variants);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "Pending,PENDING,PROCESSING,PENDING,200",
        "Processing,PROCESSING,SHIPPED,PAID,200",
        "Shipped,SHIPPED,DELIVERED,PENDING,200",
        "Pending,PENDING,SHIPPED,PENDING,409",
        "Processing,PROCESSING,PENDING,PENDING,409",
        "Processing,PENDING,PROCESSING,PENDING,409",
        "Delivered,DELIVERED,PROCESSING,PAID,409",
        "Cancelled,CANCELLED,PROCESSING,PENDING,409",
        "Pending,PENDING,PROCESSING,FAILED,409",
        "Pending,PENDING,PROCESSING,REFUNDED,409",
        "Pending,PENDING,CANCELLED,PAID,409",
        "Processing,PROCESSING,CANCELLED,PAID,409",
        "Shipped,SHIPPED,CANCELLED,PENDING,409",
        "Delivered,DELIVERED,CANCELLED,PENDING,409",
        "Pending,PENDING,CANCELLED,FAILED,409",
        "Pending,PENDING,UNKNOWN,PENDING,400"
    })
    void enforcesTransitionAndPaymentRules(String actual, String expected, String target,
                                           String payment, int responseStatus) throws Exception {
        var orders = mock(OrderRepository.class);
        var variants = mock(ProductVariantRepository.class);
        var order = new Order();
        order.setOrderStatus(Order.OrderStatus.valueOf(actual));
        order.setPaymentStatus(Order.PaymentStatus.valueOf(payment));
        when(orders.findByOrderCodeForUpdate("ORDER-1")).thenReturn(Optional.of(order));
        var mvc = MockMvcBuilders.standaloneSetup(new AdminOrderStatusController(
                new AdminOrderWorkflowService(orders, variants))).build();
        mvc.perform(post("/admin/orders/ORDER-1/status")
                .sessionAttr("ADMIN_USER", "Staff").sessionAttr("ADMIN_ROLE", com.example.dhcnttweb2026.entity.User.Role.staff).sessionAttr("ORDER_CSRF", "token")
                .header("X-Order-CSRF", "token").contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedStatus\":\"" + expected + "\",\"status\":\"" + target
                        + "\",\"reason\":\"Khách yêu cầu\"}"))
                .andExpect(status().is(responseStatus));
        org.junit.jupiter.api.Assertions.assertEquals(Order.PaymentStatus.valueOf(payment), order.getPaymentStatus());
        if (responseStatus != 200) {
            org.junit.jupiter.api.Assertions.assertEquals(Order.OrderStatus.valueOf(actual), order.getOrderStatus());
        }
        verifyNoInteractions(variants);
    }

    @Test
    void rejectsAnonymousOrForgedRequestsBeforeReadingOrder() throws Exception {
        var orders = mock(OrderRepository.class);
        var variants = mock(ProductVariantRepository.class);
        var mvc = MockMvcBuilders.standaloneSetup(new AdminOrderStatusController(
                new AdminOrderWorkflowService(orders, variants))).build();
        String body = "{\"expectedStatus\":\"PENDING\",\"status\":\"PROCESSING\"}";
        mvc.perform(post("/admin/orders/ORDER-1/status").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/admin/orders/ORDER-1/status").sessionAttr("ADMIN_USER", "Admin").sessionAttr("ADMIN_ROLE", com.example.dhcnttweb2026.entity.User.Role.admin)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        verifyNoInteractions(orders, variants);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "   "})
    void requiresCancellationReason(String reason) throws Exception {
        var orders = mock(OrderRepository.class);
        var variants = mock(ProductVariantRepository.class);
        var order = new Order();
        when(orders.findByOrderCodeForUpdate("ORDER-1")).thenReturn(Optional.of(order));
        var mvc = MockMvcBuilders.standaloneSetup(new AdminOrderStatusController(
                new AdminOrderWorkflowService(orders, variants))).build();
        mvc.perform(post("/admin/orders/ORDER-1/status")
                .sessionAttr("ADMIN_USER", "Admin").sessionAttr("ADMIN_ROLE", com.example.dhcnttweb2026.entity.User.Role.admin).sessionAttr("ORDER_CSRF", "token")
                .header("X-Order-CSRF", "token").contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedStatus\":\"PENDING\",\"status\":\"CANCELLED\",\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isBadRequest());
        org.junit.jupiter.api.Assertions.assertEquals(Order.OrderStatus.Pending, order.getOrderStatus());
        verifyNoInteractions(variants);
    }

    @Test
    void cancellationRestoresStockOnceAndKeepsReason() throws Exception {
        var orders = mock(OrderRepository.class);
        var variants = mock(ProductVariantRepository.class);
        var order = new Order();
        order.setOrderCode("ORDER-1");
        var variant = new com.example.dhcnttweb2026.entity.ProductVariant();
        variant.setId(7L);
        order.getOrderItems().add(new com.example.dhcnttweb2026.entity.OrderItem(
                order, variant, 2, java.math.BigDecimal.TEN, new java.math.BigDecimal("20")));
        when(orders.findByOrderCodeForUpdate("ORDER-1")).thenReturn(Optional.of(order));
        when(variants.restoreStock(7L, 2)).thenReturn(1);
        var mvc = MockMvcBuilders.standaloneSetup(new AdminOrderStatusController(
                new AdminOrderWorkflowService(orders, variants))).build();
        var request = "{\"expectedStatus\":\"PENDING\",\"status\":\"CANCELLED\",\"reason\":\"Khách yêu cầu\"}";
        mvc.perform(post("/admin/orders/ORDER-1/status")
                .sessionAttr("ADMIN_USER", "Admin").sessionAttr("ADMIN_ROLE", com.example.dhcnttweb2026.entity.User.Role.admin).sessionAttr("ORDER_CSRF", "token")
                .header("X-Order-CSRF", "token").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Khách yêu cầu"));
        mvc.perform(post("/admin/orders/ORDER-1/status")
                .sessionAttr("ADMIN_USER", "Admin").sessionAttr("ADMIN_ROLE", com.example.dhcnttweb2026.entity.User.Role.admin).sessionAttr("ORDER_CSRF", "token")
                .header("X-Order-CSRF", "token").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
        verify(variants, times(1)).restoreStock(7L, 2);
    }

    @Test
    void approvesPendingOrderWithoutChangingPaymentOrStock() throws Exception {
        var orders = mock(OrderRepository.class);
        var variants = mock(ProductVariantRepository.class);
        var order = new Order();
        order.setOrderCode("ORDER-1");
        when(orders.findByOrderCodeForUpdate("ORDER-1")).thenReturn(Optional.of(order));
        var mvc = MockMvcBuilders.standaloneSetup(new AdminOrderStatusController(
                new AdminOrderWorkflowService(orders, variants))).build();
        mvc.perform(post("/admin/orders/ORDER-1/status")
                .sessionAttr("ADMIN_USER", "Staff").sessionAttr("ADMIN_ROLE", com.example.dhcnttweb2026.entity.User.Role.staff).sessionAttr("ORDER_CSRF", "token")
                .header("X-Order-CSRF", "token").contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedStatus\":\"PENDING\",\"status\":\"PROCESSING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.payment").value("PENDING"));
        verifyNoInteractions(variants);
    }
}
