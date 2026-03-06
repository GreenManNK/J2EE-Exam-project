package com.example.hutech.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthorizationInterceptor adminAuthorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthorizationInterceptor)
                .addPathPatterns(
                        "/admin/**",
                        "/products/add",
                        "/products/edit/**",
                        "/products/delete/**",
                        "/categories/add",
                        "/categories/edit/**",
                        "/categories/delete/**",
                        "/orders/add",
                        "/orders/edit/**",
                        "/orders/delete/**");
    }
}
