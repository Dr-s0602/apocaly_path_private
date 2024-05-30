package com.apocaly.apocaly_path_private.security.config;


// Spring Security와 관련된 클래스들을 import 합니다.
import com.apocaly.apocaly_path_private.security.handler.CustomLogoutHandler;
import com.apocaly.apocaly_path_private.security.jwt.filter.JWTFilter;
import com.apocaly.apocaly_path_private.security.jwt.filter.LoginFilter;
import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;


// Spring Framework 설정 관련 클래스들을 import 합니다.
import com.apocaly.apocaly_path_private.security.service.RefreshService;
import com.apocaly.apocaly_path_private.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration // 스프링의 설정 정보를 담는 클래스임을 나타내는 어노테이션입니다.
@EnableWebSecurity // 스프링 시큐리티 설정을 활성화합니다.
public class SecurityConfig {
    private final UserService userService;
    private final RefreshService refreshService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    // 생성자를 통한 의존성 주입으로, 필요한 서비스와 설정을 초기화합니다.
    public SecurityConfig(UserService userService, RefreshService refreshService, AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.userService = userService;
        this.refreshService = refreshService;
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    // 인증 관리자를 스프링 컨테이너에 Bean으로 등록합니다. 인증 과정에서 중요한 역할을 합니다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // HTTP 보안 관련 설정을 정의합니다.
    // SecurityFilterChain Bean을 등록하여 HTTP 요청에 대한 보안을 구성합니다.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF, Form Login, Http Basic 인증을 비활성화합니다.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // HTTP 요청에 대한 접근 권한을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/notice").hasRole("ADMIN") // '/notice' 경로에 대한 POST 요청은 ADMIN 역할을 가진 사용자만 가능합니다.
                        .requestMatchers("/api/auth/user", "/login", "/notice", "/reissue", "/auth/kakao/callback","/auth/kakao/signup/callback","/file/view/*","/file/download/*").permitAll() // 해당 경로들은 인증 없이 접근 가능합니다.
                        .anyRequest().authenticated()) // 그 외의 모든 요청은 인증을 요구합니다.
                // JWTFilter와 LoginFilter를 필터 체인에 등록합니다.
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                .addFilterAt(new LoginFilter(userService, refreshService, authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)
                // 로그아웃 처리를 커스터마이징합니다.
                .logout(logout -> logout
                        .addLogoutHandler(new CustomLogoutHandler(jwtUtil, refreshService, userService))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                // 세션 정책을 STATELESS로 설정하여, 세션을 사용하지 않는다는 것을 명시합니다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
