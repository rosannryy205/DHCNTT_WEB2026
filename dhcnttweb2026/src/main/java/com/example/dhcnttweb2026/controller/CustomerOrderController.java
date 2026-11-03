package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerOrderDetailDto;
import com.example.dhcnttweb2026.dto.CustomerOrderSummaryDto;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.service.CustomerOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@SuppressWarnings("null")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    public CustomerOrderController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @GetMapping("/orders")
    public String showOrderHistory(@RequestParam(name = "status", required = false, defaultValue = "all") String status,
                                   HttpSession session,
                                   Model model) {
        CustomerSessionDto customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login?returnUrl=/orders";
        }

        List<CustomerOrderSummaryDto> orders = customerOrderService.getCustomerOrders(customer.getId(), status);

        model.addAttribute("orders", orders);
        model.addAttribute("currentFilter", status != null ? status.toLowerCase() : "all");
        model.addAttribute("pageTitle", "Đơn hàng của tôi - IUH Store");

        return "client/orders";
    }

    @GetMapping("/orders/{orderCode}")
    public String showOrderDetail(@PathVariable String orderCode,
                                  HttpSession session,
                                  Model model) {
        CustomerSessionDto customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login?returnUrl=/orders/" + orderCode;
        }

        CustomerOrderDetailDto order = customerOrderService.getCustomerOrderDetail(orderCode, customer.getId());

        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Đơn hàng " + orderCode + " - IUH Store");

        return "client/order-detail";
    }

    @PostMapping("/orders/{orderCode}/cancel")
    public String cancelOrder(@PathVariable String orderCode,
                              @RequestParam(name = "cancellationReason", required = false) String cancellationReason,
                              @RequestParam(name = "redirectSource", required = false, defaultValue = "detail") String redirectSource,
                              HttpSession session,
                              RedirectAttributes ra) {
        CustomerSessionDto customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login?returnUrl=/orders";
        }

        try {
            customerOrderService.cancelCustomerOrder(orderCode, customer.getId(), cancellationReason);
            ra.addFlashAttribute("successMessage", "Đã hủy đơn hàng " + orderCode + " thành công. Số lượng sản phẩm đã được hoàn trả về kho.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng: " + e.getMessage());
        }

        if ("list".equalsIgnoreCase(redirectSource)) {
            return "redirect:/orders";
        }
        return "redirect:/orders/" + orderCode;
    }

    private CustomerSessionDto getLoggedInCustomer(HttpSession session) {
        if (session == null) return null;
        Object obj = session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY);
        if (obj instanceof CustomerSessionDto dto) {
            return dto;
        }
        return null;
    }
}
