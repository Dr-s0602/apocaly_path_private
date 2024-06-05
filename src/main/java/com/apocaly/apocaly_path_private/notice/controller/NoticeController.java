package com.apocaly.apocaly_path_private.notice.controller;

import com.apocaly.apocaly_path_private.file.model.entity.NoticeFile;
import com.apocaly.apocaly_path_private.notice.model.entity.NoticeBoard;
import com.apocaly.apocaly_path_private.notice.model.entity.UserNoticeLikeId;
import com.apocaly.apocaly_path_private.notice.model.input.Notice_Input;
import com.apocaly.apocaly_path_private.notice.service.NoticeService;
import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notice")
@Slf4j
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
    @Operation(
            summary = "Write a new notice",
            description = "Creates a new notice post. Only admins can perform this action.",
            requestBody = @RequestBody(
                    description = "Notice input payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "example",
                                    value = "{ \"title\": \"Sample Title\", \"content\": \"Sample content.\", \"isPinned\": true, \"fileIds\": [\"UUID\", \"UUID\"] }"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notice created successfully", content = @Content(schema = @Schema(implementation = NoticeBoard.class))),
            @ApiResponse(responseCode = "403", description = "Access denied. User is not an administrator."),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> boardWriting(HttpServletRequest request, @RequestBody Notice_Input noticeInput) {
        log.info("notice_input = {}", noticeInput);
        String token = request.getHeader("Authorization").substring("Bearer ".length());
        String userEmail = jwtUtil.getUserEmailFromToken(token);
        boolean isAdmin = jwtUtil.isAdminFromToken(token);

        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. User is not an administrator.");
        }

        Optional<User> loginUser = userRepository.findByEmail(userEmail);
        if (loginUser.isEmpty()) {
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

        List<NoticeFile> noticeFiles = null;
        if (noticeInput.getFileIds() != null && !noticeInput.getFileIds().isEmpty()) {
            noticeFiles = noticeInput.getFileIds().stream()
                    .map(fileId -> {
                        NoticeFile noticeFile = new NoticeFile();
                        noticeFile.setId(UUID.randomUUID().toString());
                        noticeFile.setNoticeId(noticeBoard.getId().toString());
                        noticeFile.setFileId(fileId);
                        return noticeFile;
                    }).collect(Collectors.toList());
        }

        log.info("Notice Files to be saved: {}", noticeFiles);

        NoticeBoard savedNoticeBoard = noticeService.save(noticeBoard, noticeFiles);

        return new ResponseEntity<>(savedNoticeBoard, HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(
            summary = "Get notices",
            description = "Retrieve notices with pinned notices separated.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notices retrieved successfully",
                            content = @Content(
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"pinnedNotices\": [\n" +
                                                    "    {\n" +
                                                    "      \"id\": \"1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p\",\n" +
                                                    "      \"title\": \"Pinned Notice Title\",\n" +
                                                    "      \"content\": \"Pinned Notice Content\",\n" +
                                                    "      \"authorEmail\": \"author@example.com\",\n" +
                                                    "      \"createdAt\": \"2023-05-01T12:00:00Z\",\n" +
                                                    "      \"views\": 123,\n" +
                                                    "      \"commentsCount\": 4,\n" +
                                                    "      \"likeCount\": 56,\n" +
                                                    "      \"isPinned\": true,\n" +
                                                    "      \"category\": \"notice\",\n" +
                                                    "      \"fileIds\": [\"file1\", \"file2\"]\n" +
                                                    "    }\n" +
                                                    "  ],\n" +
                                                    "  \"regularNotices\": [\n" +
                                                    "    {\n" +
                                                    "      \"id\": \"1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p\",\n" +
                                                    "      \"title\": \"Regular Notice Title\",\n" +
                                                    "      \"content\": \"Regular Notice Content\",\n" +
                                                    "      \"authorEmail\": \"author@example.com\",\n" +
                                                    "      \"createdAt\": \"2023-05-01T12:00:00Z\",\n" +
                                                    "      \"views\": 123,\n" +
                                                    "      \"commentsCount\": 4,\n" +
                                                    "      \"likeCount\": 56,\n" +
                                                    "      \"isPinned\": false,\n" +
                                                    "      \"category\": \"notice\",\n" +
                                                    "      \"fileIds\": [\"file3\", \"file4\"]\n" +
                                                    "    }\n" +
                                                    "  ],\n" +
                                                    "  \"currentPage\": 0,\n" +
                                                    "  \"totalItems\": 2,\n" +
                                                    "  \"totalPages\": 1\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Map<String, Object>> getNoticesWithPinnedSeparate(
            @RequestParam(defaultValue = "notice") String category,
            @RequestParam(defaultValue = "inactivate") String status,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> notices = noticeService.getNoticesWithPinnedSeparate(category, status, title, page, size);
        return ResponseEntity.ok(notices);
    }

    @PostMapping("/read/{postId}")
    @Operation(
            summary = "Increment read count",
            description = "Increments the read count for the specified post.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Read count incremented successfully",
                            content = @Content(
                                    schema = @Schema(
                                            type = "string",
                                            example = "Read count incremented successfully"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Error incrementing read count",
                            content = @Content(
                                    schema = @Schema(
                                            type = "string",
                                            example = "Error incrementing read count: error message"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<String> incrementReadCount(@PathVariable String postId) {
        try {
            noticeService.incrementReadCount(postId);
            return ResponseEntity.ok("Read count incremented successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error incrementing read count: " + e.getMessage());
        }
    }

    @PostMapping("/likes")
    @Operation(
            summary = "Toggle like",
            description = "Toggles the like status for a notice.",
            requestBody = @RequestBody(
                    description = "Like toggle input",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "example",
                                    value = "{ \"userId\": \"user1\", \"noticeId\": \"notice1\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Like toggled successfully",
                            content = @Content(
                                    schema = @Schema(
                                            type = "string",
                                            example = "Like successfully added."
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    schema = @Schema(
                                            type = "string",
                                            example = "User not found."
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Error processing like toggle",
                            content = @Content(
                                    schema = @Schema(
                                            type = "string",
                                            example = "Error processing like toggle: error message"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<String> toggleLike(HttpServletRequest request, @RequestBody UserNoticeLikeId requestData) {
        try {
            String token = request.getHeader("Authorization").substring("Bearer ".length());
            Optional<User> userOptional = userRepository.findByEmail(jwtUtil.getUserEmailFromToken(token));
            if (userOptional.isPresent()) {
                requestData.setUserId(userOptional.get().getId());
                String result = noticeService.toggleLike(requestData);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing like toggle: " + e.getMessage());
        }
    }
}
