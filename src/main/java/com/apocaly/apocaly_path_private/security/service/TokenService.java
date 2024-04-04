package com.apocaly.apocaly_path_private.security.service;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] secretKeyBytes = Base64.getDecoder().decode(secretKey);
        this.signingKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(User userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userDetails.getId()))
                .claim("is_admin", userDetails.getIsAdmin())
                .claim("is_email_verified", userDetails.getIsEmailVerified())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey) // 이제 여기서 signingKey 사용 가능
                .compact();
    }

    public boolean validateToken(String jwt) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(jwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromJWT(String jwt) {
        Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(jwt);
        return claimsJws.getBody().getSubject();
    }
}
