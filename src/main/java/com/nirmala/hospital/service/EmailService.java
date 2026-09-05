package com.nirmala.hospital.service;

import com.nirmala.hospital.model.Appointment;
import com.nirmala.hospital.model.EmailLog;
import com.nirmala.hospital.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Value("${MAIL_FROM:${spring.mail.username:nirmalaneurocarevzm@gmail.com}}")
    private String fromEmail;

    @Value("${MAIL_FROM_NAME:Nirmala Neuro & General Medical Centre}")
    private String fromName;

    @Value("${BREVO_API_KEY:${MAIL_API_KEY:}}")
    private String brevoApiKey;

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendAppointmentPendingEmail(Appointment appointment, String doctorName, String departmentName) {
        String apptRef = appointment.getAppointmentId() != null ? appointment.getAppointmentId() : appointment.getId();
        String subject = "Appointment Request Received - Nirmala Neuro & General Medical Centre";
        String emailType = "APPOINTMENT_PENDING";

        String reasonDisplay = (appointment.getReason() != null && !appointment.getReason().isBlank()) 
                ? appointment.getReason() 
                : "General Consultation";

        String htmlContent = "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; }"
                + ".card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08); border: 1px solid #e2e8f0; }"
                + ".header { background: #0f4c81; color: #ffffff; padding: 24px; text-align: center; }"
                + ".header h2 { margin: 0; font-size: 20px; font-weight: 700; }"
                + ".body-content { padding: 28px; color: #1e293b; font-size: 15px; line-height: 1.6; }"
                + ".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; background: #f8fafc; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }"
                + ".details-table td { padding: 12px 16px; border-bottom: 1px solid #edf2f7; font-size: 14px; }"
                + ".details-table td.label { font-weight: 600; color: #64748b; width: 40%; }"
                + ".details-table td.value { font-weight: 600; color: #0f172a; }"
                + ".badge { display: inline-block; padding: 4px 12px; border-radius: 20px; color: #ffffff; background-color: #f59e0b; font-size: 12px; font-weight: 700; text-transform: uppercase; }"
                + ".footer { background: #f1f5f9; color: #64748b; text-align: center; padding: 16px; font-size: 13px; border-top: 1px solid #e2e8f0; }"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div class='header'><h2>Nirmala Neuro & General Medical Centre</h2></div>"
                + "<div class='body-content'>"
                + "<p>Dear " + escapeHtml(appointment.getPatientName()) + ",</p>"
                + "<p>Your appointment request has been successfully received.</p>"
                + "<p>Your appointment is currently <strong>PENDING</strong> and is waiting for confirmation from our hospital.</p>"
                + "<table class='details-table'>"
                + "<tr><td class='label'>Appointment ID:</td><td class='value' style='font-family:monospace; font-size:15px; color:#0f4c81;'>" + apptRef + "</td></tr>"
                + "<tr><td class='label'>Patient Name:</td><td class='value'>" + escapeHtml(appointment.getPatientName()) + "</td></tr>"
                + "<tr><td class='label'>Department:</td><td class='value'>" + escapeHtml(departmentName) + "</td></tr>"
                + "<tr><td class='label'>Doctor:</td><td class='value'>" + escapeHtml(doctorName) + "</td></tr>"
                + "<tr><td class='label'>Preferred Date:</td><td class='value'>" + escapeHtml(appointment.getPreferredDate()) + "</td></tr>"
                + "<tr><td class='label'>Preferred Time:</td><td class='value'>" + escapeHtml(appointment.getPreferredTime()) + "</td></tr>"
                + "<tr><td class='label'>Reason / Symptoms:</td><td class='value'>" + escapeHtml(reasonDisplay) + "</td></tr>"
                + "<tr><td class='label'>Status:</td><td class='value'><span class='badge'>PENDING</span></td></tr>"
                + "</table>"
                + "<p>We will notify you by email once your appointment is confirmed.</p>"
                + "<p>For assistance, please contact Nirmala Neuro & General Medical Centre.</p>"
                + "<p>Regards,<br><strong>Nirmala Neuro & General Medical Centre</strong></p>"
                + "</div>"
                + "<div class='footer'>Nirmala Neuro & General Medical Centre | Phone: 6305471147 / 6302963312 | Email: nirmalaneurocare@gmail.com</div>"
                + "</div></body></html>";

        dispatchEmail(appointment.getEmail(), subject, htmlContent, emailType, apptRef, null, null);
    }

    public boolean sendAppointmentConfirmedEmail(Appointment appointment, String doctorName, String departmentName, byte[] pdfBytes) {
        String apptRef = appointment.getAppointmentId() != null ? appointment.getAppointmentId() : appointment.getId();
        String subject = "Appointment Confirmed - Nirmala Neuro & General Medical Centre";
        String emailType = "APPOINTMENT_CONFIRMED";

        String reasonDisplay = (appointment.getReason() != null && !appointment.getReason().isBlank()) 
                ? appointment.getReason() 
                : "General Consultation";

        String htmlContent = "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; }"
                + ".card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08); border: 1px solid #e2e8f0; }"
                + ".header { background: #0f4c81; color: #ffffff; padding: 24px; text-align: center; }"
                + ".header h2 { margin: 0; font-size: 20px; font-weight: 700; }"
                + ".body-content { padding: 28px; color: #1e293b; font-size: 15px; line-height: 1.6; }"
                + ".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; background: #f8fafc; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }"
                + ".details-table td { padding: 12px 16px; border-bottom: 1px solid #edf2f7; font-size: 14px; }"
                + ".details-table td.label { font-weight: 600; color: #64748b; width: 40%; }"
                + ".details-table td.value { font-weight: 600; color: #0f172a; }"
                + ".badge { display: inline-block; padding: 4px 12px; border-radius: 20px; color: #ffffff; background-color: #10b981; font-size: 12px; font-weight: 700; text-transform: uppercase; }"
                + ".footer { background: #f1f5f9; color: #64748b; text-align: center; padding: 16px; font-size: 13px; border-top: 1px solid #e2e8f0; }"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div class='header'><h2>Nirmala Neuro & General Medical Centre</h2></div>"
                + "<div class='body-content'>"
                + "<p>Dear " + escapeHtml(appointment.getPatientName()) + ",</p>"
                + "<p>Your appointment with Nirmala Neuro & General Medical Centre has been <strong>CONFIRMED</strong>.</p>"
                + "<table class='details-table'>"
                + "<tr><td class='label'>Appointment ID:</td><td class='value' style='font-family:monospace; font-size:15px; color:#0f4c81;'>" + apptRef + "</td></tr>"
                + "<tr><td class='label'>Patient Name:</td><td class='value'>" + escapeHtml(appointment.getPatientName()) + "</td></tr>"
                + "<tr><td class='label'>Department:</td><td class='value'>" + escapeHtml(departmentName) + "</td></tr>"
                + "<tr><td class='label'>Doctor:</td><td class='value'>" + escapeHtml(doctorName) + "</td></tr>"
                + "<tr><td class='label'>Appointment Date:</td><td class='value'>" + escapeHtml(appointment.getPreferredDate()) + "</td></tr>"
                + "<tr><td class='label'>Appointment Time:</td><td class='value'>" + escapeHtml(appointment.getPreferredTime()) + "</td></tr>"
                + "<tr><td class='label'>Reason / Symptoms:</td><td class='value'>" + escapeHtml(reasonDisplay) + "</td></tr>"
                + "<tr><td class='label'>Status:</td><td class='value'><span class='badge'>CONFIRMED</span></td></tr>"
                + "</table>"
                + "<p>Your appointment confirmation PDF is attached to this email.</p>"
                + "<p>Please bring the required medical documents/reports if applicable.</p>"
                + "<p>For assistance, please contact the hospital.</p>"
                + "<p>Regards,<br><strong>Nirmala Neuro & General Medical Centre</strong></p>"
                + "</div>"
                + "<div class='footer'>Nirmala Neuro & General Medical Centre | Phone: 6305471147 / 6302963312 | Email: nirmalaneurocare@gmail.com</div>"
                + "</div></body></html>";

        String attachmentName = "Appointment_" + apptRef + ".pdf";
        return dispatchEmail(appointment.getEmail(), subject, htmlContent, emailType, apptRef, pdfBytes, attachmentName);
    }

    public boolean sendAppointmentRescheduledEmail(Appointment appointment, String doctorName, String departmentName, String previousDate, String previousTime, byte[] pdfBytes) {
        String apptRef = appointment.getAppointmentId() != null ? appointment.getAppointmentId() : appointment.getId();
        String subject = "Appointment Rescheduled - Nirmala Neuro & General Medical Centre";
        String emailType = "APPOINTMENT_RESCHEDULED";

        String reasonDisplay = (appointment.getReason() != null && !appointment.getReason().isBlank()) 
                ? appointment.getReason() 
                : "General Consultation";

        String htmlContent = "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; }"
                + ".card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08); border: 1px solid #e2e8f0; }"
                + ".header { background: #0f4c81; color: #ffffff; padding: 24px; text-align: center; }"
                + ".header h2 { margin: 0; font-size: 20px; font-weight: 700; }"
                + ".body-content { padding: 28px; color: #1e293b; font-size: 15px; line-height: 1.6; }"
                + ".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; background: #f8fafc; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }"
                + ".details-table td { padding: 12px 16px; border-bottom: 1px solid #edf2f7; font-size: 14px; }"
                + ".details-table td.label { font-weight: 600; color: #64748b; width: 40%; }"
                + ".details-table td.value { font-weight: 600; color: #0f172a; }"
                + ".badge { display: inline-block; padding: 4px 12px; border-radius: 20px; color: #ffffff; background-color: #3b82f6; font-size: 12px; font-weight: 700; text-transform: uppercase; }"
                + ".footer { background: #f1f5f9; color: #64748b; text-align: center; padding: 16px; font-size: 13px; border-top: 1px solid #e2e8f0; }"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div class='header'><h2>Nirmala Neuro & General Medical Centre</h2></div>"
                + "<div class='body-content'>"
                + "<p>Dear " + escapeHtml(appointment.getPatientName()) + ",</p>"
                + "<p>Your appointment has been <strong>RESCHEDULED</strong> to a new time slot.</p>"
                + "<table class='details-table'>"
                + "<tr><td class='label'>Appointment ID:</td><td class='value' style='font-family:monospace; font-size:15px; color:#0f4c81;'>" + apptRef + "</td></tr>"
                + "<tr><td class='label'>Patient Name:</td><td class='value'>" + escapeHtml(appointment.getPatientName()) + "</td></tr>"
                + "<tr><td class='label'>Department:</td><td class='value'>" + escapeHtml(departmentName) + "</td></tr>"
                + "<tr><td class='label'>Doctor:</td><td class='value'>" + escapeHtml(doctorName) + "</td></tr>"
                + "<tr><td class='label'>Previous Date/Time:</td><td class='value' style='color:#94a3b8; text-decoration:line-through;'>" + escapeHtml(previousDate) + " @ " + escapeHtml(previousTime) + "</td></tr>"
                + "<tr><td class='label'>New Date/Time:</td><td class='value' style='color:#10b981; font-weight:bold;'>" + escapeHtml(appointment.getPreferredDate()) + " @ " + escapeHtml(appointment.getPreferredTime()) + "</td></tr>"
                + "<tr><td class='label'>Reason / Symptoms:</td><td class='value'>" + escapeHtml(reasonDisplay) + "</td></tr>"
                + "<tr><td class='label'>Status:</td><td class='value'><span class='badge'>RESCHEDULED</span></td></tr>"
                + "</table>"
                + "<p>Your updated appointment confirmation PDF is attached to this email.</p>"
                + "<p>For assistance, please contact the hospital.</p>"
                + "<p>Regards,<br><strong>Nirmala Neuro & General Medical Centre</strong></p>"
                + "</div>"
                + "<div class='footer'>Nirmala Neuro & General Medical Centre | Phone: 6305471147 / 6302963312 | Email: nirmalaneurocare@gmail.com</div>"
                + "</div></body></html>";

        String attachmentName = "Appointment_" + apptRef + ".pdf";
        return dispatchEmail(appointment.getEmail(), subject, htmlContent, emailType, apptRef, pdfBytes, attachmentName);
    }

    public boolean sendAppointmentCancelledEmail(Appointment appointment, String doctorName, String departmentName, String reason) {
        String apptRef = appointment.getAppointmentId() != null ? appointment.getAppointmentId() : appointment.getId();
        String subject = "Appointment Cancelled - Nirmala Neuro & General Medical Centre";
        String emailType = "APPOINTMENT_CANCELLED";

        String htmlContent = "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; }"
                + ".card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08); border: 1px solid #e2e8f0; }"
                + ".header { background: #0f4c81; color: #ffffff; padding: 24px; text-align: center; }"
                + ".header h2 { margin: 0; font-size: 20px; font-weight: 700; }"
                + ".body-content { padding: 28px; color: #1e293b; font-size: 15px; line-height: 1.6; }"
                + ".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; background: #f8fafc; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }"
                + ".details-table td { padding: 12px 16px; border-bottom: 1px solid #edf2f7; font-size: 14px; }"
                + ".details-table td.label { font-weight: 600; color: #64748b; width: 40%; }"
                + ".details-table td.value { font-weight: 600; color: #0f172a; }"
                + ".badge { display: inline-block; padding: 4px 12px; border-radius: 20px; color: #ffffff; background-color: #ef4444; font-size: 12px; font-weight: 700; text-transform: uppercase; }"
                + ".footer { background: #f1f5f9; color: #64748b; text-align: center; padding: 16px; font-size: 13px; border-top: 1px solid #e2e8f0; }"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div class='header'><h2>Nirmala Neuro & General Medical Centre</h2></div>"
                + "<div class='body-content'>"
                + "<p>Dear " + escapeHtml(appointment.getPatientName()) + ",</p>"
                + "<p>Your appointment with Nirmala Neuro & General Medical Centre has been <strong>CANCELLED</strong>.</p>"
                + "<table class='details-table'>"
                + "<tr><td class='label'>Appointment ID:</td><td class='value' style='font-family:monospace; font-size:15px; color:#0f4c81;'>" + apptRef + "</td></tr>"
                + "<tr><td class='label'>Patient Name:</td><td class='value'>" + escapeHtml(appointment.getPatientName()) + "</td></tr>"
                + "<tr><td class='label'>Department:</td><td class='value'>" + escapeHtml(departmentName) + "</td></tr>"
                + "<tr><td class='label'>Doctor:</td><td class='value'>" + escapeHtml(doctorName) + "</td></tr>"
                + "<tr><td class='label'>Original Date:</td><td class='value'>" + escapeHtml(appointment.getPreferredDate()) + "</td></tr>"
                + "<tr><td class='label'>Original Time:</td><td class='value'>" + escapeHtml(appointment.getPreferredTime()) + "</td></tr>"
                + (reason != null && !reason.isBlank() ? "<tr><td class='label'>Reason:</td><td class='value'>" + escapeHtml(reason) + "</td></tr>" : "")
                + "<tr><td class='label'>Status:</td><td class='value'><span class='badge'>CANCELLED</span></td></tr>"
                + "</table>"
                + "<p>If you need to reschedule or have questions, please contact the hospital.</p>"
                + "<p>Regards,<br><strong>Nirmala Neuro & General Medical Centre</strong></p>"
                + "</div>"
                + "<div class='footer'>Nirmala Neuro & General Medical Centre | Phone: 6305471147 / 6302963312 | Email: nirmalaneurocare@gmail.com</div>"
                + "</div></body></html>";

        return dispatchEmail(appointment.getEmail(), subject, htmlContent, emailType, apptRef, null, null);
    }

    private boolean dispatchEmail(String recipientEmail, String subject, String htmlContent, String emailType, String apptRef, byte[] attachmentBytes, String attachmentName) {
        EmailLog log = EmailLog.builder()
                .appointmentId(apptRef)
                .recipientEmail(recipientEmail)
                .emailType(emailType)
                .subject(subject)
                .status("QUEUED")
                .createdAt(LocalDateTime.now())
                .build();

        if (emailLogRepository != null) {
            log = emailLogRepository.save(log);
        }

        if (recipientEmail == null || recipientEmail.isBlank()) {
            logger.warn("No recipient email provided for appointment reference: {}", apptRef);
            updateLogStatus(log, "FAILED", "Recipient email address is missing");
            return false;
        }

        try {
            // 1. Try Brevo HTTPS REST API (Port 443 — Unblocked on Render)
            if (brevoApiKey != null && !brevoApiKey.isBlank()) {
                sendViaBrevo(recipientEmail, subject, htmlContent, attachmentBytes, attachmentName);
                logger.info("Successfully sent email [{}] via Brevo API to {}", emailType, recipientEmail);
                updateLogStatus(log, "SENT", "Sent via Brevo HTTP API");
                return true;
            }

            // 2. Try Resend HTTPS REST API (Port 443 — Unblocked on Render)
            if (resendApiKey != null && !resendApiKey.isBlank()) {
                sendViaResend(recipientEmail, subject, htmlContent, attachmentBytes, attachmentName);
                logger.info("Successfully sent email [{}] via Resend API to {}", emailType, recipientEmail);
                updateLogStatus(log, "SENT", "Sent via Resend HTTP API");
                return true;
            }

            // 3. Fallback to JavaMailSender (SMTP)
            if (mailSender == null) {
                logger.info("JavaMailSender not configured. Email logged locally. To: {}, Subject: {}", recipientEmail, subject);
                updateLogStatus(log, "SENT", "Logged to console (No mail provider configured)");
                return true;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromName != null && !fromName.isBlank()) {
                helper.setFrom(fromEmail, fromName);
            } else {
                helper.setFrom(fromEmail);
            }

            helper.setReplyTo(fromEmail, "Nirmala Hospital Support");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            
            String plainText = htmlContent.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
            helper.setText(plainText, htmlContent);

            message.addHeader("X-Priority", "1");
            message.addHeader("X-MSMail-Priority", "High");
            message.addHeader("Importance", "High");
            message.addHeader("X-Mailer", "NirmalaHospitalNotificationSystem/1.0");

            if (attachmentBytes != null && attachmentName != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));
            }

            mailSender.send(message);
            logger.info("Successfully sent email [{}] via SMTP to {}", emailType, recipientEmail);
            updateLogStatus(log, "SENT", "Sent via SMTP");
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email [{}] to {}: {}", emailType, recipientEmail, e.getMessage());
            updateLogStatus(log, "FAILED", e.getMessage());
            return false;
        }
    }

    private void sendViaBrevo(String recipientEmail, String subject, String htmlContent, byte[] attachmentBytes, String attachmentName) throws Exception {
        String url = "https://api.brevo.com/v3/smtp/email";
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("name", fromName, "email", fromEmail));
        payload.put("to", List.of(Map.of("email", recipientEmail)));
        payload.put("subject", subject);
        payload.put("htmlContent", htmlContent);

        if (attachmentBytes != null && attachmentName != null) {
            String base64Content = Base64.getEncoder().encodeToString(attachmentBytes);
            payload.put("attachment", List.of(Map.of("name", attachmentName, "content", base64Content)));
        }

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .header("api-key", brevoApiKey.trim())
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Brevo API HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private void sendViaResend(String recipientEmail, String subject, String htmlContent, byte[] attachmentBytes, String attachmentName) throws Exception {
        String url = "https://api.resend.com/emails";

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", fromName + " <" + fromEmail + ">");
        payload.put("to", List.of(recipientEmail));
        payload.put("subject", subject);
        payload.put("html", htmlContent);

        if (attachmentBytes != null && attachmentName != null) {
            String base64Content = Base64.getEncoder().encodeToString(attachmentBytes);
            payload.put("attachments", List.of(Map.of("filename", attachmentName, "content", base64Content)));
        }

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + resendApiKey.trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Resend API HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private void updateLogStatus(EmailLog log, String status, String errorMessage) {
        if (log != null && emailLogRepository != null) {
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setSentAt(LocalDateTime.now());
            emailLogRepository.save(log);
        }
    }

    public Map<String, Object> testSmtpConnection(String toEmail) {
        Map<String, Object> res = new HashMap<>();
        res.put("fromEmail", fromEmail);
        res.put("fromName", fromName);
        res.put("recipient", toEmail);

        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            res.put("provider", "Brevo HTTPS API (Port 443)");
            try {
                sendViaBrevo(toEmail, "Test Email - Nirmala Hospital Cloud Deployment",
                        "<div style='font-family:sans-serif; padding:20px; color:#1e293b;'>" +
                        "<h2>Test Email Successful!</h2>" +
                        "<p>Your email service on Render is connected to Brevo API over HTTPS (Port 443) and working 100% reliably!</p>" +
                        "<p>Regards,<br><strong>Nirmala Neuro & General Medical Centre</strong></p></div>",
                        null, null);
                res.put("status", "SUCCESS");
                res.put("message", "Email successfully dispatched via Brevo HTTP API to " + toEmail);
                return res;
            } catch (Exception e) {
                logger.error("Brevo API test failed", e);
                res.put("status", "ERROR");
                res.put("errorMessage", e.getMessage());
                return res;
            }
        }

        if (resendApiKey != null && !resendApiKey.isBlank()) {
            res.put("provider", "Resend HTTPS API (Port 443)");
            try {
                sendViaResend(toEmail, "Test Email - Nirmala Hospital Cloud Deployment",
                        "<h2>Test Email Successful!</h2><p>Sent via Resend API over HTTPS.</p>", null, null);
                res.put("status", "SUCCESS");
                res.put("message", "Email successfully dispatched via Resend HTTP API to " + toEmail);
                return res;
            } catch (Exception e) {
                logger.error("Resend API test failed", e);
                res.put("status", "ERROR");
                res.put("errorMessage", e.getMessage());
                return res;
            }
        }

        res.put("provider", "SMTP (Port 465/587)");
        if (mailSender == null) {
            res.put("status", "ERROR");
            res.put("message", "No email provider or JavaMailSender configured");
            return res;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            if (fromName != null && !fromName.isBlank()) {
                helper.setFrom(fromEmail, fromName);
            } else {
                helper.setFrom(fromEmail);
            }
            helper.setTo(toEmail);
            helper.setSubject("Test Email - Nirmala Hospital Cloud Deployment");
            helper.setText("Hello! If you are reading this email, your SMTP configuration on Render is working 100% successfully!", false);

            mailSender.send(message);
            res.put("status", "SUCCESS");
            res.put("message", "Email successfully dispatched to " + toEmail);
            return res;
        } catch (Exception e) {
            logger.error("Test email connection failed", e);
            res.put("status", "ERROR");
            res.put("errorMessage", e.getMessage());
            res.put("errorClass", e.getClass().getName());
            if (e.getCause() != null) {
                res.put("causeMessage", e.getCause().getMessage());
            }
            return res;
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
