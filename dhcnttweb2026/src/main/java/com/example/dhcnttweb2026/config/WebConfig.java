package com.example.dhcnttweb2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @NonNull
    private final CustomerAuthInterceptor customerAuthInterceptor;

    public WebConfig(@NonNull CustomerAuthInterceptor customerAuthInterceptor) {
        this.customerAuthInterceptor = Objects.requireNonNull(customerAuthInterceptor);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Bảo vệ các route yêu cầu xác thực Customer (bắt buộc đăng nhập để mua hàng theo ADR-0004)
        registry.addInterceptor(customerAuthInterceptor)
                .addPathPatterns("/bag/**", "/orders/**", "/profile/**", "/checkout/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**");
    }
}
