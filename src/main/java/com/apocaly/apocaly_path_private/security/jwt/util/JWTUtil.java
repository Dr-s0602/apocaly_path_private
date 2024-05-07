package com.apocaly.apocaly_path_private.security.jwt.util;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

// 스프링 컨테이너에 의해 관리되는 컴포넌트로 선언합니다.
@Component
@Slf4j
public class JWTUtil {

    // JWT 생성과 검증에 사용될 비밀키와 만료 시간을 필드로 선언합니다.
    private final SecretKey secretKey;
    private final UserRepository userRepository;

    // 생성자를 통해 application.properties에서 정의된 JWT 비밀키와 만료 시간, UserRepository를 주입받습니다.
    public JWTUtil(@Value("${jwt.secret}") String secret, UserRepository userRepository) {
        // 비밀키를 초기화합니다. 이 비밀키는 JWT의 서명에 사용됩니다.
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.userRepository = userRepository; // 사용자 정보를 조회하기 위한 UserRepository 인스턴스를 초기화합니다.
    }

    // 사용자의 이메일을 기반으로 JWT를 생성합니다.
    public String generateToken(String userEmail,String category, Long expiredMs) {
        // UserRepository를 사용해 사용자 정보를 조회합니다.
        Optional<User> user = userRepository.findByEmail(userEmail);

        // 사용자 정보가 없는 경우, UsernameNotFoundException을 발생시킵니다.
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User with email " + userEmail + " not found");
        }

        // 사용자의 관리자 여부를 확인합니다.
        boolean isAdmin = user.get().getIsAdmin();

        // JWT를 생성합니다. 여기서는 사용자 이메일을 주체(subject)로, 관리자 여부를 클레임으로 추가합니다.
        return Jwts.builder()
                .setSubject(userEmail)
                .claim("admin", isAdmin) // "admin" 클레임에 관리자 여부를 설정합니다.
                .claim("category",category)
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs)) // 만료 시간을 설정합니다.
                .signWith(secretKey, SignatureAlgorithm.HS256) // 비밀키와 HS256 알고리즘으로 JWT를 서명합니다.
                .compact(); // JWT 문자열을 생성합니다.
    }

    // JWT에서 사용자 이메일을 추출합니다.
    public String getUserEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // JWT의 만료 여부를 검증합니다.
    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.getExpiration().before(new Date());
    }

    // JWT에서 사용자의 관리자 여부를 확인합니다.
    public boolean isAdminFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.get("admin", Boolean.class);
    }

    // JWT에서 사용자의 카테고리를 확인합니다.
    public String getCategoryFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.get("category", String.class);
    }

}
