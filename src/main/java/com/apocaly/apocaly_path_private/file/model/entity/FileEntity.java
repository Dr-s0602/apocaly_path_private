package com.apocaly.apocaly_path_private.file.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "files")
public class FileEntity {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    private String originalFilename;
    private String fileType;
    private String fileUrl;
    private Integer fileIndex;
}
