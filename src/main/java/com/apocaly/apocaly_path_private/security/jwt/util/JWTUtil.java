package com.apocaly.apocaly_path_private.security.jwt.util;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import com.apocaly.apocaly_path_private.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    private final long expirationTime;
    private final UserRepository userRepository;


    public JWTUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationTime, UserRepository userRepository) {
        // SignatureAlgorithm.HS256을 사용하여 SecretKey를 생성합니다.
        // secret.getBytes(StandardCharsets.UTF_8) 대신 Keys.secretKeyFor을 사용하여 키를 생성할 수 있습니다.
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        // 미리 정의된 알고리즘을 사용
        this.expirationTime = expirationTime;
        this.userRepository = userRepository;
    }

    // 사용자의 이메일과 만료 시간을 사용하여 JWT 토큰을 생성합니다.
    public String generateToken(String userEmail) {
        // 사용자 정보를 조회
        Optional<User> user = userRepository.findByEmail(userEmail);

        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User with email " + userEmail + " not found");
        }

        // is_admin 값을 추출
        boolean isAdmin = user.get().getIsAdmin();

        return Jwts.builder()
                .setSubject(userEmail)
                // "admin" 클레임 추가: true 또는 false
                .claim("admin", isAdmin)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰에서 사용자의 이메일을 추출합니다.
    public String getUserEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // JWT 토큰의 만료를 체크합니다.
    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.getExpiration().before(new Date());
    }

    // JWT 토큰에서 "admin" 클레임을 통해 사용자가 관리자인지 여부를 확인합니다.
    public boolean isAdminFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.get("admin", Boolean.class);
    }
}