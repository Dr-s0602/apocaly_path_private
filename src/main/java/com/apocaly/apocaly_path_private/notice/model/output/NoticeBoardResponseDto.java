package com.apocaly.apocaly_path_private.notice.model.output;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeBoardResponseDto {
    private UUID id;
    private String title;
    private String content;
    private String authorEmail;
    private Date createdAt;
    private Integer views;
    private Integer commentsCount;
    private Integer likeCount;
    private Boolean isPinned;
    private String category;
    private List<String> fileIds;

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
        this.fileIds = noticeBoard.getFiles().stream()
                .map(file -> file.getFileId())
                .collect(Collectors.toList());
    }
}
