package com.apocaly.apocaly_path_private.notice.repository;

import com.apocaly.apocaly_path_private.file.model.entity.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeFileRepository extends JpaRepository<NoticeFile, String> {
}
