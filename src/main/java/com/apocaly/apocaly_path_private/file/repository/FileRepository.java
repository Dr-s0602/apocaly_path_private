package com.apocaly.apocaly_path_private.file.repository;

import com.apocaly.apocaly_path_private.file.model.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
}
