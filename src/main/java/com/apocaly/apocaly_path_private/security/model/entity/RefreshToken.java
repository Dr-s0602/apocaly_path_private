package com.apocaly.apocaly_path_private.security.model.entity;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "token_value", nullable = false, length = 255)
    private String tokenValue;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_in", nullable = false)
    private Long expiresIn;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(length = 50)
    private String status;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (issuedAt == null) issuedAt = now;
        if (expirationDate == null) expirationDate = now.plusSeconds(expiresIn / 1000); // 예를 들어 expiresIn이 밀리초 단위라면
    }
}

