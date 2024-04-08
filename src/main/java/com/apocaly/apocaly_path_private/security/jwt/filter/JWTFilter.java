package com.apocaly.apocaly_path_private.security.jwt.filter;

// 필요한 클래스와 인터페이스를 import 합니다.
import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.output.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Lombok의 @Slf4j 어노테이션을 사용하여 로깅 기능을 자동으로 추가합니다.
@Slf4j
// Spring Security의 OncePerRequestFilter를 상속받아, 모든 요청에 대해 한 번씩 실행되는 필터를 정의합니다.
public class JWTFilter extends OncePerRequestFilter {

    // JWT 관련 유틸리티 메서드를 제공하는 JWTUtil 클래스의 인스턴스를 멤버 변수로 가집니다.
    private final JWTUtil jwtUtil;

    // 생성자를 통해 JWTUtil의 인스턴스를 주입받습니다.
    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 필터의 주요 로직을 구현하는 메서드입니다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청에서 'Authorization' 헤더를 추출합니다.
        String authorization = request.getHeader("Authorization");

        // 'Authorization' 헤더가 없거나 Bearer 토큰이 아니면 요청을 계속 진행합니다.
        if( authorization == null || !authorization.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        // Bearer 토큰에서 JWT를 추출합니다.
        String token = authorization.split(" ")[1];

        // 토큰이 만료되었으면 요청을 계속 진행합니다.
        if(jwtUtil.isTokenExpired(token)){
            filterChain.doFilter(request,response);
            return;
        }

        // 토큰에서 사용자 이메일과 관리자 여부를 추출합니다.
        String userEmail = jwtUtil.getUserEmailFromToken(token);
        boolean is_admin = jwtUtil.isAdminFromToken(token);

        // 인증에 사용할 임시 User 객체를 생성하고, 이메일과 관리자 여부를 설정합니다.
        User user = new User();
        user.setEmail(userEmail);
        user.setPassword("tempPassword"); // 실제 인증에서는 사용되지 않는 임시 비밀번호를 설정합니다.
        user.setIsAdmin(is_admin);

        // User 객체를 기반으로 CustomUserDetails 객체를 생성합니다.
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // Spring Security의 Authentication 객체를 생성하고, SecurityContext에 설정합니다.
        // 이로써 해당 요청에 대한 사용자 인증이 완료됩니다.
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 필터 체인을 계속 진행합니다.
        filterChain.doFilter(request,response);
    }
}
