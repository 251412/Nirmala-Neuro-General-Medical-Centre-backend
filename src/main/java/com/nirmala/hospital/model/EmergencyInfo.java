package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "emergency_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyInfo {
    @Id
    private String id;
    
    private String emergencyNumber;
    
    private String description;
    
    private String address;
    
    private String availability; // e.g. "24/7 Hours Available"
    
    private List<String> instructions; // First-aid or emergency directions
    
    private LocalDateTime updatedAt;
}
