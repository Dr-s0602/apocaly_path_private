package com.apocaly.apocaly_path_private.notice.repository;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeBoard, UUID> {

    List<NoticeBoard> findTop5ByCategoryAndIsPinnedTrueOrderByCreatedAtDesc(String category);

    @Query("SELECT DISTINCT n FROM NoticeBoard n LEFT JOIN FETCH n.files f WHERE n.category = :category AND n.status = :status" +
            " AND (:title IS NULL OR :title = '' OR LOWER(n.title) LIKE LOWER(CONCAT('%', :title, '%')))" +
            " ORDER BY n.createdAt DESC")
    Page<NoticeBoard> findByCategoryAndStatusAndTitleContainingIgnoreCase(
            @Param("category") String category,
            @Param("status") String status,
            @Param("title") String title,
            Pageable pageable);
}
