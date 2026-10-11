package com.govfix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".pdf");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/jpg", "application/pdf"
    );

    private final Path uploadPath;

    public FileStorageService(@Value("${govfix.upload.dir:./uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
            log.info("Initialized file upload directory at: {}", this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid filename sequence: " + originalFilename);
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type. Only JPG, PNG, and PDF files are allowed.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file MIME type: " + contentType);
        }

        try {
            String sanitizedFilename = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            Path targetLocation = this.uploadPath.resolve(sanitizedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/" + sanitizedFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    public Path getFilePath(String fileName) {
        return this.uploadPath.resolve(fileName).normalize();
    }
}
