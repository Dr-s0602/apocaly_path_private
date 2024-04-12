package com.apocaly.apocaly_path_private.notice.controller;

import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import com.apocaly.apocaly_path_private.notice.model.input.Notice_Input;
import com.apocaly.apocaly_path_private.notice.service.NoticeService;
import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    public NoticeController(NoticeService noticeService, JWTUtil jwtUtil, UserRepository userRepository) {
        this.noticeService = noticeService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("")
    public ResponseEntity<?> boardWriting(HttpServletRequest request, @RequestBody Notice_Input noticeInput){
        String token = request.getHeader("Authorization").substring("Bearer ".length());
        String userEmail = jwtUtil.getUserEmailFromToken(token);
        boolean isAdmin = jwtUtil.isAdminFromToken(token);
        if(!isAdmin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. User is not an administrator.");
        }
        Optional<User> loginUser = userRepository.findByEmail(userEmail);
        if(loginUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");

        }

        NoticeBoard noticeBoard = NoticeBoard.builder()
                .id(UUID.randomUUID())
                .authorId(loginUser.get().getId())
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
