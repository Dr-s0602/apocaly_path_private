package com.apocaly.apocaly_path_private.security.controller;

import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.security.model.entity.RefreshToken;
import com.apocaly.apocaly_path_private.security.service.RefreshService;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class ReissueController {

    private final Long accessExpiredMs;

    private final JWTUtil jwtUtil;

    private final UserService userService;
    private final RefreshService refreshService;

    public ReissueController(JWTUtil jwtUtil, UserService userService, RefreshService refreshService) {
        this.userService = userService;
        this.refreshService = refreshService;
        accessExpiredMs = 600000L;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        // Authorization 헤더에서 리프레시 토큰을 가져옵니다.
        String refresh = request.getHeader("Authorization");
        if (refresh == null || !refresh.startsWith("Bearer ")) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // "Bearer "를 제거하여 실제 토큰 값만 추출합니다.
        String token = refresh.substring("Bearer ".length());

        // 토큰이 만료되었는지 확인합니다.
        try {
            if (jwtUtil.isTokenExpired(token)) {
                return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
            }
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 리프레시 토큰인지 확인합니다.
        String category = jwtUtil.getCategoryFromToken(token);
        if (!category.equals("refresh")) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 토큰에서 사용자 이메일을 추출합니다.
        String username = jwtUtil.getUserEmailFromToken(token);

        // 사용자 정보와 연관된 리프레시 토큰을 데이터베이스에서 조회합니다.
        Optional<User> userOptional = userService.findByEmail(username);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>("user not found", HttpStatus.NOT_FOUND);
        }

        // 리프레시 토큰이 데이터베이스에 있는지 확인합니다.
        Optional<RefreshToken> refreshTokenOptional = refreshService.findByTokenValue(token);
        if (refreshTokenOptional.isEmpty() || !refreshTokenOptional.get().getUser().equals(userOptional.get())) {
            return new ResponseEntity<>("refresh token not found or does not match", HttpStatus.BAD_REQUEST);
        }

        // 여기서 토큰의 만료 여부, 상태 등 추가적인 검증을 수행할 수 있습니다.
        RefreshToken refreshToken = refreshTokenOptional.get();
        if (!refreshToken.getStatus().equals("activated")) {
            return new ResponseEntity<>("refresh token invalid or expired", HttpStatus.BAD_REQUEST);
        }


        // 새로운 액세스 토큰을 생성합니다.
        String access = jwtUtil.generateToken(username, "access", accessExpiredMs);

        // 응답 헤더에 새로운 'Bearer' 토큰을 추가합니다.
        response.addHeader("Authorization", "Bearer " + access);

        // 클라이언트가 Authorization 헤더를 읽을 수 있도록 헤더를 노출시킵니다.
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        // HTTP 200 OK 응답을 반환합니다.
        return new ResponseEntity<>(HttpStatus.OK);
    }

}