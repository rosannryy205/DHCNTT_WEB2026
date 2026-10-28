package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.dto.CustomerLoginDto;
import com.example.dhcnttweb2026.dto.CustomerRegisterDto;
import com.example.dhcnttweb2026.dto.CustomerSessionDto;
import com.example.dhcnttweb2026.entity.Cart;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.repository.CartItemRepository;
import com.example.dhcnttweb2026.repository.CartRepository;
import com.example.dhcnttweb2026.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class CustomerAuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerAuthService(UserRepository userRepository,
                               CartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Xác thực thông tin đăng nhập của Customer.
     * Tự động nâng cấp mật khẩu sang BCrypt nếu người dùng dùng mật khẩu mẫu plain text.
     */
    public CustomerSessionDto login(CustomerLoginDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email.");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu.");
        }

        String email = dto.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không chính xác."));

        // Chỉ cho phép tài khoản customer đăng nhập trên Storefront
        if (user.getRole() != User.Role.customer) {
            throw new IllegalArgumentException("Tài khoản này không thể đăng nhập tại Storefront.");
        }

        boolean matches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        boolean plainMatch = dto.getPassword().equals(user.getPassword());

        if (!matches && !plainMatch) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không chính xác.");
        }

        // Tự động nâng cấp mật khẩu sang BCrypt nếu đang lưu plain text
        if (plainMatch && !matches) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(user);
        }

        // Đảm bảo Customer luôn có một Cart khởi tạo sẵn
        cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        });

        return new CustomerSessionDto(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }

    /**
     * Đăng ký tài khoản Customer mới với validation 2 lớp và khởi tạo sẵn Túi hàng rỗng.
     */
    public CustomerSessionDto register(CustomerRegisterDto dto) {
        validateRegistrationInput(dto);

        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email này đã được đăng ký. Bạn có muốn đăng nhập không?");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        String phone = (dto.getPhone() != null && !dto.getPhone().isBlank()) ? dto.getPhone().trim() : null;

        User newUser = new User(
                dto.getName().trim(),
                email,
                encodedPassword,
                phone,
                null,
                User.Role.customer
        );

        User savedUser = userRepository.save(newUser);

        // Khởi tạo ngay Túi hàng 1-1 cho tài khoản Customer mới
        Cart cart = new Cart(savedUser);
        cartRepository.save(cart);

        return new CustomerSessionDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getPhone());
    }

    /**
     * Lấy tổng số lượng sản phẩm trong Túi hàng của Customer để hiển thị Badge trên Header.
     */
    @Transactional(readOnly = true)
    public int getBagItemCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return 0;
        }
        return cartItemRepository.findByCartId(cartOpt.get().getId())
                .stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
    }

    private void validateRegistrationInput(CustomerRegisterDto dto) {
        if (dto.getName() == null || dto.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Họ và tên phải có ít nhất 2 ký tự.");
        }
        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Địa chỉ email không đúng định dạng.");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có tối thiểu 8 ký tự.");
        }
        boolean hasLetter = dto.getPassword().chars().anyMatch(Character::isLetter);
        boolean hasDigit = dto.getPassword().chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("Mật khẩu phải chứa ít nhất 1 chữ cái và 1 chữ số.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không trùng khớp.");
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            String cleanPhone = dto.getPhone().trim().replaceAll("\\s+", "");
            if (!cleanPhone.matches("^0[0-9]{9}$")) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ (cần đúng 10 số bắt đầu bằng số 0).");
            }
        }
    }
}
