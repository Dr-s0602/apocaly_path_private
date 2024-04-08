package com.apocaly.apocaly_path_private.security.jwt.filter;

import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.user.model.input.InputUser;
import com.apocaly.apocaly_path_private.user.model.output.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // ObjectMapper를 사용하여 요청 본문에서 InputUser 객체로 변환
            InputUser loginData = new ObjectMapper().readValue(request.getInputStream(), InputUser.class);
            // UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginData.getEmail(), loginData.getPassword());
            // AuthenticationManager에게 token을 전달하여 인증 과정 수행
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new AuthenticationServiceException("인증 처리 중 오류가 발생했습니다.", e);
        }
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //UserDetailsS
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        String token = jwtUtil.generateToken(username);

        // 토큰을 응답 헤더에 추가
        response.addHeader("Authorization", "Bearer " + token);

        // 클라이언트가 Authorization 헤더를 읽을 수 있도록 노출
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        // 실패시 401 에러
        log.info("response : {}", response);
        log.info("request : {}", request);
        log.info("failed : {}", failed);
        response.setStatus(401);
    }
}