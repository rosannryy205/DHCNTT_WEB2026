package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.entity.*;
import com.example.dhcnttweb2026.repository.*;
import com.example.dhcnttweb2026.service.AdminOrderWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminOrderConcurrencyTest {
    @Autowired OrderRepository orders;
    @Autowired ProductVariantRepository variants;
    @Autowired ProductRepository products;
    @Autowired UserRepository users;
    @Autowired AdminOrderWorkflowService workflow;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void simultaneousCancellationsRestoreStockOnlyOnce() throws Exception {
        var tx = new TransactionTemplate(transactions);
        String code = "TEST-CANCEL-" + UUID.randomUUID();
        // Dedicated records isolate stock changes from the store's existing variants.
        Long[] ids = tx.execute(status -> {
            var variant = new ProductVariant(products.findAll().get(0), code, "Test", "Test", "Test",
                    BigDecimal.TEN, BigDecimal.TEN, 10, null);
            variants.save(variant);
            var order = new Order(users.findAll().get(0), code, new BigDecimal("20"),
                    Order.PaymentMethod.cod, Order.PaymentStatus.PENDING, Order.OrderStatus.Processing,
                    "Test address", "0900000000", "Concurrency test");
            order.getOrderItems().add(new OrderItem(order, variant, 2, BigDecimal.TEN, new BigDecimal("20")));
            orders.saveAndFlush(order);
            return new Long[]{order.getId(), variant.getId()};
        });
        var workers = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try {
            Callable<Integer> cancel = () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new AssertionError("Workers did not start");
                try {
                    workflow.changeStatus(code, new AdminOrderWorkflowService.StatusChange(
                            Order.OrderStatus.Processing, Order.OrderStatus.Cancelled, "Khách yêu cầu"));
                    return 200;
                } catch (ResponseStatusException ex) {
                    return ex.getStatusCode().value();
                }
            };
            var first = workers.submit(cancel);
            var second = workers.submit(cancel);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            var responses = List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
            assertEquals(1, responses.stream().filter(value -> value == 200).count());
            assertEquals(1, responses.stream().filter(value -> value == 409).count());
            tx.executeWithoutResult(status -> {
                assertEquals(12, variants.findById(ids[1]).orElseThrow().getStock());
                var stored = orders.findById(ids[0]).orElseThrow();
                assertEquals(Order.OrderStatus.Cancelled, stored.getOrderStatus());
                assertEquals(Order.PaymentStatus.PENDING, stored.getPaymentStatus());
                assertEquals("Khách yêu cầu", stored.getCancellationReason());
            });
        } finally {
            start.countDown();
            workers.shutdownNow();
            workers.awaitTermination(25, TimeUnit.SECONDS);
            tx.executeWithoutResult(status -> {
                orders.deleteById(ids[0]);
                orders.flush();
                variants.deleteById(ids[1]);
            });
        }
    }
}
