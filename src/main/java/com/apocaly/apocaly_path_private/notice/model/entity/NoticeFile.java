package com.apocaly.apocaly_path_private.file.model.entity;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notice_files")
public class NoticeFile {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "notice_id", columnDefinition = "CHAR(36)")
    private String noticeId;

    @Column(name = "file_id", columnDefinition = "CHAR(36)")
    private String fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", insertable = false, updatable = false)
    private NoticeBoard noticeBoard;
}
