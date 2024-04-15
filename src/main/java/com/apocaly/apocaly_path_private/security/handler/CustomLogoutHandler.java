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

@Slf4j // Lombok의 로깅을 위한 어노테이션. 이 클래스에서 로깅이 필요할 때 사용합니다.
public class CustomLogoutHandler implements LogoutHandler {
    private final JWTUtil jwtUtil; // JWT 처리를 위한 유틸리티 클래스
    private final RefreshService refreshService; // 리프레시 토큰을 관리하는 서비스
    private final UserService userService; // 사용자 정보를 관리하는 서비스

    // 의존성 주입을 위한 생성자
    public CustomLogoutHandler(JWTUtil jwtUtil, RefreshService refreshService, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.refreshService = refreshService;
        this.userService = userService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 요청 헤더에서 'Authorization' 값을 추출합니다.
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // 'Bearer ' 다음부터 시작하는 실제 토큰 값을 추출합니다.
            String token = authorization.split(" ")[1];
            // 토큰에서 사용자의 이메일(사용자명)을 추출합니다.
            String userName = jwtUtil.getUserEmailFromToken(token);
            // 사용자의 이메일을 통해 사용자 정보를 조회합니다.
            Optional<User> userOptional = userService.findByEmail(userName);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 해당 사용자의 리프레시 토큰을 데이터베이스에서 조회합니다.
                Optional<RefreshToken> refresh = refreshService.findByUserId(user.getId());
                if (refresh.isPresent()) {
                    RefreshToken refreshToken = refresh.get();
                    // 리프레시 토큰을 데이터베이스에서 삭제합니다.
                    refreshService.deleteByRefresh(refreshToken.getTokenValue());
                }
            }
        }

        // 클라이언트에게 로그아웃 성공 응답을 보냅니다.
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
