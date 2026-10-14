package com.campusfix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    // allowed content types -> file extension we store with (ignores the client's file name)
    private static final Map<String, String> ALLOWED = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp");

    private final Path dir;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
    }

    /** Saves the image and returns its public URL path, e.g. /uploads/uuid.jpg */
    public String saveImage(MultipartFile file) {
        String ext = ALLOWED.get(file.getContentType());
        if (ext == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PNG, JPG or WEBP images are allowed");
        try {
            String name = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), dir.resolve(name));
            return "/uploads/" + name;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save image");
        }
    }
}
