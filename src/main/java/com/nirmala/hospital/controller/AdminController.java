package com.nirmala.hospital.controller;

import com.nirmala.hospital.model.*;
import com.nirmala.hospital.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final EnquiryRepository enquiryRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final BlogRepository blogRepository;
    private final GalleryRepository galleryRepository;
    private final EmergencyInfoRepository emergencyInfoRepository;
    private final SiteSettingsRepository siteSettingsRepository;
    private final com.nirmala.hospital.service.EmailService emailService;
    private final com.nirmala.hospital.service.PdfGeneratorService pdfGeneratorService;
    private final EmailLogRepository emailLogRepository;
    private final EventRepository eventRepository;

    public AdminController(DoctorRepository doctorRepository, DepartmentRepository departmentRepository,
                           AppointmentRepository appointmentRepository, EnquiryRepository enquiryRepository,
                           ContactMessageRepository contactMessageRepository, BlogRepository blogRepository,
                           GalleryRepository galleryRepository, EmergencyInfoRepository emergencyInfoRepository,
                           SiteSettingsRepository siteSettingsRepository,
                           com.nirmala.hospital.service.EmailService emailService,
                           com.nirmala.hospital.service.PdfGeneratorService pdfGeneratorService,
                           EmailLogRepository emailLogRepository,
                           EventRepository eventRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.enquiryRepository = enquiryRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.blogRepository = blogRepository;
        this.galleryRepository = galleryRepository;
        this.emergencyInfoRepository = emergencyInfoRepository;
        this.siteSettingsRepository = siteSettingsRepository;
        this.emailService = emailService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.emailLogRepository = emailLogRepository;
        this.eventRepository = eventRepository;
    }

    // DASHBOARD STATS
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        long totalDoctors = doctorRepository.count();
        long activeDoctors = doctorRepository.findByStatus("ACTIVE").size();
        
        long totalDepts = departmentRepository.count();
        long activeDepts = departmentRepository.findByStatus("ACTIVE").size();

        List<Appointment> appointments = appointmentRepository.findAll();
        long pendingAppointments = appointments.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long confirmedAppointments = appointments.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count();
        long completedAppointments = appointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long rescheduledAppointments = appointments.stream().filter(a -> "RESCHEDULED".equals(a.getStatus())).count();
        long cancelledAppointments = appointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();

        long newEnquiries = enquiryRepository.findByStatus("NEW").size();
        long newContactMessages = contactMessageRepository.findByStatus("NEW").size();
        long publishedBlogs = blogRepository.findByStatus("PUBLISHED").size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDoctors", totalDoctors);
        stats.put("activeDoctors", activeDoctors);
        stats.put("totalDepartments", totalDepts);
        stats.put("activeDepartments", activeDepts);
        stats.put("pendingAppointments", pendingAppointments);
        stats.put("confirmedAppointments", confirmedAppointments);
        stats.put("completedAppointments", completedAppointments);
        stats.put("rescheduledAppointments", rescheduledAppointments);
        stats.put("cancelledAppointments", cancelledAppointments);
        stats.put("newEnquiries", newEnquiries);
        stats.put("newContactMessages", newContactMessages);
        stats.put("publishedBlogs", publishedBlogs);

        return ResponseEntity.ok(stats);
    }

    // DOCTOR MANAGEMENT
    @GetMapping("/doctors")
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().isBlank() ||
            doctor.getDepartmentId() == null || doctor.getDepartmentId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name and Department are required."));
        }
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());
        if (doctor.getStatus() == null) doctor.setStatus("ACTIVE");
        Doctor saved = doctorRepository.save(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable String id, @RequestBody Doctor doctorData) {
        return doctorRepository.findById(id).map(doctor -> {
            doctor.setName(doctorData.getName());
            doctor.setPhoto(doctorData.getPhoto());
            doctor.setQualification(doctorData.getQualification());
            doctor.setSpecialization(doctorData.getSpecialization());
            doctor.setDepartmentId(doctorData.getDepartmentId());
            doctor.setExperience(doctorData.getExperience());
            doctor.setDesignation(doctorData.getDesignation());
            doctor.setBio(doctorData.getBio());
            doctor.setConsultationTimings(doctorData.getConsultationTimings());
            doctor.setPhone(doctorData.getPhone());
            doctor.setStatus(doctorData.getStatus());
            doctor.setUpdatedAt(LocalDateTime.now());
            Doctor saved = doctorRepository.save(doctor);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable String id) {
        return doctorRepository.findById(id).map(doctor -> {
            doctorRepository.delete(doctor);
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DEPARTMENT MANAGEMENT
    @GetMapping("/departments")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Department dept) {
        if (dept.getName() == null || dept.getName().isBlank() ||
            dept.getSlug() == null || dept.getSlug().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name and Slug are required."));
        }
        // Verify unique slug
        if (departmentRepository.findBySlug(dept.getSlug()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Department with this slug already exists."));
        }
        dept.setCreatedAt(LocalDateTime.now());
        dept.setUpdatedAt(LocalDateTime.now());
        if (dept.getStatus() == null) dept.setStatus("ACTIVE");
        Department saved = departmentRepository.save(dept);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable String id, @RequestBody Department deptData) {
        return departmentRepository.findById(id).map(dept -> {
            // Verify unique slug if changed
            if (!dept.getSlug().equals(deptData.getSlug()) &&
                departmentRepository.findBySlug(deptData.getSlug()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Department with this slug already exists."));
            }
            dept.setName(deptData.getName());
            dept.setSlug(deptData.getSlug());
            dept.setDescription(deptData.getDescription());
            dept.setImage(deptData.getImage());
            dept.setServices(deptData.getServices());
            dept.setStatus(deptData.getStatus());
            dept.setUpdatedAt(LocalDateTime.now());
            Department saved = departmentRepository.save(dept);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable String id) {
        return departmentRepository.findById(id).map(dept -> {
            // Deactivate or delete. Let's delete but also set linked doctors to inactive or change status.
            departmentRepository.delete(dept);
            return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // APPOINTMENT MANAGEMENT
    @GetMapping("/appointments")
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable String id, @RequestBody Appointment data) {
        return appointmentRepository.findById(id).map(appt -> {
            String oldStatus = appt.getStatus();
            String oldDate = appt.getPreferredDate();
            String oldTime = appt.getPreferredTime();

            if (data.getStatus() != null) appt.setStatus(data.getStatus());
            if (data.getAdminNotes() != null) appt.setAdminNotes(data.getAdminNotes());
            if (data.getPreferredDate() != null) appt.setPreferredDate(data.getPreferredDate());
            if (data.getPreferredTime() != null) appt.setPreferredTime(data.getPreferredTime());
            appt.setUpdatedAt(LocalDateTime.now());

            String newStatus = appt.getStatus();
            boolean isConfirmAction = "CONFIRMED".equalsIgnoreCase(newStatus);
            
            if (isConfirmAction && appt.getConfirmationTimestamp() == null) {
                appt.setConfirmationTimestamp(LocalDateTime.now());
            }

            Appointment saved = appointmentRepository.save(appt);

            String doctorName = doctorRepository.findById(saved.getDoctorId()).map(Doctor::getName).orElse("Consulting Specialist");
            String deptName = departmentRepository.findById(saved.getDepartmentId()).map(Department::getName).orElse("Medical Centre");

            boolean emailSent = false;
            boolean pdfGenerated = false;
            String pdfError = null;
            String emailError = null;

            if (isConfirmAction) {
                try {
                    byte[] pdfBytes = pdfGeneratorService.generateAppointmentPdf(saved, doctorName, deptName);
                    String apptRef = saved.getAppointmentId() != null ? saved.getAppointmentId() : saved.getId();
                    String pdfPath = pdfGeneratorService.savePdfToDisk(apptRef, pdfBytes);
                    saved.setPdfFileReference(pdfPath);
                    saved = appointmentRepository.save(saved);
                    pdfGenerated = true;

                    emailSent = emailService.sendAppointmentConfirmedEmail(saved, doctorName, deptName, pdfBytes);
                    if (!emailSent) emailError = "Email dispatch failed. Email log recorded status FAILED.";
                } catch (Exception e) {
                    pdfError = "PDF Generation failed: " + e.getMessage();
                }
            } else if ("CANCELLED".equalsIgnoreCase(newStatus) && !"CANCELLED".equalsIgnoreCase(oldStatus)) {
                emailSent = emailService.sendAppointmentCancelledEmail(saved, doctorName, deptName, saved.getAdminNotes());
            } else if ("RESCHEDULED".equalsIgnoreCase(newStatus) || 
                      ("CONFIRMED".equalsIgnoreCase(newStatus) && (!saved.getPreferredDate().equals(oldDate) || !saved.getPreferredTime().equals(oldTime)))) {
                try {
                    byte[] pdfBytes = pdfGeneratorService.generateAppointmentPdf(saved, doctorName, deptName);
                    String apptRef = saved.getAppointmentId() != null ? saved.getAppointmentId() : saved.getId();
                    String pdfPath = pdfGeneratorService.savePdfToDisk(apptRef, pdfBytes);
                    saved.setPdfFileReference(pdfPath);
                    saved = appointmentRepository.save(saved);
                    pdfGenerated = true;

                    emailSent = emailService.sendAppointmentRescheduledEmail(saved, doctorName, deptName, oldDate, oldTime, pdfBytes);
                } catch (Exception e) {
                    pdfError = "PDF Generation failed: " + e.getMessage();
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("appointment", saved);
            response.put("emailStatus", emailSent ? "SENT" : "FAILED");
            response.put("pdfStatus", pdfGenerated ? "GENERATED" : (isConfirmAction ? "FAILED" : "N/A"));
            if (pdfError != null) response.put("pdfError", pdfError);
            if (emailError != null) response.put("emailError", emailError);

            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/appointments/{id}/resend-email")
    public ResponseEntity<?> resendConfirmationEmail(@PathVariable String id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            opt = appointmentRepository.findByAppointmentId(id);
        }
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = opt.get();
        if (!"CONFIRMED".equalsIgnoreCase(appt.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only CONFIRMED appointments can receive a confirmation email."));
        }

        String doctorName = doctorRepository.findById(appt.getDoctorId()).map(Doctor::getName).orElse("Consulting Specialist");
        String deptName = departmentRepository.findById(appt.getDepartmentId()).map(Department::getName).orElse("Medical Centre");

        try {
            byte[] pdfBytes = pdfGeneratorService.generateAppointmentPdf(appt, doctorName, deptName);
            String apptRef = appt.getAppointmentId() != null ? appt.getAppointmentId() : appt.getId();
            String pdfPath = pdfGeneratorService.savePdfToDisk(apptRef, pdfBytes);
            appt.setPdfFileReference(pdfPath);
            appointmentRepository.save(appt);

            boolean sent = emailService.sendAppointmentConfirmedEmail(appt, doctorName, deptName, pdfBytes);
            Map<String, Object> resp = new HashMap<>();
            resp.put("emailStatus", sent ? "SENT" : "FAILED");
            resp.put("pdfStatus", "GENERATED");
            resp.put("message", sent ? "Confirmation email re-sent successfully." : "Failed to dispatch email. Please check server mail settings.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error generating confirmation PDF: " + e.getMessage()));
        }
    }

    @GetMapping("/appointments/{id}/pdf")
    public ResponseEntity<?> downloadAppointmentPdf(@PathVariable String id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            opt = appointmentRepository.findByAppointmentId(id);
        }
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = opt.get();
        String doctorName = doctorRepository.findById(appt.getDoctorId()).map(Doctor::getName).orElse("Consulting Specialist");
        String deptName = departmentRepository.findById(appt.getDepartmentId()).map(Department::getName).orElse("Medical Centre");

        try {
            byte[] pdfBytes = pdfGeneratorService.generateAppointmentPdf(appt, doctorName, deptName);
            String apptRef = appt.getAppointmentId() != null ? appt.getAppointmentId() : appt.getId();
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"Appointment_" + apptRef + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to generate PDF: " + e.getMessage()));
        }
    }

    @GetMapping("/appointments/{id}/email-logs")
    public ResponseEntity<?> getAppointmentEmailLogs(@PathVariable String id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            opt = appointmentRepository.findByAppointmentId(id);
        }
        String refId = opt.map(a -> a.getAppointmentId() != null ? a.getAppointmentId() : a.getId()).orElse(id);
        List<EmailLog> logs = emailLogRepository.findByAppointmentIdOrderByCreatedAtDesc(refId);
        return ResponseEntity.ok(logs);
    }

    // ENQUIRY MANAGEMENT
    @GetMapping("/enquiries")
    public List<Enquiry> getAllEnquiries() {
        return enquiryRepository.findAll();
    }

    @PutMapping("/enquiries/{id}")
    public ResponseEntity<?> updateEnquiry(@PathVariable String id, @RequestBody Enquiry data) {
        return enquiryRepository.findById(id).map(enquiry -> {
            if (data.getStatus() != null) enquiry.setStatus(data.getStatus());
            if (data.getAdminNotes() != null) enquiry.setAdminNotes(data.getAdminNotes());
            enquiry.setUpdatedAt(LocalDateTime.now());
            Enquiry saved = enquiryRepository.save(enquiry);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // CONTACT MESSAGE MANAGEMENT
    @GetMapping("/contact")
    public List<ContactMessage> getAllContactMessages() {
        return contactMessageRepository.findAll();
    }

    @PutMapping("/contact/{id}")
    public ResponseEntity<?> updateContactMessage(@PathVariable String id, @RequestBody ContactMessage data) {
        return contactMessageRepository.findById(id).map(message -> {
            if (data.getStatus() != null) message.setStatus(data.getStatus());
            if (data.getAdminNotes() != null) message.setAdminNotes(data.getAdminNotes());
            message.setUpdatedAt(LocalDateTime.now());
            ContactMessage saved = contactMessageRepository.save(message);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/contact/{id}")
    public ResponseEntity<?> deleteContactMessage(@PathVariable String id) {
        return contactMessageRepository.findById(id).map(message -> {
            contactMessageRepository.delete(message);
            return ResponseEntity.ok(Map.of("message", "Contact message deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // BLOG MANAGEMENT
    @GetMapping("/blogs")
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    @PostMapping("/blogs")
    public ResponseEntity<?> createBlog(@RequestBody Blog blog) {
        if (blog.getTitle() == null || blog.getTitle().isBlank() ||
            blog.getSlug() == null || blog.getSlug().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title and Slug are required."));
        }
        if (blogRepository.findBySlug(blog.getSlug()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Blog article with this slug already exists."));
        }
        blog.setCreatedAt(LocalDateTime.now());
        blog.setUpdatedAt(LocalDateTime.now());
        if (blog.getStatus() == null) blog.setStatus("DRAFT");
        if ("PUBLISHED".equals(blog.getStatus()) && blog.getPublishedAt() == null) {
            blog.setPublishedAt(LocalDateTime.now());
        }
        Blog saved = blogRepository.save(blog);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/blogs/{id}")
    public ResponseEntity<?> updateBlog(@PathVariable String id, @RequestBody Blog blogData) {
        return blogRepository.findById(id).map(blog -> {
            if (!blog.getSlug().equals(blogData.getSlug()) &&
                blogRepository.findBySlug(blogData.getSlug()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Blog article with this slug already exists."));
            }
            blog.setTitle(blogData.getTitle());
            blog.setSlug(blogData.getSlug());
            blog.setFeaturedImage(blogData.getFeaturedImage());
            blog.setContent(blogData.getContent());
            blog.setAuthor(blogData.getAuthor());
            blog.setCategory(blogData.getCategory());
            blog.setSeoTitle(blogData.getSeoTitle());
            blog.setSeoDescription(blogData.getSeoDescription());
            
            // Handle publish timestamp logic
            if ("PUBLISHED".equals(blogData.getStatus()) && !"PUBLISHED".equals(blog.getStatus())) {
                blog.setPublishedAt(LocalDateTime.now());
            } else if ("DRAFT".equals(blogData.getStatus())) {
                blog.setPublishedAt(null);
            }
            blog.setStatus(blogData.getStatus());
            blog.setUpdatedAt(LocalDateTime.now());
            
            Blog saved = blogRepository.save(blog);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/blogs/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable String id) {
        return blogRepository.findById(id).map(blog -> {
            blogRepository.delete(blog);
            return ResponseEntity.ok(Map.of("message", "Blog deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // GALLERY MANAGEMENT
    @GetMapping("/gallery")
    public List<Gallery> getAllGalleryImages() {
        return galleryRepository.findAll();
    }

    @PostMapping("/gallery")
    public ResponseEntity<?> createGalleryImage(@RequestBody Gallery item) {
        if (item.getImage() == null || item.getImage().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Image asset is required."));
        }
        item.setUploadedAt(LocalDateTime.now());
        if (item.getStatus() == null) item.setStatus("ACTIVE");
        Gallery saved = galleryRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/gallery/{id}")
    public ResponseEntity<?> updateGalleryImage(@PathVariable String id, @RequestBody Gallery data) {
        return galleryRepository.findById(id).map(item -> {
            item.setImage(data.getImage());
            item.setTitle(data.getTitle());
            item.setCaption(data.getCaption());
            item.setCategory(data.getCategory());
            item.setStatus(data.getStatus());
            Gallery saved = galleryRepository.save(item);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/gallery/{id}")
    public ResponseEntity<?> deleteGalleryImage(@PathVariable String id) {
        return galleryRepository.findById(id).map(item -> {
            galleryRepository.delete(item);
            return ResponseEntity.ok(Map.of("message", "Gallery image deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // EMERGENCY DETAILS MANAGEMENT
    @PutMapping("/emergency")
    public ResponseEntity<?> updateEmergencyInfo(@RequestBody EmergencyInfo data) {
        List<EmergencyInfo> list = emergencyInfoRepository.findAll();
        EmergencyInfo info;
        if (list.isEmpty()) {
            info = new EmergencyInfo();
        } else {
            info = list.get(0);
        }
        info.setEmergencyNumber(data.getEmergencyNumber());
        info.setDescription(data.getDescription());
        info.setAddress(data.getAddress());
        info.setAvailability(data.getAvailability());
        info.setInstructions(data.getInstructions());
        info.setUpdatedAt(LocalDateTime.now());
        
        EmergencyInfo saved = emergencyInfoRepository.save(info);
        
        // Also sync emergency number to site settings for consistency
        List<SiteSettings> settingsList = siteSettingsRepository.findAll();
        if (!settingsList.isEmpty()) {
            SiteSettings settings = settingsList.get(0);
            settings.setEmergencyNumber(data.getEmergencyNumber());
            siteSettingsRepository.save(settings);
        }
        
        return ResponseEntity.ok(saved);
    }

    // WEBSITE SETTINGS MANAGEMENT
    @PutMapping("/settings")
    public ResponseEntity<?> updateSiteSettings(@RequestBody SiteSettings data) {
        List<SiteSettings> list = siteSettingsRepository.findAll();
        SiteSettings settings;
        if (list.isEmpty()) {
            settings = new SiteSettings();
        } else {
            settings = list.get(0);
        }
        settings.setHospitalName(data.getHospitalName());
        settings.setLogo(data.getLogo());
        settings.setAddress(data.getAddress());
        settings.setPhone(data.getPhone());
        settings.setEmail(data.getEmail());
        settings.setWorkingHours(data.getWorkingHours());
        settings.setSocialLinks(data.getSocialLinks());
        settings.setMapInformation(data.getMapInformation());
        settings.setMapLink(data.getMapLink());
        settings.setEmergencyNumber(data.getEmergencyNumber());
        settings.setCity(data.getCity());
        settings.setState(data.getState());
        settings.setCountry(data.getCountry());
        settings.setPincode(data.getPincode());
        settings.setLatitude(data.getLatitude());
        settings.setLongitude(data.getLongitude());
        settings.setGoogleMapsUrl(data.getGoogleMapsUrl());
        settings.setGoogleMapsDirectionsUrl(data.getGoogleMapsDirectionsUrl());
        settings.setGooglePlaceId(data.getGooglePlaceId());
        settings.setGoogleMapsApiKey(data.getGoogleMapsApiKey());
        settings.setUpdatedAt(LocalDateTime.now());
        
        SiteSettings saved = siteSettingsRepository.save(settings);
        
        // Also sync emergency number to emergency info for consistency
        List<EmergencyInfo> emergencyList = emergencyInfoRepository.findAll();
        if (!emergencyList.isEmpty()) {
            EmergencyInfo info = emergencyList.get(0);
            info.setEmergencyNumber(data.getEmergencyNumber());
            emergencyInfoRepository.save(info);
        }
        
        return ResponseEntity.ok(saved);
    }

    // EVENT MANAGEMENT
    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        if (event.getTitle() == null || event.getTitle().isBlank() ||
            event.getEventDate() == null || event.getEventDate().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title and Event Date are required."));
        }
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        Event saved = eventRepository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @RequestBody Event eventData) {
        return eventRepository.findById(id).map(event -> {
            event.setTitle(eventData.getTitle());
            event.setEventDate(eventData.getEventDate());
            event.setEventTime(eventData.getEventTime());
            event.setEventType(eventData.getEventType());
            event.setDescription(eventData.getDescription());
            event.setActive(eventData.isActive());
            event.setPopupEnabled(eventData.isPopupEnabled());
            event.setDisplayOrder(eventData.getDisplayOrder());
            event.setUpdatedAt(LocalDateTime.now());
            Event saved = eventRepository.save(event);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id) {
        return eventRepository.findById(id).map(event -> {
            eventRepository.delete(event);
            return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
