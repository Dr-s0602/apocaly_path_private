package com.apocaly.apocaly_path_private.file.controller;

import com.apocaly.apocaly_path_private.file.model.entity.FileEntity;
import com.apocaly.apocaly_path_private.file.service.FileService;
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
public class FileController {

    private final FileService fileService;

    // FileController 생성자
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 파일 업로드 엔드포인트
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("index") int index) {
        // 파일을 저장하고 저장된 파일 ID를 반환
        FileEntity storedFile = fileService.storeFile(file, index);
        return ResponseEntity.ok().body(Map.of("fileId", storedFile.getId()));
    }

    // 파일을 브라우저에 보여주는 엔드포인트
    @GetMapping("/view/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileId) {
        try {
            // 파일 ID로 파일 정보를 가져옴
            FileEntity fileEntity = fileService.getFileById(fileId);
            // 파일 경로를 설정
            Path filePath = Paths.get(fileEntity.getFileUrl()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 파일이 존재하고 읽을 수 있는 경우, 파일을 반환
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"" + resource.getFilename() + "\"")
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

    // 파일 다운로드 엔드포인트
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        try {
            // 파일 ID로 파일 정보를 가져옴
            FileEntity fileEntity = fileService.getFileById(fileId);
            // 파일 경로를 설정
            Path filePath = Paths.get(fileEntity.getFileUrl()).toAbsolutePath().normalize();
            log.info("File path: " + filePath.toString());
            Resource resource = new UrlResource(filePath.toUri());

            // 파일이 존재하고 읽을 수 있는 경우, 파일을 다운로드
            if (resource.exists() && resource.isReadable()) {
                String encodedFilename = UriUtils.encode(fileEntity.getOriginalFilename(), StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + encodedFilename + "\"")
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
