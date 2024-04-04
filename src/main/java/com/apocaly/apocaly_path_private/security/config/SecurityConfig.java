package com.apocaly.apocaly_path_private.security.config;

import com.apocaly.apocaly_path_private.security.filter.JwtAuthenticationFilter;
import com.apocaly.apocaly_path_private.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity // 스프링 시큐리티 활성화
public class SecurityConfig {
    private UserDetailsService userDetailsService;

    private TokenService tokenService;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // CSRF 보호기능 비활성화 REST API 구축할땐 일반적으로 비활성화
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            // 인증 규칙 정의 .requestMatchers("/api/auth/**").permitAll() /api/auth/** 패턴에 인증없이 접근 가능
            // .anyRequest().authenticated() 위에서 정의한 패턴을 제외한 모든 요청에 대해 인증 요구
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            // 세션 관리 정책 설정 세션 사용 안함

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(userDetailsService,tokenService);
    }
}
