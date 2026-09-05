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

import java.time.LocalDateTime;

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

        if (mailSender == null) {
            logger.info("JavaMailSender not configured. Email logged locally. To: {}, Subject: {}", recipientEmail, subject);
            updateLogStatus(log, "SENT", "Logged to console (JavaMailSender disabled)");
            return true;
        }

        try {
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
            
            // Add plain text + HTML multipart to pass spam filters (SPF/DKIM alignment)
            String plainText = htmlContent.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
            helper.setText(plainText, htmlContent);

            // Anti-Spam Headers
            message.addHeader("X-Priority", "1");
            message.addHeader("X-MSMail-Priority", "High");
            message.addHeader("Importance", "High");
            message.addHeader("X-Mailer", "NirmalaHospitalNotificationSystem/1.0");
            message.addHeader("Precedence", "bulk");

            if (attachmentBytes != null && attachmentName != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));
            }

            mailSender.send(message);
            logger.info("Successfully sent email [{}] to {}", emailType, recipientEmail);
            updateLogStatus(log, "SENT", null);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email [{}] to {}: {}", emailType, recipientEmail, e.getMessage());
            updateLogStatus(log, "FAILED", e.getMessage());
            return false;
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

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
