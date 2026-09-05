package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "uploaded_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFile {
    @Id
    private String id;
    private String imageUrl;
    private String originalFilename;
    private String storedFilename;
    private String mimeType;
    private long fileSize;
    private LocalDateTime uploadedAt;
}
