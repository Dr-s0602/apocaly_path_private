package com.apocaly.apocaly_path_private.notice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notice_board")
public class NoticeBoard {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "author_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID authorId;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(nullable = false)
    private Integer views;

    @Column(length = 50)
    private String status;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned;

    @Column(length = 50)
    private String category;
}
