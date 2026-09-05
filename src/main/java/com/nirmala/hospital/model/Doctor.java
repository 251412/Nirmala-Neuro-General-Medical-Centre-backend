package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    @Id
    private String id;
    
    private String name;
    
    private String photo; // URL, path, or Base64
    
    private String qualification;
    
    private String specialization;
    
    private String departmentId; // Linked Department ID
    
    private String experience; // e.g. "15 Years"
    
    private String designation; // e.g. "Chief Neurologist"
    
    private String bio;
    
    private List<String> consultationTimings; // e.g., ["Mon-Fri: 09:00 AM - 01:00 PM", "Sat: 10:00 AM - 12:00 PM"]
    
    private String phone;
    
    private String status; // "ACTIVE" or "INACTIVE"
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
