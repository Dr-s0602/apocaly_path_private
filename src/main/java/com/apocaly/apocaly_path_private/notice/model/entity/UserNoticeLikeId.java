package com.apocaly.apocaly_path_private.notice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.UUID;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class UserNoticeLikeId implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 3591122493988909805L;
    @Size(max = 36)
    @Column(name = "user_id", length = 36,columnDefinition = "CHAR(36)")
    private UUID userId;

    @Size(max = 36)
    @Column(name = "notice_id", length = 36,columnDefinition = "CHAR(36)")
    private UUID noticeId;

}