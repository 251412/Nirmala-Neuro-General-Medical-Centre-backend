package com.nirmala.hospital.config;

import com.nirmala.hospital.model.*;
import com.nirmala.hospital.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final BlogRepository blogRepository;
    private final GalleryRepository galleryRepository;
    private final EmergencyInfoRepository emergencyInfoRepository;
    private final SiteSettingsRepository siteSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(AdminRepository adminRepository, DepartmentRepository departmentRepository,
                          DoctorRepository doctorRepository, BlogRepository blogRepository,
                          GalleryRepository galleryRepository, EmergencyInfoRepository emergencyInfoRepository,
                          SiteSettingsRepository siteSettingsRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.blogRepository = blogRepository;
        this.galleryRepository = galleryRepository;
        this.emergencyInfoRepository = emergencyInfoRepository;
        this.siteSettingsRepository = siteSettingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedDepartmentsAndDoctors();
        seedBlogs();
        seedGallery();
        seedEmergencyInfo();
        seedSiteSettings();
    }

    private void seedAdmin() {
        Admin admin = adminRepository.findByEmail("nirmalaneurocare@gmail.com").orElse(null);
        if (admin == null) {
            admin = Admin.builder()
                    .name("Hospital Administrator")
                    .email("nirmalaneurocare@gmail.com")
                    .passwordHash(passwordEncoder.encode("NirmalaAdmin2026!"))
                    .role("ADMIN")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            adminRepository.save(admin);
            System.out.println("[DB Seed] Seeded default Admin user: nirmalaneurocare@gmail.com");
        } else {
            admin.setPasswordHash(passwordEncoder.encode("NirmalaAdmin2026!"));
            admin.setUpdatedAt(LocalDateTime.now());
            adminRepository.save(admin);
            System.out.println("[DB Seed] Updated default Admin password for: nirmalaneurocare@gmail.com");
        }
    }

    private void seedDepartmentsAndDoctors() {
        if (departmentRepository.count() == 0) {
            // Seed Departments
            Department neuro = Department.builder()
                    .name("Neurology & Neurosurgery")
                    .slug("neurology")
                    .description("Advanced diagnosis and surgical/non-surgical treatment of disorders of the brain, spinal cord, and nervous system.")
                    .image("https://images.unsplash.com/photo-1559757175-5700dde675bc?auto=format&fit=crop&q=80&w=800")
                    .services(Arrays.asList("Stroke Management", "Epilepsy Clinic", "Spine Surgery", "Neuropathy & Headaches", "Electroencephalogram (EEG)"))
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Department medicine = Department.builder()
                    .name("General Medicine")
                    .slug("general-medicine")
                    .description("Comprehensive primary care, chronic disease management, preventive medicine, and diagnostic workups for adult healthcare.")
                    .image("https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&q=80&w=800")
                    .services(Arrays.asList("Hypertension Treatment", "Diabetes Care", "Infectious Diseases", "Geriatric Care", "General Health Screenings"))
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Department cardio = Department.builder()
                    .name("Cardiology")
                    .slug("cardiology")
                    .description("State-of-the-art diagnostic, preventive, and therapeutic cardiac services for coronary diseases, heart failure, and arrhythmias.")
                    .image("https://images.unsplash.com/photo-1628348068343-c6a848d2b6dd?auto=format&fit=crop&q=80&w=800")
                    .services(Arrays.asList("Echocardiography (ECG/Echo)", "Treadmill Test (TMT)", "Hypertension Clinic", "Heart Failure Management"))
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            neuro = departmentRepository.save(neuro);
            medicine = departmentRepository.save(medicine);
            cardio = departmentRepository.save(cardio);
            System.out.println("[DB Seed] Seeded 3 Departments");

            // Seed/Update Doctors
            doctorRepository.findAll().forEach(doc -> {
                if (doc.getName() != null && doc.getName().toLowerCase().contains("nirmala")) {
                    doc.setPhoto("/uploads/dr_nirmala_vangapandu.png");
                    doctorRepository.save(doc);
                }
            });

            if (doctorRepository.count() == 0) {
                Doctor d1 = Doctor.builder()
                        .name("Dr Vangapandu Nirmala")
                        .photo("/uploads/dr_nirmala_vangapandu.png")
                        .qualification("MD, DM (Neurology)")
                        .specialization("Stroke Care, Brain Tumors, & Epilepsy Treatment")
                        .departmentId(neuro.getId())
                        .experience("18 Years")
                        .designation("Director & Head of Neurology")
                        .bio("Dr Vangapandu Nirmala is a highly respected neurologist with nearly two decades of experience in treating complex brain disorders. She is dedicated to patient-centered neurological rehabilitation.")
                        .consultationTimings(Arrays.asList("Mon-Wed: 10:00 AM - 01:00 PM", "Fri: 10:00 AM - 01:00 PM"))
                        .phone("+91 98765 43210")
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                Doctor d2 = Doctor.builder()
                        .name("Dr. Rajesh Kumar")
                        .photo("https://images.unsplash.com/photo-1622253692010-333f2da6031d?auto=format&fit=crop&q=80&w=600")
                        .qualification("MD (General Medicine)")
                        .specialization("Diabetology, Hypertension & Chronic Fever Management")
                        .departmentId(medicine.getId())
                        .experience("14 Years")
                        .designation("Senior Consultant Physician")
                        .bio("Dr. Rajesh Kumar specializes in internal medicine and lifestyle diseases. He helps patients optimize their health parameters through comprehensive pharmacological and dietetic plans.")
                        .consultationTimings(Arrays.asList("Mon-Sat: 09:00 AM - 12:00 PM", "Mon-Fri: 04:00 PM - 06:00 PM"))
                        .phone("+91 98765 43211")
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                Doctor d3 = Doctor.builder()
                        .name("Dr. Anjali Sharma")
                        .photo("https://images.unsplash.com/photo-1594824813573-246434de83fb?auto=format&fit=crop&q=80&w=600")
                        .qualification("MD, DM (Cardiology)")
                        .specialization("Preventive Cardiology, Valvular Disease & Echo Diagnostics")
                        .departmentId(cardio.getId())
                        .experience("10 Years")
                        .designation("Consultant Cardiologist")
                        .bio("Dr. Anjali Sharma has worked with leading medical institutes. She specialises in non-invasive cardiac checkups, cardiac failure management, and lipid control therapies.")
                        .consultationTimings(Arrays.asList("Tue-Thu: 11:00 AM - 02:00 PM", "Sat: 09:00 AM - 01:00 PM"))
                        .phone("+91 98765 43212")
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                doctorRepository.saveAll(Arrays.asList(d1, d2, d3));
                System.out.println("[DB Seed] Seeded 3 Doctors");
            }
        }
    }

    private void seedBlogs() {
        if (blogRepository.count() == 0) {
            Blog b1 = Blog.builder()
                    .title("Understanding Stroke: Warning Signs and Urgent Prevention")
                    .slug("understanding-stroke-warning-signs")
                    .featuredImage("https://images.unsplash.com/photo-1505751172876-fa1923c5c528?auto=format&fit=crop&q=80&w=800")
                    .content("<h2>What is a Stroke?</h2><p>A stroke occurs when blood flow to a part of the brain is interrupted or reduced, preventing brain tissue from getting oxygen and nutrients. Brain cells begin to die in minutes.</p><h3>Recognize the F.A.S.T. Signs:</h3><ul><li><strong>Face Drooping:</strong> Does one side of the face droop or is it numb?</li><li><strong>Arm Weakness:</strong> Is one arm weak or numb? Ask the person to raise both arms. Does one arm drift downward?</li><li><strong>Speech Difficulty:</strong> Is speech slurred? Is the person unable to speak or hard to understand?</li><li><strong>Time to Call Emergency:</strong> If someone shows any of these symptoms, call our emergency hotline immediately.</li></ul><p>Every second counts during a stroke. Seeking immediate medical care minimizes brain damage and improves recovery prospects.</p>")
                    .author("Dr Vangapandu Nirmala")
                    .category("Neurology")
                    .seoTitle("Understanding Stroke Symptoms & Warning Signs | Nirmala Hospital")
                    .seoDescription("Learn the FAST signs of a stroke, what causes brain stroke, and the importance of immediate care. By Chief Neurologist Dr Vangapandu Nirmala.")
                    .status("PUBLISHED")
                    .publishedAt(LocalDateTime.now().minusDays(10))
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .updatedAt(LocalDateTime.now().minusDays(10))
                    .build();

            Blog b2 = Blog.builder()
                    .title("Managing Hypertension: Essential Lifestyle Tips and Medical Control")
                    .slug("managing-hypertension-lifestyle-tips")
                    .featuredImage("https://images.unsplash.com/photo-1530026405186-ed1ea0ac7a63?auto=format&fit=crop&q=80&w=800")
                    .content("<h2>Living with High Blood Pressure</h2><p>Hypertension is often called the silent killer because it typically has no warning symptoms. Uncontrolled high blood pressure increases the risk of heart attacks, stroke, and kidney failure.</p><h3>Steps to Lower Blood Pressure Naturally:</h3><ol><li><strong>Reduce Salt Intake:</strong> Limit sodium intake to less than 2,000 mg per day.</li><li><strong>Exercise Regularly:</strong> Aim for 30 minutes of moderate aerobic activity like brisk walking 5 days a week.</li><li><strong>Eat a DASH Diet:</strong> Focus on whole grains, fruits, vegetables, and low-fat dairy products.</li><li><strong>Monitor at Home:</strong> Keep a regular log of your blood pressure readings to show your physician.</li></ol><p>Always consult your primary care doctor before starting new fitness regimens or modifying prescribed hypertensive medications.</p>")
                    .author("Dr. Rajesh Kumar")
                    .category("General Health")
                    .seoTitle("How to Manage High Blood Pressure (Hypertension) | Nirmala Hospital")
                    .seoDescription("Discover lifestyle changes and medical advice to control hypertension and reduce cardiovascular risks. Written by Dr. Rajesh Kumar.")
                    .status("PUBLISHED")
                    .publishedAt(LocalDateTime.now().minusDays(5))
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .updatedAt(LocalDateTime.now().minusDays(5))
                    .build();

            blogRepository.saveAll(Arrays.asList(b1, b2));
            System.out.println("[DB Seed] Seeded 2 Blog articles");
        }
    }

    private void seedGallery() {
        if (galleryRepository.count() == 0) {
            Gallery g1 = Gallery.builder()
                    .image("https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?auto=format&fit=crop&q=80&w=800")
                    .title("Main Reception Lobby")
                    .caption("Spacious and welcoming outpatient reception desk designed for patient comfort.")
                    .category("Facilities")
                    .status("ACTIVE")
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Gallery g2 = Gallery.builder()
                    .image("https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&q=80&w=800")
                    .title("Neurological Consultation Chamber")
                    .caption("Equipped with neurological examination tools and comfortable consulting environment.")
                    .category("Facilities")
                    .status("ACTIVE")
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Gallery g3 = Gallery.builder()
                    .image("https://images.unsplash.com/photo-1579684385127-1ef15d508118?auto=format&fit=crop&q=80&w=800")
                    .title("Diagnostic Laboratory Room")
                    .caption("Equipped with high-end diagnostic tools for blood counts, biochemistry, and general pathology.")
                    .category("Equipment")
                    .status("ACTIVE")
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Gallery g4 = Gallery.builder()
                    .image("https://images.unsplash.com/photo-1581594693702-fbdc51b2763b?auto=format&fit=crop&q=80&w=800")
                    .title("General Medical Ward")
                    .caption("Clean, sanitised, and ventilated general wards monitored 24/7 by our medical staff.")
                    .category("Wards")
                    .status("ACTIVE")
                    .uploadedAt(LocalDateTime.now())
                    .build();

            galleryRepository.saveAll(Arrays.asList(g1, g2, g3, g4));
            System.out.println("[DB Seed] Seeded 4 Gallery Images");
        }
    }

    private void seedEmergencyInfo() {
        if (emergencyInfoRepository.count() == 0) {
            EmergencyInfo info = EmergencyInfo.builder()
                    .emergencyNumber("6305471147 / 6302963312")
                    .description("For immediate assistance during a neurological episode or medical emergency, reach our emergency desk. We provide ambulance support and round-the-clock doctor availability.")
                    .address("Back of INOX Multiplex, Opposite RTC Complex, Fort Area, Vizianagaram, Andhra Pradesh - 535002")
                    .availability("24 Hours a Day, 7 Days a Week")
                    .instructions(Arrays.asList(
                            "Do not panic. Stay calm and assess the situation.",
                            "If the patient is unconscious, turn them onto their side (recovery position).",
                            "Call our hotline 6305471147 / 6302963312 directly. State the patient's condition and exact location.",
                            "Do not offer water or food to an unconscious patient.",
                            "Keep any medical history or prescription files ready for the arriving paramedics."
                    ))
                    .updatedAt(LocalDateTime.now())
                    .build();
            emergencyInfoRepository.save(info);
            System.out.println("[DB Seed] Seeded Emergency Information");
        }
    }

    private void seedSiteSettings() {
        String mapsUrl = "https://www.google.com/maps/place/Nirmala+Neuro+%26+General+Medical+Centre/@18.1068518,83.3932009,17z/data=!4m14!1m7!3m6!1s0x3a3be504cd790a65:0xe2fae04c868b4d7!2sNirmala+Neuro+%26+General+Medical+Centre!8m2!3d18.1068468!4d83.3980718!16s%2Fg%2F11shr_nqs7!3m5!1s0x3a3be504cd790a65:0xe2fae04c868b4d7!8m2!3d18.1068468!4d83.3980718!16s%2Fg%2F11shr_nqs7?entry=ttu";
        String directionsUrl = "https://www.google.com/maps/dir/?api=1&destination=18.1068468,83.3980718&destination_place_id=ChIJZQq5zUT1OzoR102LhkzA-uI";
        String embedUrl = "https://maps.google.com/maps?q=18.1068468,83.3980718+(Nirmala+Neuro+%26+General+Medical+Centre)&t=m&z=17&ie=UTF8&iwloc=B&output=embed";

        if (siteSettingsRepository.count() == 0) {
            Map<String, String> social = new HashMap<>();
            social.put("facebook", "https://facebook.com/nirmalahospital");
            social.put("twitter", "https://twitter.com/nirmalahospital");
            social.put("linkedin", "https://linkedin.com/company/nirmala-neuro-general");
            social.put("youtube", "https://youtube.com/nirmalahospital");

            SiteSettings settings = SiteSettings.builder()
                    .hospitalName("Nirmala Neuro & General Medical Centre")
                    .logo("NIRMALA HOSPITAL")
                    .address("Back of INOX Multiplex, Opposite RTC Complex, Fort Area, Vizianagaram, Andhra Pradesh - 535003")
                    .phone("6305471147 / 6302963312")
                    .email("nirmalaneurocare@gmail.com")
                    .workingHours("Sunday to Friday: 09:30 AM to 6:30 PM | Saturday: Closed")
                    .socialLinks(social)
                    .mapInformation(embedUrl)
                    .mapLink(mapsUrl)
                    .emergencyNumber("6305471147 / 6302963312")
                    .city("Vizianagaram")
                    .state("Andhra Pradesh")
                    .country("India")
                    .pincode("535003")
                    .latitude(18.1068468)
                    .longitude(83.3980718)
                    .googleMapsUrl(mapsUrl)
                    .googleMapsDirectionsUrl(directionsUrl)
                    .googlePlaceId("ChIJZQq5zUT1OzoR102LhkzA-uI")
                    .updatedAt(LocalDateTime.now())
                    .build();
            siteSettingsRepository.save(settings);
            System.out.println("[DB Seed] Seeded Site Settings");
        } else {
            // Update existing settings to ensure coordinates and location fields are populated
            List<SiteSettings> settingsList = siteSettingsRepository.findAll();
            if (!settingsList.isEmpty()) {
                SiteSettings settings = settingsList.get(0);
                settings.setHospitalName("Nirmala Neuro & General Medical Centre");
                settings.setAddress("Back of INOX Multiplex, Opposite RTC Complex, Fort Area, Vizianagaram, Andhra Pradesh - 535003");
                settings.setCity("Vizianagaram");
                settings.setState("Andhra Pradesh");
                settings.setCountry("India");
                settings.setPincode("535003");
                settings.setLatitude(18.1068468);
                settings.setLongitude(83.3980718);
                settings.setGoogleMapsUrl(mapsUrl);
                settings.setGoogleMapsDirectionsUrl(directionsUrl);
                settings.setGooglePlaceId("ChIJZQq5zUT1OzoR102LhkzA-uI");
                settings.setMapInformation(embedUrl);
                settings.setMapLink(mapsUrl);
                settings.setUpdatedAt(LocalDateTime.now());
                siteSettingsRepository.save(settings);
                System.out.println("[DB Seed] Updated Site Settings with exact hospital location & coordinates");
            }
        }
    }
}
