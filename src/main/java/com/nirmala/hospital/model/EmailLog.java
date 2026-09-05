package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {
    @Id
    private String id;
    
    private String appointmentId;
    
    private String recipientEmail;
    
    private String emailType; // APPOINTMENT_PENDING, APPOINTMENT_CONFIRMED, APPOINTMENT_RESCHEDULED, APPOINTMENT_CANCELLED
    
    private String subject;
    
    private String status; // QUEUED, SENT, FAILED
    
    private String providerMessageId;
    
    private String errorMessage;
    
    private LocalDateTime sentAt;
    
    private LocalDateTime createdAt;
}
