package com.apocaly.apocaly_path_private.notice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_notice_likes", schema = "apocaly")
public class UserNoticeLike {
    @EmbeddedId
    private UserNoticeLikeId id;

    @ColumnDefault("current_timestamp()")
    @Column(name = "liked_at")
    private Instant likedAt;

    @PrePersist
    protected void onCreate() {
        likedAt = Instant.now(); // 엔티티가 저장되기 전 현재 시간으로 설정
    }
}