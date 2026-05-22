package com.marketplace.backend.service;

import com.marketplace.backend.exception.ImageProcessingException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {
    private static final int UNKNOWN_PART_SIZE = -1;

    private final MinioClient minioClient;

    @Value("${images.s3Endpoint}")
    private String endpoint;

    @Value("${images.bucketName}")
    private String bucketName;

    @Value("${images.defaultAvatarPath}")
    private String defaultAvatarPath;


    public String upload(MultipartFile file) {
        try {
            return upload(
                    file.getInputStream(),
                    FileNameUtils.getExtension(file.getOriginalFilename()),
                    file.getSize()
            );
        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new ImageProcessingException("Failed to upload file: " + e.getMessage());
        }
    }

    public List<String> upload(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        return files.stream()
                .map(this::upload)
                .collect(Collectors.toList());
    }

    public List<String> upload(List<MultipartFile> files, List<String> existingPaths) {
        List<String> result = new ArrayList<>();
        if (existingPaths != null) {
            result.addAll(existingPaths);
        }
        if (files != null) {
            files.stream()
                    .map(this::upload)
                    .forEach(result::add);
        }
        return result;
    }

    public String upload(InputStream inputStream, String extension, long size) {
        try {
            String fileName = UUID.randomUUID() + "." + extension;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .contentType(URLConnection.guessContentTypeFromName(fileName))
                            .stream(inputStream, size, UNKNOWN_PART_SIZE)
                            .build()
            );
            return fileName;
        } catch (Exception e) {
            log.error("MinIO upload error", e);
            throw new ImageProcessingException("Failed to upload file to storage: " + e.getMessage());
        }
    }

    public void delete(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileName, e);
            throw new ImageProcessingException("Failed to delete file: " + fileName);
        }
    }

    public String buildUrlImage(String imageFileName) {
        if (imageFileName == null || imageFileName.isBlank()) {
            return defaultAvatarPath;
        }
        return String.format("%s/%s/%s",
                endpoint.replaceAll("/+$", ""),
                bucketName,
                imageFileName);
    }

    public List<String> buildUrlImage(List<String> imagesFileName) {
        if (imagesFileName == null) return List.of();
        return imagesFileName.stream()
                .map(this::buildUrlImage)
                .collect(Collectors.toList());
    }
}