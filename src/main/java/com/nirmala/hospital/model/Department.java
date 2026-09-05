package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String slug;
    
    private String description;
    
    private String image; // Base64 or image path/URL
    
    private List<String> services; // Array of services offered by the department
    
    private String status; // "ACTIVE" or "INACTIVE"
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
