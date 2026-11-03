package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.service.AdminOrderWorkflowService;
import com.example.dhcnttweb2026.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
public class AdminOrderStatusController {
    private final AdminOrderWorkflowService workflow;

    public AdminOrderStatusController(AdminOrderWorkflowService workflow) {
        this.workflow = workflow;
    }

    @PostMapping("/admin/orders/{code}/status")
    public AdminOrderWorkflowService.Result update(@PathVariable String code,
            @RequestBody AdminOrderWorkflowService.StatusChange change,
            @RequestHeader(name = "X-Order-CSRF", required = false) String csrf,
            HttpSession session) {
        if (session.getAttribute("ADMIN_USER") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập lại.");
        }
        Object role = session.getAttribute("ADMIN_ROLE");
        if (role != User.Role.admin && role != User.Role.staff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ Admin hoặc Staff được thao tác đơn hàng.");
        }
        if (csrf == null || !csrf.equals(session.getAttribute("ORDER_CSRF"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Phiên thao tác không hợp lệ. Vui lòng tải lại trang.");
        }
        return workflow.changeStatus(code, change);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> rejected(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", ex.getReason()));
    }
}
