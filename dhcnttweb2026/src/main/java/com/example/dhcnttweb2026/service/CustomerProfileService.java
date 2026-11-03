package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.dto.CustomerPasswordChangeDto;
import com.example.dhcnttweb2026.dto.CustomerProfileDto;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@SuppressWarnings("null")
public class CustomerProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public CustomerProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tài khoản."));
        return new CustomerProfileDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress()
        );
    }

    @Transactional
    public void updateProfile(Long userId, CustomerProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tài khoản."));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().strip());
        }
        user.setPhone(dto.getPhone() != null ? dto.getPhone().strip() : null);
        user.setAddress(dto.getAddress() != null ? dto.getAddress().strip() : null);

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, CustomerPasswordChangeDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu đổi mật khẩu không hợp lệ.");
        }
        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại.");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới.");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không trùng khớp.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tài khoản."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
