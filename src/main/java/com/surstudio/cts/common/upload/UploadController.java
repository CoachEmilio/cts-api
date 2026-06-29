package com.surstudio.cts.common.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:}")
    private String baseUrl;

    @PostMapping
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds 5 MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image files are accepted");
        }

        String ext = contentType.contains("png") ? ".png"
                : contentType.contains("webp") ? ".webp"
                : ".jpg";
        String filename = UUID.randomUUID() + ext;

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
        }

        String url = (baseUrl.isBlank() ? "" : baseUrl) + "/uploads/" + filename;
        return Map.of("url", url);
    }
}