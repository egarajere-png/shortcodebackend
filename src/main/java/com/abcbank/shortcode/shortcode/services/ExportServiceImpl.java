package com.abcbank.shortcode.shortcode.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ShortCodeRepo shortCodeRepo;

    @Override
    public ByteArrayInputStream exportRegistryToExcel(String status) {

        List<ShortCode> registry = shortCodeRepo.findAll();

        if (status != null && !status.isBlank()) {

        registry = registry.stream()
            .filter(sc -> {

                String currentStatus;

                if (sc.isDeleted()) {
                    currentStatus = "deleted";
                } else if (sc.isDeleteInitiated()) {
                    currentStatus = "pending-deletion";
                } else if (!sc.isApproved()) {
                    currentStatus = "pending-approval";
                } else {
                    currentStatus = "active";
                }

                return currentStatus.equalsIgnoreCase(status);

            })
            .toList();
}

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Shortcode Registry");

            // Header row
            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Account Number");
            header.createCell(1).setCellValue("Account Name");
            header.createCell(2).setCellValue("Shortcode");
            header.createCell(3).setCellValue("Preferred Shortcode");
            header.createCell(4).setCellValue("Phone Number");
            header.createCell(5).setCellValue("Email");
            header.createCell(6).setCellValue("Customer ID");
            header.createCell(7).setCellValue("Status");
            header.createCell(8).setCellValue("Initiator");
            header.createCell(9).setCellValue("Approver");
            header.createCell(10).setCellValue("Date Initiated");
            header.createCell(11).setCellValue("Date Approved");
            header.createCell(12).setCellValue("Deletion Reason");

            int rowIdx = 1;

            for (ShortCode sc : registry) {

                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(
                        sc.getAccountNumber() == null ? "" : sc.getAccountNumber());

                row.createCell(1).setCellValue(
                        sc.getAccountName() == null ? "" : sc.getAccountName());

                row.createCell(2).setCellValue(sc.getShortCode());

                // Preferred Shortcode (nullable)
                row.createCell(3).setCellValue(
                        sc.getPreferredShortCode() == null
                                ? ""
                                : sc.getPreferredShortCode().toString());

                row.createCell(4).setCellValue(
                        sc.getPhoneNumber() == null ? "" : sc.getPhoneNumber());

                row.createCell(5).setCellValue(
                        sc.getEmailAddress() == null ? "" : sc.getEmailAddress());

                row.createCell(6).setCellValue(
                        sc.getCustId() == null ? "" : sc.getCustId());

                // Status
                String rowStatus;

                if (sc.isDeleted()) {
                        rowStatus = "Deleted";
                } else if (sc.isDeleteInitiated()) {
                        rowStatus = "Pending Deletion";
                } else if (!sc.isApproved()) {
                        rowStatus = "Pending Approval";
                } else {
                        rowStatus = "Active";
                }

                row.createCell(7).setCellValue(rowStatus);

                row.createCell(7).setCellValue(status);

                row.createCell(8).setCellValue(
                        sc.getInitiator() == null ? "" : sc.getInitiator());

                row.createCell(9).setCellValue(
                        sc.getApprover() == null ? "" : sc.getApprover());

                row.createCell(10).setCellValue(
                        sc.getDateInitiated() == null
                                ? ""
                                : sc.getDateInitiated().toString());

                row.createCell(11).setCellValue(
                        sc.getDateApproved() == null
                                ? ""
                                : sc.getDateApproved().toString());

                row.createCell(12).setCellValue(
                        sc.getDeleteRemark() == null
                                ? ""
                                : sc.getDeleteRemark());
            }

            // Auto-size columns
            for (int i = 0; i <= 12; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export registry.", e);
        }
    }
}