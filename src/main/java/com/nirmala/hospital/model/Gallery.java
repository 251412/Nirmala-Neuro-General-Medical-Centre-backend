package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "gallery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gallery {
    @Id
    private String id;
    
    private String image; // URL, path, or Base64
    
    private String title;
    
    private String caption;
    
    private String category; // e.g. "Facilities", "Equipment", "Wards"
    
    private String status; // "ACTIVE" or "INACTIVE"
    
    private LocalDateTime uploadedAt;
}
