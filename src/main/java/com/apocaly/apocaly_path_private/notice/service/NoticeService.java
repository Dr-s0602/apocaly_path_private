package com.apocaly.apocaly_path_private.notice.service;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import com.apocaly.apocaly_path_private.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public NoticeBoard save(NoticeBoard noticeBoard) {
        return noticeRepository.save(noticeBoard);
    }

    public Map<String, Object> getNoticesWithPinnedSeparate(String category, String inactiveStatus, String title, int page, int size) {
        List<NoticeBoard> pinnedNotices = noticeRepository.findTop5ByCategoryAndIsPinnedTrueOrderByCreatedAtDesc(category);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NoticeBoard> noticePage = noticeRepository.findByCategoryAndStatusNotAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                category, inactiveStatus, title, pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pinnedNotices", pinnedNotices);
        response.put("regularNotices", noticePage.getContent());
        response.put("currentPage", noticePage.getNumber());
        response.put("totalItems", noticePage.getTotalElements());
        response.put("totalPages", noticePage.getTotalPages());

        return response;
    }


}
