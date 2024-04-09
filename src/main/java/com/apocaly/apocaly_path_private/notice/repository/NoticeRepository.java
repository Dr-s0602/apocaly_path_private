package com.apocaly.apocaly_path_private.notice.repository;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeBoard, UUID> {

    List<NoticeBoard> findTop5ByCategoryAndIsPinnedTrueOrderByCreatedAtDesc(String category);

    Page<NoticeBoard> findByCategoryAndStatusNotAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            String category, String status, String title, Pageable pageable);
}
