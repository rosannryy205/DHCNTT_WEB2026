package com.example.dhcnttweb2026.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomerAuthInterceptor implements HandlerInterceptor {

    public static final String CUSTOMER_SESSION_KEY = "CUSTOMER_USER";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute(CUSTOMER_SESSION_KEY) != null);

        if (!loggedIn) {
            String requestUri = request.getRequestURI();
            String queryString = request.getQueryString();
            String returnUrl = (queryString != null && !queryString.isBlank())
                    ? requestUri + "?" + queryString
                    : requestUri;

            String encodedReturnUrl = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);

            String acceptHeader = request.getHeader("Accept");
            String requestedWith = request.getHeader("X-Requested-With");
            boolean isAjax = (requestedWith != null && "XMLHttpRequest".equalsIgnoreCase(requestedWith))
                    || (acceptHeader != null && acceptHeader.contains("application/json"));

            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"requireLogin\":true,\"redirectUrl\":\"/login?returnUrl=" + encodedReturnUrl + "\",\"message\":\"Vui lòng đăng nhập tài khoản IUH Store để sử dụng Túi hàng và mua sản phẩm.\"}");
                return false;
            }

            // Lưu thông báo yêu cầu đăng nhập vào session để trang login hiển thị
            request.getSession(true).setAttribute("authWarning", "Vui lòng đăng nhập tài khoản IUH Store để sử dụng Túi hàng và mua sản phẩm.");
            response.sendRedirect(request.getContextPath() + "/login?returnUrl=" + encodedReturnUrl);
            return false;
        }
        return true;
    }
}
