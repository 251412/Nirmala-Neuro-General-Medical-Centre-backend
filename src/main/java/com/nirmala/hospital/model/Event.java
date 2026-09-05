package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    private String id;

    private String title;

    private String eventDate; // Format: "YYYY-MM-DD"

    private String eventTime; // e.g. "10:00 AM – 2:00 PM"

    private String eventType; // e.g. "Health Check-up Camp"

    private String description;

    private boolean active; // Admin can enable/disable the event

    private boolean popupEnabled; // Whether to show this event as a popup

    private int displayOrder; // Lower number = higher priority

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
