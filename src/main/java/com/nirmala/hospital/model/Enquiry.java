package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "enquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enquiry {
    @Id
    private String id;
    
    private String patientName;
    
    private String phone;
    
    private String email;
    
    private String subject;
    
    private String departmentId; // Linked Department ID (optional)
    
    private String message;
    
    private String status; // "NEW", "CONTACTED", "IN_PROGRESS", "RESOLVED"
    
    private String adminNotes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
