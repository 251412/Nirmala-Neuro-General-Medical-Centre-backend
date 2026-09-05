package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    private String id;
    
    private String appointmentId; // Format: "NM-2026-XXXX"
    
    private String patientName;
    
    private int age;
    
    private String gender;
    
    private String phone;
    
    private String email;
    
    private String departmentId; // Linked Department ID
    
    private String doctorId; // Linked Doctor ID
    
    private String preferredDate; // Format: "YYYY-MM-DD"
    
    private String preferredTime; // Format: "HH:MM AM/PM" or slot name
    
    private String reason;
    
    private String status; // "PENDING", "CONFIRMED", "RESCHEDULED", "CANCELLED", "COMPLETED"
    
    private String adminNotes;
    
    private LocalDateTime confirmationTimestamp;
    
    private String pdfFileReference;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
