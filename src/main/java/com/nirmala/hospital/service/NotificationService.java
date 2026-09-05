package com.nirmala.hospital.service;

import com.nirmala.hospital.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    public void sendAppointmentNotifications(Appointment appointment, String doctorName, String departmentName) {
        String patientEmail = appointment.getEmail();
        String patientPhone = appointment.getPhone();
        String date = appointment.getPreferredDate();
        String time = appointment.getPreferredTime();

        String emailSubject = "Appointment Request Received - Nirmala Hospital";
        String emailMessage = String.format(
                "Dear %s,\n\n" +
                "Thank you for requesting an appointment with Nirmala Neuro & General Medical Centre.\n\n" +
                "Appointment Details:\n" +
                "- Doctor: %s\n" +
                "- Department: %s\n" +
                "- Date: %s\n" +
                "- Time: %s\n" +
                "- Status: PENDING (Our team will contact you shortly to confirm)\n\n" +
                "If you need to reschedule or cancel, please call our emergency/support desk.\n\n" +
                "Best regards,\nNirmala Medical Team",
                appointment.getPatientName(), doctorName, departmentName, date, time
        );

        String smsMessage = String.format(
                "Nirmala Hospital: Dear %s, your appointment request with %s on %s at %s is received. Status: PENDING.",
                appointment.getPatientName(), doctorName, date, time
        );

        String whatsappMessage = String.format(
                "Hello %s,\nYour appointment booking request has been registered at *Nirmala Neuro & General Medical Centre*.\n\n" +
                "*Doctor:* %s\n" +
                "*Date:* %s\n" +
                "*Time:* %s\n" +
                "*Status:* PENDING\n\n" +
                "We will call you shortly to confirm. Thank you for choosing us!",
                appointment.getPatientName(), doctorName, date, time
        );

        // Simulation/Console Logging
        logger.info("====== SIMULATED EMAIL SENT TO {} ======", patientEmail);
        logger.info("Subject: {}", emailSubject);
        logger.info("Body:\n{}", emailMessage);
        logger.info("=========================================");

        logger.info("====== SIMULATED SMS SENT TO {} ======", patientPhone);
        logger.info("Body: {}", smsMessage);
        logger.info("=========================================");

        logger.info("====== SIMULATED WHATSAPP SENT TO {} ======", patientPhone);
        logger.info("Body: {}", whatsappMessage);
        logger.info("=========================================");

        // In a real application, integration with SendGrid, Twilio, etc. happens here.
        if (emailEnabled) {
            // Integration code with JavaMailSender
        }
        if (smsEnabled) {
            // Integration code with Twilio SDK
        }
        if (whatsappEnabled) {
            // Integration code with Twilio WhatsApp Business API
        }
    }

    public void sendAdminEnquiryNotification(String patientName, String subject, String message) {
        logger.info("====== ADMIN NOTIFICATION: NEW PATIENT ENQUIRY ======");
        logger.info("From: {}", patientName);
        logger.info("Subject: {}", subject);
        logger.info("Message: {}", message);
        logger.info("====================================================");
    }

    public void sendAdminContactNotification(String name, String subject, String message) {
        logger.info("====== ADMIN NOTIFICATION: NEW CONTACT MESSAGE ======");
        logger.info("From: {}", name);
        logger.info("Subject: {}", subject);
        logger.info("Message: {}", message);
        logger.info("====================================================");
    }
}
