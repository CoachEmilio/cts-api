package com.surstudio.cts.common.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/png",  ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp"
    );

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:}")
    private String baseUrl;

    @PostMapping
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("File exceeds 5 MB limit");
        }
        String contentType = file.getContentType();
        String ext = contentType != null ? ALLOWED_TYPES.get(contentType) : null;
        if (ext == null) {
            throw new IllegalArgumentException("Only PNG, JPEG and WEBP images are accepted");
        }

        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename));

        String url = (baseUrl.isBlank() ? "" : baseUrl) + "/uploads/" + filename;
        return Map.of("url", url);
    }
}
