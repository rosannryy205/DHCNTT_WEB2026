package com.example.dhcnttweb2026.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @NonNull
    private final HandlerInterceptor adminAuthInterceptor;

    public WebConfig(@NonNull AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = Objects.requireNonNull(adminAuthInterceptor);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/css/**", "/js/**", "/images/**");
    }
}
