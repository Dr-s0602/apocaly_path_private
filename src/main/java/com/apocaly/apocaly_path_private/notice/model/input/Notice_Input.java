package com.apocaly.apocaly_path_private.notice.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notice_Input {
    private String title;
    private String content;
    private UUID authorId;
    private Boolean isPinned;
}
