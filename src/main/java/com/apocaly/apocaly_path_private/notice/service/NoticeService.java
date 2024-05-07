package com.apocaly.apocaly_path_private.notice.service;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import com.apocaly.apocaly_path_private.notice.model.entity.UserNoticeLike;
import com.apocaly.apocaly_path_private.notice.model.entity.UserNoticeLikeId;
import com.apocaly.apocaly_path_private.notice.model.output.NoticeBoardResponseDto;
import com.apocaly.apocaly_path_private.notice.repository.NoticeRepository;
import com.apocaly.apocaly_path_private.notice.repository.UserNoticeLikesRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    private final UserNoticeLikesRepository userNoticeLikesRepository;

    public NoticeService(NoticeRepository noticeRepository, UserNoticeLikesRepository userNoticeLikesRepository) {
        this.noticeRepository = noticeRepository;
        this.userNoticeLikesRepository = userNoticeLikesRepository;
    }

    public NoticeBoard save(NoticeBoard noticeBoard) {
        return noticeRepository.save(noticeBoard);
    }

    public Map<String, Object> getNoticesWithPinnedSeparate(String category, String inactiveStatus, String title, int page, int size) {
        // 고정된 공지사항을 조회하여 DTO로 변환
        List<NoticeBoard> pinnedNoticeBoards = noticeRepository.findTop5ByCategoryAndIsPinnedTrueOrderByCreatedAtDesc(category);
        List<NoticeBoardResponseDto> pinnedNotices = pinnedNoticeBoards.stream()
                .map(NoticeBoardResponseDto::new)
                .collect(Collectors.toList());

        // 일반 공지사항을 페이지네이션하여 조회하고, DTO로 변환
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NoticeBoard> noticePage = noticeRepository.findByCategoryAndStatusAndTitleContainingIgnoreCase(
                category, inactiveStatus, title, pageable);
        Page<NoticeBoardResponseDto> noticePageDto = noticePage.map(NoticeBoardResponseDto::new);

        Map<String, Object> response = new HashMap<>();
        response.put("pinnedNotices", pinnedNotices);
        response.put("regularNotices", noticePageDto.getContent());
        response.put("currentPage", noticePageDto.getNumber());
        response.put("totalItems", noticePageDto.getTotalElements());
        response.put("totalPages", noticePageDto.getTotalPages());

        return response;
    }

    public void incrementReadCount(String postId) {
        NoticeBoard notice = noticeRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));
        notice.setViews(notice.getViews()+1);
        noticeRepository.save(notice);
    }


    public String toggleLike(UserNoticeLikeId id) {
        boolean exists = userNoticeLikesRepository.existsById(id);
        if (exists) {
            userNoticeLikesRepository.deleteById(id);
            decrementLikeCount(String.valueOf(id.getNoticeId()));
            return "Like successfully removed.";
        } else {
            UserNoticeLike likeEntry = new UserNoticeLike();
            likeEntry.setId(id);
            userNoticeLikesRepository.save(likeEntry);
            incrementLikeCount(String.valueOf(id.getNoticeId()));
            return "Like successfully added.";
        }
    }

    private void incrementLikeCount(String noticeId) {
        NoticeBoard notice = noticeRepository.findById(UUID.fromString(noticeId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid notice ID: " + noticeId));
        notice.setLikeCount(notice.getLikeCount() + 1);
        noticeRepository.save(notice);
    }

    private void decrementLikeCount(String noticeId) {
        NoticeBoard notice = noticeRepository.findById(UUID.fromString(noticeId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid notice ID: " + noticeId));
        int currentLikes = notice.getLikeCount();
        notice.setLikeCount(Math.max(0, currentLikes - 1)); // Avoid negative counts
        noticeRepository.save(notice);
    }
}
