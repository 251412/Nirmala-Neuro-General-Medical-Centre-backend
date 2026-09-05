package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "blogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog {
    @Id
    private String id;
    
    private String title;
    
    @Indexed(unique = true)
    private String slug;
    
    private String featuredImage; // URL or Base64 string
    
    private String content; // Rich HTML or markdown or raw text
    
    private String author;
    
    private String category; // e.g. "Neurology", "General Health"
    
    private String seoTitle;
    
    private String seoDescription;
    
    private LocalDateTime publishedAt; // Null if draft
    
    private String status; // "DRAFT" or "PUBLISHED"
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
