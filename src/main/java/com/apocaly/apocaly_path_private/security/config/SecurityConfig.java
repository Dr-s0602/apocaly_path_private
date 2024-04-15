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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration // 스프링의 설정 클래스임을 나타냅니다.
@EnableWebSecurity // 스프링 시큐리티를 활성화하는 어노테이션입니다.
public class SecurityConfig {
    private UserDetailsService userDetailsService;


    private final UserService userService;
    private final RefreshService refreshService;

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    // 생성자를 통해 AuthenticationConfiguration과 JWTUtil의 의존성을 주입받습니다.
    public SecurityConfig(UserService userService, RefreshService refreshService, AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.userService = userService;
        this.refreshService = refreshService;
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    // AuthenticationManager Bean을 등록합니다. 이는 인증 관리자로 사용됩니다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // SecurityFilterChain Bean을 등록합니다. 이는 HTTP 보안 설정을 정의합니다.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/notice").hasRole("ADMIN")
                        .requestMatchers("/api/auth/user", "/login", "/notice", "/reissue").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                .addFilterAt(new LoginFilter(userService, refreshService, authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .addLogoutHandler(new CustomLogoutHandler(jwtUtil, refreshService, userService))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
