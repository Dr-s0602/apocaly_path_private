package com.apocaly.apocaly_path_private.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins("http://localhost:3005") // 이 오리진을 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS") // 허용할 HTTP 메소드 지정
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 인증정보와 함께 요청 허용
    }
}
