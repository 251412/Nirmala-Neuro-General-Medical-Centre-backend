package com.nirmala.hospital.service;

import com.nirmala.hospital.model.UploadedFile;
import com.nirmala.hospital.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LocalStorageService implements StorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );

    private final Path rootLocation;
    private final UploadedFileRepository uploadedFileRepository;

    public LocalStorageService(@Value("${app.upload.dir:uploads}") String uploadDir,
                               UploadedFileRepository uploadedFileRepository) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.uploadedFileRepository = uploadedFileRepository;
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public UploadedFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type. Please upload a JPG, PNG, or WEBP image.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "image.jpg";
        }
        
        // Prevent path traversal
        originalFilename = Paths.get(originalFilename).getFileName().toString();

        String extension = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension. Only .jpg, .jpeg, .png, and .webp are allowed.");
        }

        // Generate safe unique stored filename
        String storedFilename = "img_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;

        Path destinationFile = this.rootLocation.resolve(storedFilename).normalize();
        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new SecurityException("Cannot store file outside current directory.");
        }

        try {
            byte[] bytes = file.getBytes();
            if ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension)) {
                try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes)) {
                    BufferedImage originalImage = ImageIO.read(bais);
                    if (originalImage != null && (originalImage.getWidth() > 2000 || originalImage.getHeight() > 2000)) {
                        BufferedImage resizedImage = resizeImage(originalImage, 2000);
                        String format = "png".equals(extension) ? "png" : "jpg";
                        ImageIO.write(resizedImage, format, destinationFile.toFile());
                    } else {
                        Files.write(destinationFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                } catch (Exception e) {
                    Files.write(destinationFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            } else {
                Files.write(destinationFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + storedFilename, e);
        }

        String imageUrl = "/uploads/" + storedFilename;

        UploadedFile uploadedFile = UploadedFile.builder()
                .imageUrl(imageUrl)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .mimeType(contentType)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        return uploadedFileRepository.save(uploadedFile);
    }

    @Override
    public List<UploadedFile> storeBatch(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for batch upload.");
        }
        List<UploadedFile> stored = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                stored.add(store(file));
            }
        }
        return stored;
    }

    @Override
    public boolean deleteByUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            return false;
        }

        String storedFilename = imageUrl.replace("/uploads/", "");
        Path file = this.rootLocation.resolve(storedFilename).normalize();

        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }
            uploadedFileRepository.findByImageUrl(imageUrl).ifPresent(uploadedFileRepository::delete);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "jpg";
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxDimension) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return originalImage;
        }

        double ratio;
        if (width > height) {
            ratio = (double) maxDimension / width;
        } else {
            ratio = (double) maxDimension / height;
        }

        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, 
                originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }
}
