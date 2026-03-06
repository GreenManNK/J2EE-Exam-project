package com.example.hutech.config;

import com.example.hutech.model.User;
import com.example.hutech.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            session.setAttribute("redirectAfterLogin", request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        User user = userService.getUserById(userId).orElse(null);
        if (user == null || !user.isActive()) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        if (!userService.isAdmin(user)) {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        session.setAttribute("isAdmin", true);
        return true;
    }
}
