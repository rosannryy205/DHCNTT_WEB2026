package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.config.CustomerAuthInterceptor;
import com.example.dhcnttweb2026.dto.CustomerPasswordChangeDto;
import com.example.dhcnttweb2026.dto.CustomerProfileDto;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.service.CustomerProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SuppressWarnings("null")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    public CustomerProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model) {
        CustomerSessionDto customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login?returnUrl=/profile";
        }

        CustomerProfileDto profileDto = customerProfileService.getProfile(customer.getId());
        model.addAttribute("profileDto", profileDto);
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new CustomerPasswordChangeDto());
        }
        model.addAttribute("pageTitle", "Hồ sơ của tôi - IUH Store");
        return "client/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute CustomerProfileDto profileDto,
                                HttpSession session,
                                RedirectAttributes ra) {
        CustomerSessionDto customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login?returnUrl=/profile";
        }

        try {
            customerProfileService.updateProfile(customer.getId(), profileDto);

            // Update session DTO so global nav updates immediately
            if (profileDto.getName() != null && !profileDto.getName().isBlank()) {
                customer.setName(profileDto.getName().strip());
            }
            if (profileDto.getPhone() != null) {
                customer.setPhone(profileDto.getPhone().strip());
            }
            session.setAttribute(CustomerAuthInterceptor.CUSTOMER_SESSION_KEY, customer);

            ra.addFlashAttribute("successMessage", "Thông tin hồ sơ đã được cập nhật thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể cập nhật hồ sơ: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute CustomerPasswordChangeDto passwordDto,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        CustomerSessionDto customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login?returnUrl=/profile";
        }

        try {
            customerProfileService.changePassword(customer.getId(), passwordDto);
            ra.addFlashAttribute("passwordSuccessMessage", "Đổi mật khẩu thành công. Vui lòng sử dụng mật khẩu mới cho các lần đăng nhập tiếp theo.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("passwordErrorMessage", e.getMessage());
            ra.addFlashAttribute("passwordDto", passwordDto);
        } catch (Exception e) {
            ra.addFlashAttribute("passwordErrorMessage", "Đã xảy ra lỗi khi đổi mật khẩu: " + e.getMessage());
        }

        return "redirect:/profile";
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
