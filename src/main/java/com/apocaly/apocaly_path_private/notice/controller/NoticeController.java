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

@RestController // 이 클래스가 RESTful 웹 서비스의 컨트롤러임을 나타냅니다.
@RequestMapping("/notice") // 이 컨트롤러의 모든 메소드가 처리할 기본 URL 경로를 '/notice'로 설정합니다.
public class NoticeController {

    private final NoticeService noticeService; // 공지사항 관련 비즈니스 로직을 처리하는 서비스
    private final JWTUtil jwtUtil; // JWT 토큰을 처리하는 유틸리티 클래스
    private final UserRepository userRepository; // 사용자 정보를 다루는 레포지토리

    // 생성자 주입을 통해 의존성 주입을 받습니다.
    public NoticeController(NoticeService noticeService, JWTUtil jwtUtil, UserRepository userRepository) {
        this.noticeService = noticeService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("") // HTTP POST 요청을 '/notice' 경로로 매핑합니다.
    public ResponseEntity<?> boardWriting(HttpServletRequest request, @RequestBody Notice_Input noticeInput){
        // 공지사항 작성 요청을 처리하는 메소드입니다.
        String token = request.getHeader("Authorization").substring("Bearer ".length()); // 요청 헤더에서 JWT 토큰을 추출합니다.
        String userEmail = jwtUtil.getUserEmailFromToken(token); // 토큰에서 사용자 이메일을 추출합니다.
        boolean isAdmin = jwtUtil.isAdminFromToken(token); // 토큰에서 사용자의 관리자 여부를 추출합니다.

        if(!isAdmin){ // 사용자가 관리자가 아니라면 접근을 거부합니다.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. User is not an administrator.");
        }

        Optional<User> loginUser = userRepository.findByEmail(userEmail); // 이메일을 통해 사용자 정보를 조회합니다.
        if(loginUser.isEmpty()){ // 조회된 사용자 정보가 없다면 에러를 반환합니다.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // NoticeBoard 엔티티를 생성하고 요청 받은 내용을 바탕으로 설정합니다.
        NoticeBoard noticeBoard = NoticeBoard.builder()
                .id(UUID.randomUUID()) // 고유 ID를 UUID로 생성합니다.
                .authorId(loginUser.get().getId()) // 작성자 ID를 설정합니다.
                .title(noticeInput.getTitle()) // 제목을 설정합니다.
                .content(noticeInput.getContent()) // 내용을 설정합니다.
                .isPinned(noticeInput.getIsPinned()) // 공지사항 상단 고정 여부를 설정합니다.
                .category("notice") // 카테고리를 'notice'로 설정합니다.
                .status("activated") // 상태를 'activated'로 설정합니다.
                .build();
        NoticeBoard savedNoticeBoard = noticeService.save(noticeBoard);  // 데이터베이스에 저장합니다.
        return new ResponseEntity<>(savedNoticeBoard, HttpStatus.CREATED);  // 저장된 공지사항 정보와 함께 생성됨 상태 코드를 반환합니다.
    }

    @GetMapping("") // HTTP GET 요청을 '/notice' 경로로 매핑합니다.
    public ResponseEntity<Map<String, Object>> getNoticesWithPinnedSeparate(
            @RequestParam(defaultValue = "notice") String category, // 카테고리, 기본값은 'notice'
            @RequestParam(defaultValue = "inactivate") String status, // 상태, 기본값은 'inactivate'
            @RequestParam(defaultValue = "") String title, // 제목 검색어, 기본값은 빈 문자열
            @RequestParam(defaultValue = "0") int page, // 페이지 번호, 기본값은 0
            @RequestParam(defaultValue = "10") int size) { // 페이지 당 항목 수, 기본값은 10

        // 공지사항을 조회하는 비즈니스 로직을 호출하고 결과를 맵 형태로 반환받습니다.
        Map<String, Object> notices = noticeService.getNoticesWithPinnedSeparate(category, status, title, page, size);

        return ResponseEntity.ok(notices); // 조회된 공지사항 정보와 함께 OK 상태 코드를 반환합니다.
    }
}