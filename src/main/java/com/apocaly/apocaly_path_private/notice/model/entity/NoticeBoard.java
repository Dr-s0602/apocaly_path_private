package com.apocaly.apocaly_path_private.notice.model.entity;

import com.apocaly.apocaly_path_private.file.model.entity.NoticeFile;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User author;

    @Column(name = "author_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID authorId;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    @Builder.Default
    @Column(nullable = false)
    private Integer views = 0;

    @Column(length = 50)
    private String status;

    @Builder.Default
    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned;

    @Column(length = 50)
    private String category;

    @OneToMany(mappedBy = "noticeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<NoticeFile> files;
}
