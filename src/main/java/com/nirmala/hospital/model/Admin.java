package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {
    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String email;
    
    private String passwordHash;
    
    private String role; // e.g., "ADMIN", "SUPER_ADMIN"
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLogin;
}
