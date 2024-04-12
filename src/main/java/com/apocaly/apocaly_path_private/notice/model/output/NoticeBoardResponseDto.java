package com.apocaly.apocaly_path_private.notice.model.output;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeBoardResponseDto {
    private UUID id;
    private String title;
    private String content;
    private String authorEmail; // 사용자 이메일만 포함
    private Date createdAt;
    private Integer views;
    private Integer commentsCount;
    private Integer likeCount;
    private Boolean isPinned;
    private String category;

    // 생성자에서 NoticeBoard와 User 엔티티를 받아 필요한 정보를 설정
    public NoticeBoardResponseDto(NoticeBoard noticeBoard) {
        this.id = noticeBoard.getId();
        this.title = noticeBoard.getTitle();
        this.content = noticeBoard.getContent();
        this.authorEmail = noticeBoard.getAuthor().getEmail(); // Lazy Loading에 주의
        this.createdAt = noticeBoard.getCreatedAt();
        this.views = noticeBoard.getViews();
        this.commentsCount = noticeBoard.getCommentsCount();
        this.likeCount = noticeBoard.getLikeCount();
        this.isPinned = noticeBoard.getIsPinned();
        this.category = noticeBoard.getCategory();
    }
}

