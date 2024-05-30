package com.apocaly.apocaly_path_private.notice.model.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notice_Input {
    @NotBlank(message = "Title cannot be empty")
    @Size(max = 255, message= "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    private Boolean isPinned;

    private List<String> fileIds;
}
