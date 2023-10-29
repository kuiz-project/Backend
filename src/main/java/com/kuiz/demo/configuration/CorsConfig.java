package com.kuiz.demo.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // ALLOWED_METHOD_NAMES를 정의하세요.
    private static final String ALLOWED_METHOD_NAMES = "GET,POST,PUT,DELETE,PATCH";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://localhost:3000","https://3.39.190.225:3000") // 리액트 앱의 주소를 추가
                .allowedMethods(ALLOWED_METHOD_NAMES.split(",")) // 허용할 HTTP method
                .allowCredentials(true); // 쿠키 인증 요청 허용
    }
}