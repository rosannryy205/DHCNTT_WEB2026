package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.service.CustomerAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.example.dhcnttweb2026.controller")
public class GlobalClientAdvice {

    private final CustomerAuthService customerAuthService;

    public GlobalClientAdvice(CustomerAuthService customerAuthService) {
        this.customerAuthService = customerAuthService;
    }

    @ModelAttribute("currentCustomer")
    public CustomerSessionDto populateCurrentCustomer(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object customerObj = session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY);
        if (customerObj instanceof CustomerSessionDto customer) {
            return customer;
        }
        return null;
    }

    @ModelAttribute("bagItemCount")
    public int populateBagItemCount(HttpSession session) {
        if (session == null) {
            return 0;
        }
        Object customerObj = session.getAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY);
        if (customerObj instanceof CustomerSessionDto customer) {
            return customerAuthService.getBagItemCount(customer.getId());
        }
        return 0;
    }
}
