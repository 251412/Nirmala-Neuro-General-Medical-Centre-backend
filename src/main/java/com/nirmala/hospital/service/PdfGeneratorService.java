package com.nirmala.hospital.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.nirmala.hospital.model.Appointment;
import com.nirmala.hospital.model.SiteSettings;
import com.nirmala.hospital.repository.SiteSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGeneratorService.class);

    private static final String STORAGE_DIR = "storage/pdfs";

    @Autowired(required = false)
    private SiteSettingsRepository siteSettingsRepository;

    public byte[] generateAppointmentPdf(Appointment appointment, String doctorName, String departmentName) throws Exception {
        // Fetch dynamic hospital settings for PDF contact block
        String hospitalAddress = "Back of INOX Multiplex, Opposite RTC Complex, Fort Area, Vizianagaram, Andhra Pradesh - 535002";
        String hospitalPhone = "6305471147 / 6302963312";
        String hospitalEmail = "nirmalaneurocare@gmail.com";
        String emergencyPhone = "6305471147 / 6302963312";

        if (siteSettingsRepository != null) {
            List<SiteSettings> settingsList = siteSettingsRepository.findAll();
            if (!settingsList.isEmpty()) {
                SiteSettings s = settingsList.get(0);
                if (s.getAddress() != null && !s.getAddress().isBlank()) hospitalAddress = s.getAddress();
                if (s.getPhone() != null && !s.getPhone().isBlank()) hospitalPhone = s.getPhone();
                if (s.getEmail() != null && !s.getEmail().isBlank()) hospitalEmail = s.getEmail();
                if (s.getEmergencyNumber() != null && !s.getEmergencyNumber().isBlank()) emergencyPhone = s.getEmergencyNumber();
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // Palette
        Color primaryNavy = new Color(15, 76, 129); // #0f4c81
        Color darkText = new Color(30, 41, 59);    // #1e293b
        Color lightBg = new Color(248, 250, 252);  // #f8fafc
        Color borderGray = new Color(226, 232, 240); // #e2e8f0
        Color statusGreen = new Color(16, 185, 129); // #10b981

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, primaryNavy);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Font sectionHeadingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, primaryNavy);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, darkText);
        Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkText);
        Font smallMutedFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);

        // Header Table
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(lightBg);
        headerCell.setBorderColor(borderGray);
        headerCell.setBorderWidth(1);
        headerCell.setPadding(16);

        Paragraph pHospital = new Paragraph("NIRMALA NEURO & GENERAL MEDICAL CENTRE", titleFont);
        pHospital.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(pHospital);

        Paragraph pDocType = new Paragraph("OFFICIAL APPOINTMENT CONFIRMATION", subTitleFont);
        pDocType.setAlignment(Element.ALIGN_CENTER);
        pDocType.setSpacingBefore(4);
        headerCell.addElement(pDocType);

        headerTable.addCell(headerCell);
        document.add(headerTable);

        document.add(new Paragraph(" ")); // Spacer

        // Status Banner Card
        PdfPTable statusTable = new PdfPTable(2);
        statusTable.setWidthPercentage(100);
        statusTable.setWidths(new float[]{70, 30});

        String apptRef = appointment.getAppointmentId() != null ? appointment.getAppointmentId() : appointment.getId();

        PdfPCell refCell = new PdfPCell(new Phrase("Appointment Reference ID: " + apptRef, boldFont));
        refCell.setBorder(Rectangle.NO_BORDER);
        refCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        statusTable.addCell(refCell);

        PdfPCell badgeCell = new PdfPCell(new Phrase("CONFIRMED", statusFont));
        badgeCell.setBackgroundColor(statusGreen);
        badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        badgeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        badgeCell.setPadding(6);
        badgeCell.setBorder(Rectangle.NO_BORDER);
        statusTable.addCell(badgeCell);

        document.add(statusTable);
        document.add(new Paragraph(" "));

        // Appointment & Patient Details Grid Table
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{50, 50});

        addTableRow(detailsTable, "Patient Name:", appointment.getPatientName() != null ? appointment.getPatientName() : "N/A", boldFont, regularFont);
        addTableRow(detailsTable, "Patient Email:", appointment.getEmail() != null ? appointment.getEmail() : "N/A", boldFont, regularFont);
        addTableRow(detailsTable, "Phone Number:", appointment.getPhone() != null ? appointment.getPhone() : "N/A", boldFont, regularFont);
        addTableRow(detailsTable, "Age / Gender:", appointment.getAge() + " yrs / " + (appointment.getGender() != null ? appointment.getGender() : "N/A"), boldFont, regularFont);
        addTableRow(detailsTable, "Department:", departmentName != null ? departmentName : "N/A", boldFont, regularFont);
        addTableRow(detailsTable, "Consulting Doctor:", doctorName != null ? doctorName : "N/A", boldFont, regularFont);
        addTableRow(detailsTable, "Appointment Date:", appointment.getPreferredDate() != null ? appointment.getPreferredDate() : "N/A", boldFont, regularFont);
        addTableRow(detailsTable, "Appointment Time:", appointment.getPreferredTime() != null ? appointment.getPreferredTime() : "N/A", boldFont, regularFont);

        document.add(detailsTable);
        document.add(new Paragraph(" "));

        // Reason for Visit Section
        Paragraph pReasonHead = new Paragraph("Reason for Visit / Symptoms", sectionHeadingFont);
        pReasonHead.setSpacingAfter(4);
        document.add(pReasonHead);

        PdfPTable reasonTable = new PdfPTable(1);
        reasonTable.setWidthPercentage(100);
        PdfPCell reasonCell = new PdfPCell();
        reasonCell.setBackgroundColor(lightBg);
        reasonCell.setBorderColor(borderGray);
        reasonCell.setPadding(10);
        
        String reasonText = appointment.getReason();
        if (reasonText == null || reasonText.isBlank()) {
            reasonText = "General Consultation / Routine Checkup";
        }
        reasonCell.addElement(new Paragraph(reasonText, regularFont));
        reasonTable.addCell(reasonCell);

        document.add(reasonTable);
        document.add(new Paragraph(" "));

        // Hospital Contact & Instructions Box
        Paragraph pContactHead = new Paragraph("Hospital Contact Information & Guidelines", sectionHeadingFont);
        pContactHead.setSpacingAfter(4);
        document.add(pContactHead);

        PdfPTable contactTable = new PdfPTable(1);
        contactTable.setWidthPercentage(100);
        PdfPCell contactCell = new PdfPCell();
        contactCell.setBorderColor(borderGray);
        contactCell.setPadding(10);

        contactCell.addElement(new Paragraph("Address: " + hospitalAddress, regularFont));
        contactCell.addElement(new Paragraph("Phone: " + hospitalPhone + " | Email: " + hospitalEmail, regularFont));
        contactCell.addElement(new Paragraph("Emergency Contact Helpline: " + emergencyPhone, regularFont));
        contactCell.addElement(new Paragraph("\nImportant Patient Instructions:", boldFont));
        contactCell.addElement(new Paragraph("• Please arrive at the hospital 15 minutes before your scheduled appointment time.", regularFont));
        contactCell.addElement(new Paragraph("• Bring previous medical reports, prescriptions, and a government ID proof.", regularFont));
        contactCell.addElement(new Paragraph("• Show this official confirmation document at the reception desk upon arrival.", regularFont));

        contactTable.addCell(contactCell);
        document.add(contactTable);

        document.add(new Paragraph(" "));

        // Footer Stamp
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String genTime = LocalDateTime.now().format(dtf);
        Paragraph footerPara = new Paragraph("Generated on: " + genTime + " IST | Nirmala Neuro & General Medical Centre System", smallMutedFont);
        footerPara.setAlignment(Element.ALIGN_CENTER);
        document.add(footerPara);

        document.close();
        return baos.toByteArray();
    }

    public String savePdfToDisk(String appointmentId, byte[] pdfBytes) {
        try {
            File dir = new File(STORAGE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "Appointment_" + appointmentId + ".pdf";
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBytes);
            }
            logger.info("Saved appointment confirmation PDF to disk: {}", file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            logger.error("Failed to save PDF to disk for appointment {}: {}", appointmentId, e.getMessage());
            return null;
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setPadding(6);
        lCell.setBackgroundColor(new Color(241, 245, 249));
        lCell.setBorderColor(new Color(226, 232, 240));

        PdfPCell vCell = new PdfPCell(new Phrase(value, valueFont));
        vCell.setPadding(6);
        vCell.setBorderColor(new Color(226, 232, 240));

        table.addCell(lCell);
        table.addCell(vCell);
    }
}
