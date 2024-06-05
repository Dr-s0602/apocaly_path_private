package com.apocaly.apocaly_path_private.file.controller;

import com.apocaly.apocaly_path_private.file.model.entity.FileEntity;
import com.apocaly.apocaly_path_private.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/file")
@Slf4j
@Tag(name = "File", description = "File management APIs")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @Operation(
            summary = "Upload file",
            description = "파일 업로드 하는 함수 입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "File upload request body",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(
                                    type = "object",
                                    example = "{ \"file\": \"binary data\", \"index\": 1 }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File uploaded successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            type = "object",
                                            example = "{ \"fileId\": \"UUID\" }"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Map<String,String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("index") int index
    ) {
        FileEntity storedFile = fileService.storeFile(file, index);
        return ResponseEntity.ok().body(Map.of("fileId", storedFile.getId()));
    }

    @GetMapping("/view/{fileId}")
    @Operation(
            summary = "View file",
            description = "Displays the file in the browser.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File displayed successfully",
                            content = @Content(
                                    mediaType = "application/octet-stream",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "File not found",
                            content = @Content(schema = @Schema())
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file ID",
                            content = @Content(schema = @Schema())
                    )
            }
    )
    public ResponseEntity<Resource> viewFile(@PathVariable String fileId) {
        return getFileResponse(fileId, "inline");
    }

    @GetMapping("/download/{fileId}")
    @Operation(
            summary = "Download file",
            description = "Downloads the file.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File downloaded successfully",
                            content = @Content(
                                    mediaType = "application/octet-stream",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "File not found",
                            content = @Content(schema = @Schema())
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file ID",
                            content = @Content(schema = @Schema())
                    )
            }
    )
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        return getFileResponse(fileId, "attachment");
    }

    private ResponseEntity<Resource> getFileResponse(String fileId, String dispositionType) {
        try {
            FileEntity fileEntity = fileService.getFileById(fileId);
            Path filePath = Paths.get(fileEntity.getFileUrl()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String encodedFilename = UriUtils.encode(fileEntity.getOriginalFilename(), StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + ";filename=\"" + encodedFilename + "\"")
                        .header(HttpHeaders.CONTENT_TYPE, fileEntity.getFileType())
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Error while fetching the file: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
