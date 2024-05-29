package com.apocaly.apocaly_path_private.file.service;

import com.apocaly.apocaly_path_private.file.model.entity.FileEntity;
import com.apocaly.apocaly_path_private.file.repository.FileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    // FileService 생성자
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            // 파일을 저장할 디렉토리를 생성
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    // 파일을 저장하는 메서드
    public FileEntity storeFile(MultipartFile file, int index) {
        String originalFilename = file.getOriginalFilename();
        // 파일 이름에서 공백을 밑줄로 변환
        String sanitizedFilename = originalFilename.replaceAll("\\s+", "_");
        String fileType = file.getContentType();
        // 파일 ID를 UUID로 생성
        String fileId = UUID.randomUUID().toString();
        // 새 파일 이름을 설정
        String newFilename = fileId + "." + sanitizedFilename.substring(sanitizedFilename.lastIndexOf(".") + 1);

        try {
            // 파일을 저장할 위치를 설정
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 파일 엔티티를 생성하고 저장
            FileEntity fileEntity = new FileEntity();
            fileEntity.setId(fileId);
            fileEntity.setOriginalFilename(sanitizedFilename);
            fileEntity.setFileType(fileType);
            fileEntity.setFileUrl(targetLocation.toString());
            fileEntity.setFileIndex(index);
            fileRepository.save(fileEntity);

            return fileEntity;
        } catch (Exception ex) {
            throw new RuntimeException("Could not store file " + sanitizedFilename + ". Please try again!", ex);
        }
    }

    // 파일 ID로 파일 엔티티를 가져오는 메서드
    public FileEntity getFileById(String fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + fileId));
    }
}
