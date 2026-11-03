package com.example.dhcnttweb2026.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionTrackingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.Objects;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @NonNull
    private final CustomerAuthInterceptor customerAuthInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;

    public WebConfig(@NonNull CustomerAuthInterceptor customerAuthInterceptor,
                     AdminAuthInterceptor adminAuthInterceptor) {
        this.customerAuthInterceptor = Objects.requireNonNull(customerAuthInterceptor);
        this.adminAuthInterceptor = Objects.requireNonNull(adminAuthInterceptor);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Disable URL-based session rewriting (;jsessionid=...) completely.
     * Enforce cookie-only session tracking for security and clean Apple URL structure.
     */
    @Autowired
    public void configureSessionTracking(ServletContext servletContext) {
        servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin", "/admin/", "/admin/**")
                .excludePathPatterns("/admin/login", "/admin/logout");
        // Bảo vệ các route yêu cầu xác thực Customer (bắt buộc đăng nhập để mua hàng theo ADR-0004)
        registry.addInterceptor(customerAuthInterceptor)
                .addPathPatterns("/bag/**", "/orders/**", "/profile/**", "/checkout/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**");
    }
}
