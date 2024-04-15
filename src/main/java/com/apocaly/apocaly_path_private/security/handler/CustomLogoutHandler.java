package com.apocaly.apocaly_path_private.security.handler;

import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.security.model.entity.RefreshToken;
import com.apocaly.apocaly_path_private.security.service.RefreshService;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.Optional;

@Slf4j
public class CustomLogoutHandler implements LogoutHandler {
    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;
    private final UserService userService;

    public CustomLogoutHandler(JWTUtil jwtUtil, RefreshService refreshService, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.refreshService = refreshService;
        this.userService = userService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Authorization 헤더에서 토큰 추출
        String authorization = request.getHeader("Authorization");
        if(authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.split(" ")[1];
            // 토큰에서 사용자 이름(이메일) 추출
            String userName = jwtUtil.getUserEmailFromToken(token);
            Optional<User> userOptional = userService.findByEmail(userName);
            if(userOptional.isPresent()) {
                User user = userOptional.get();
                // 사용자 ID를 기반으로 Refresh 토큰 검색
                Optional<RefreshToken> refresh = refreshService.findByUserId(user.getId());
                if(refresh.isPresent()) {
                    RefreshToken refreshToken = refresh.get();
                    // Refresh 토큰 삭제
                    refreshService.deleteByRefresh(refreshToken.getTokenValue());
                }
            }
        }

        // 로그아웃 성공 응답 설정
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
