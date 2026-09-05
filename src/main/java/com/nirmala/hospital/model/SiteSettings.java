package com.nirmala.hospital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "site_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettings {
    @Id
    private String id;
    
    private String hospitalName;
    
    private String logo; // URL, path, or text-based logo
    
    private String address;
    
    private String phone;
    
    private String email;
    
    private String workingHours; // e.g. "Mon - Sat: 9:00 AM - 8:00 PM"
    
    private Map<String, String> socialLinks; // e.g. {"facebook": "...", "twitter": "..."}
    
    private String mapInformation; // Google Maps embed source URL or iframe
    
    private String mapLink; // Direct Google Maps share link
    
    private String emergencyNumber;
    
    // Hospital Location & Google Maps Configuration
    private String city;
    private String state;
    private String country;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String googleMapsUrl;
    private String googleMapsDirectionsUrl;
    private String googlePlaceId;
    private String googleMapsApiKey;
    
    private LocalDateTime updatedAt;
}
