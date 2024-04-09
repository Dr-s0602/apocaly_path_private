package com.apocaly.apocaly_path_private.notice.controller;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import com.apocaly.apocaly_path_private.notice.model.input.Notice_Input;
import com.apocaly.apocaly_path_private.notice.service.NoticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping("")
    public ResponseEntity<?> boardWriting(@RequestBody Notice_Input noticeInput){
        UUID noticeId = UUID.randomUUID();
        NoticeBoard noticeBoard = NoticeBoard.builder()
                .id(noticeId)
                .authorId(noticeInput.getAuthorId())
                .title(noticeInput.getTitle())
                .content(noticeInput.getContent())
                .isPinned(noticeInput.getIsPinned())
                .category("notice")
                .status("activated")
                .build();
        NoticeBoard savedNoticeBoard = noticeService.save(noticeBoard);  // 데이터베이스에 저장
        return new ResponseEntity<>(savedNoticeBoard, HttpStatus.CREATED);  // 클라이언트에게 저장된 객체와 HTTP 상태 코드를 반환
    }

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getNoticesWithPinnedSeparate(
            @RequestParam(defaultValue = "notice") String category,
            @RequestParam(defaultValue = "inactivate") String status,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> notices = noticeService.getNoticesWithPinnedSeparate(category, status, title, page, size);

        return ResponseEntity.ok(notices);
    }
}
