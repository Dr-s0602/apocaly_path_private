package com.apocaly.apocaly_path_private.security.config;


// Spring Security와 관련된 클래스들을 import 합니다.
import com.apocaly.apocaly_path_private.security.jwt.filter.JWTFilter;
import com.apocaly.apocaly_path_private.security.jwt.filter.LoginFilter;
import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;


// Spring Framework 설정 관련 클래스들을 import 합니다.
import com.apocaly.apocaly_path_private.security.service.RefreshService;
import com.apocaly.apocaly_path_private.user.service.UserService;
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
                // CSRF, formLogin, httpBasic 인증을 비활성화합니다.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // URL 별 접근 권한을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,"/notice").hasRole("ADMIN")
                        // 특정 경로에 대한 접근을 누구나 허용합니다.
                        .requestMatchers("/api/auth/user","/login","/notice","/reissue").permitAll()
                        // 나머지 요청에 대해서는 인증을 요구합니다.
                        .anyRequest().authenticated()
                )
                // JWTFilter와 LoginFilter를 필터 체인에 추가합니다.
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                .addFilterAt(new LoginFilter(userService,refreshService,authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)
                // 세션 정책을 STATELESS로 설정하여 세션을 사용하지 않도록 합니다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
