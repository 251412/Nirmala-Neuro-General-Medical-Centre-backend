package com.nirmala.hospital.controller;

import com.nirmala.hospital.model.UploadedFile;
import com.nirmala.hospital.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/uploads", "/api/admin/uploads"})
public class UploadController {

    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/images")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            UploadedFile stored = storageService.store(file);
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", stored.getImageUrl());
            response.put("originalFilename", stored.getOriginalFilename());
            response.put("storedFilename", stored.getStoredFilename());
            response.put("mimeType", stored.getMimeType());
            response.put("fileSize", stored.getFileSize());
            response.put("uploadedAt", stored.getUploadedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Image upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/images/batch")
    public ResponseEntity<?> uploadImagesBatch(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<UploadedFile> storedList = storageService.storeBatch(files);
            return ResponseEntity.status(HttpStatus.CREATED).body(storedList);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Batch image upload failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/images")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String url) {
        try {
            boolean deleted = storageService.deleteByUrl(url);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Image not found or could not be deleted"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete image: " + e.getMessage()));
        }
    }
}
