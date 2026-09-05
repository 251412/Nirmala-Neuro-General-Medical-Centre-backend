package com.nirmala.hospital.controller;

import com.nirmala.hospital.model.*;
import com.nirmala.hospital.repository.*;
import com.nirmala.hospital.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final EnquiryRepository enquiryRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final BlogRepository blogRepository;
    private final GalleryRepository galleryRepository;
    private final EmergencyInfoRepository emergencyInfoRepository;
    private final SiteSettingsRepository siteSettingsRepository;
    private final NotificationService notificationService;
    private final com.nirmala.hospital.service.EmailService emailService;
    private final EventRepository eventRepository;

    public PublicController(DoctorRepository doctorRepository, DepartmentRepository departmentRepository,
                            AppointmentRepository appointmentRepository, EnquiryRepository enquiryRepository,
                            ContactMessageRepository contactMessageRepository, BlogRepository blogRepository,
                            GalleryRepository galleryRepository, EmergencyInfoRepository emergencyInfoRepository,
                            SiteSettingsRepository siteSettingsRepository, NotificationService notificationService,
                            com.nirmala.hospital.service.EmailService emailService,
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
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.eventRepository = eventRepository;
    }

    // Departments
    @GetMapping("/departments")
    public List<Department> getActiveDepartments() {
        return departmentRepository.findByStatus("ACTIVE");
    }

    @GetMapping("/departments/{slug}")
    public ResponseEntity<?> getDepartmentBySlug(@PathVariable String slug) {
        Optional<Department> deptOpt = departmentRepository.findBySlug(slug);
        if (deptOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Department dept = deptOpt.get();
        List<Doctor> doctors = doctorRepository.findByDepartmentIdAndStatus(dept.getId(), "ACTIVE");
        
        Map<String, Object> response = new HashMap<>();
        response.put("department", dept);
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    // Doctors
    @GetMapping("/doctors")
    public List<Doctor> getAllDoctors(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String search) {
        
        boolean hasDept = departmentId != null && !departmentId.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        if (hasDept && hasSearch) {
            return doctorRepository.searchAllDoctorsInDepartment(search, departmentId);
        } else if (hasDept) {
            return doctorRepository.findByDepartmentId(departmentId);
        } else if (hasSearch) {
            return doctorRepository.searchAllDoctors(search);
        } else {
            return doctorRepository.findAll();
        }
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        return doctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Appointments
    @GetMapping("/appointments/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable String id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            opt = appointmentRepository.findByAppointmentId(id);
        }
        Appointment app;
        if (opt.isPresent()) {
            app = opt.get();
        } else {
            List<Appointment> byPhone = appointmentRepository.findByPhone(id);
            if (!byPhone.isEmpty()) {
                app = byPhone.get(byPhone.size() - 1);
            } else {
                List<Appointment> byEmail = appointmentRepository.findByEmail(id);
                if (!byEmail.isEmpty()) {
                    app = byEmail.get(byEmail.size() - 1);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", app.getId());
        resp.put("appointmentId", app.getAppointmentId() != null ? app.getAppointmentId() : app.getId());
        resp.put("patientName", app.getPatientName());
        resp.put("age", app.getAge());
        resp.put("gender", app.getGender());
        resp.put("phone", app.getPhone());
        resp.put("email", app.getEmail());
        resp.put("preferredDate", app.getPreferredDate());
        resp.put("preferredTime", app.getPreferredTime());
        resp.put("status", app.getStatus());
        resp.put("adminNotes", app.getAdminNotes());
        resp.put("reason", app.getReason());
        resp.put("createdAt", app.getCreatedAt());

        doctorRepository.findById(app.getDoctorId()).ifPresent(d -> {
            resp.put("doctorName", d.getName());
            resp.put("doctorDesignation", d.getDesignation());
            resp.put("doctorQualification", d.getQualification());
            resp.put("doctorPhoto", d.getPhoto());
        });

        departmentRepository.findById(app.getDepartmentId()).ifPresent(dept -> {
            resp.put("departmentName", dept.getName());
        });

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment) {
        // Validation
        if (appointment.getPatientName() == null || appointment.getPatientName().isBlank() ||
            appointment.getPhone() == null || appointment.getPhone().isBlank() ||
            appointment.getEmail() == null || appointment.getEmail().isBlank() ||
            appointment.getDepartmentId() == null || appointment.getDepartmentId().isBlank() ||
            appointment.getDoctorId() == null || appointment.getDoctorId().isBlank() ||
            appointment.getPreferredDate() == null || appointment.getPreferredDate().isBlank() ||
            appointment.getPreferredTime() == null || appointment.getPreferredTime().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required."));
        }

        // Verify date is not in past
        try {
            LocalDate date = LocalDate.parse(appointment.getPreferredDate());
            if (date.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Preferred date cannot be in the past."));
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid preferred date format. Must be YYYY-MM-DD."));
        }

        // Verify doctor exists and belongs to the specified department
        Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctorId());
        if (doctorOpt.isEmpty() || !"ACTIVE".equals(doctorOpt.get().getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Selected doctor is not available."));
        }
        Doctor doctor = doctorOpt.get();
        if (!doctor.getDepartmentId().equals(appointment.getDepartmentId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "The selected doctor does not belong to the selected department."));
        }

        Optional<Department> departmentOpt = departmentRepository.findById(appointment.getDepartmentId());
        if (departmentOpt.isEmpty() || !"ACTIVE".equals(departmentOpt.get().getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Selected department is not available."));
        }
        Department department = departmentOpt.get();

        // Check for double bookings
        List<Appointment> existingBookings = appointmentRepository.findByPreferredDateAndDoctorIdAndPreferredTime(
                appointment.getPreferredDate(), appointment.getDoctorId(), appointment.getPreferredTime()
        );
        boolean doubleBooked = existingBookings.stream()
                .anyMatch(a -> !"CANCELLED".equals(a.getStatus()));
        
        if (doubleBooked) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "The selected time slot is already booked for this doctor. Please choose a different time slot or date."
            ));
        }

        // Format unique Appointment Reference Number: NM-2026-XXXX
        long count = appointmentRepository.count() + 1001;
        appointment.setAppointmentId(String.format("NM-2026-%04d", count));
        appointment.setStatus("PENDING");
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);

        // Send notifications and pending email
        notificationService.sendAppointmentNotifications(saved, doctor.getName(), department.getName());
        emailService.sendAppointmentPendingEmail(saved, doctor.getName(), department.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Enquiries
    @PostMapping("/enquiries")
    public ResponseEntity<?> submitEnquiry(@RequestBody Enquiry enquiry) {
        if (enquiry.getPatientName() == null || enquiry.getPatientName().isBlank() ||
            enquiry.getPhone() == null || enquiry.getPhone().isBlank() ||
            enquiry.getEmail() == null || enquiry.getEmail().isBlank() ||
            enquiry.getSubject() == null || enquiry.getSubject().isBlank() ||
            enquiry.getMessage() == null || enquiry.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required."));
        }

        enquiry.setStatus("NEW");
        enquiry.setCreatedAt(LocalDateTime.now());
        enquiry.setUpdatedAt(LocalDateTime.now());
        Enquiry saved = enquiryRepository.save(enquiry);

        notificationService.sendAdminEnquiryNotification(saved.getPatientName(), saved.getSubject(), saved.getMessage());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Contact Us
    @PostMapping("/contact")
    public ResponseEntity<?> submitContactMessage(@RequestBody ContactMessage message) {
        if (message.getName() == null || message.getName().isBlank() ||
            message.getPhone() == null || message.getPhone().isBlank() ||
            message.getEmail() == null || message.getEmail().isBlank() ||
            message.getSubject() == null || message.getSubject().isBlank() ||
            message.getMessage() == null || message.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required."));
        }

        message.setStatus("NEW");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        ContactMessage saved = contactMessageRepository.save(message);

        notificationService.sendAdminContactNotification(saved.getName(), saved.getSubject(), saved.getMessage());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Blogs
    @GetMapping("/blogs")
    public List<Blog> getPublishedBlogs(@RequestParam(required = false) String category,
                                        @RequestParam(required = false) String search) {
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        if (hasCategory && hasSearch) {
            // Find by status and category, then filter search locally or use custom query.
            // For simplicity, search active blogs and filter by category or just load and filter.
            List<Blog> list = blogRepository.searchActiveBlogs(search);
            return list.stream().filter(b -> category.equalsIgnoreCase(b.getCategory())).toList();
        } else if (hasCategory) {
            return blogRepository.findByStatusAndCategory("PUBLISHED", category);
        } else if (hasSearch) {
            return blogRepository.searchActiveBlogs(search);
        } else {
            return blogRepository.findByStatus("PUBLISHED");
        }
    }

    @GetMapping("/blogs/{slug}")
    public ResponseEntity<Blog> getBlogBySlug(@PathVariable String slug) {
        return blogRepository.findBySlug(slug)
                .filter(b -> "PUBLISHED".equals(b.getStatus()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Gallery
    @GetMapping("/gallery")
    public List<Gallery> getActiveGallery(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return galleryRepository.findByStatusAndCategory("ACTIVE", category);
        }
        return galleryRepository.findByStatus("ACTIVE");
    }

    // Emergency Info
    @GetMapping("/emergency")
    public ResponseEntity<EmergencyInfo> getEmergencyInfo() {
        List<EmergencyInfo> list = emergencyInfoRepository.findAll();
        if (list.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list.get(0));
    }

    // Site Settings
    @GetMapping("/settings")
    public ResponseEntity<SiteSettings> getSiteSettings() {
        List<SiteSettings> list = siteSettingsRepository.findAll();
        if (list.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list.get(0));
    }

    // Upcoming Events — returns the next single upcoming active popup event
    @GetMapping("/events/upcoming")
    public ResponseEntity<?> getUpcomingEvent() {
        String today = LocalDate.now().toString(); // "YYYY-MM-DD"
        List<Event> events = eventRepository.findByActiveTrueAndPopupEnabledTrueOrderByEventDateAsc();
        Optional<Event> upcoming = events.stream()
                .filter(e -> e.getEventDate() != null && e.getEventDate().compareTo(today) >= 0)
                .findFirst();
        if (upcoming.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(upcoming.get());
    }

    // SMTP Diagnostic Test Endpoint
    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam(defaultValue = "nirmalaneurocarevzm@gmail.com") String to) {
        Map<String, Object> result = emailService.testSmtpConnection(to);
        return ResponseEntity.ok(result);
    }
}
