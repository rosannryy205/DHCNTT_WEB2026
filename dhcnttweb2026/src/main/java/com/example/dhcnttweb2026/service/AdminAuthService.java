package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdminAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email và mật khẩu.");
        }
        var user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không chính xác."));
        String stored = user.getPassword();
        // Support legacy seed passwords; BCrypt hashes must only be verified by the encoder.
        boolean matches = stored != null && (stored.startsWith("$2")
                ? passwordEncoder.matches(password, stored) : password.equals(stored));
        if (!matches) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không chính xác.");
        }
        if (user.getRole() != User.Role.admin && user.getRole() != User.Role.staff) {
            throw new IllegalArgumentException("Chỉ tài khoản Admin hoặc Staff được truy cập trang quản trị.");
        }
        return user;
    }
}
