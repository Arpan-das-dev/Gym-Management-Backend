package com.gym.planService.Services.OtherServices;

import com.gym.planService.Models.PlanPayment;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ReceiptGenerator {

    @Async("pdfExecutor")
    public CompletableFuture<byte[]> generatePlanPaymentReceipt(PlanPayment payment) {
        log.info("Starting PDF generation for payment ID: {}", payment.getPaymentId());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);


            // Header
            document.add(new Paragraph("FITh STUDIO")
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE));

            document.add(new Paragraph("Payment Receipt")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            // ✅ Proper line separator
            LineSeparator line = new LineSeparator(new SolidLine());
            line.setMarginTop(5);
            line.setMarginBottom(10);
            document.add(line);

            // Payment Information
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            Table table = new Table(2).useAllAvailableWidth();

            addRow(table, "Receipt ID:", payment.getPaymentId());
            addRow(table, "Order ID:", payment.getOrderId());
            addRow(table, "User Name:", payment.getUserName());
            addRow(table, "User ID:", payment.getUserId());
            addRow(table, "Plan Name:", payment.getPlanName());
            addRow(table, "Amount Paid:", "₹" + payment.getPaidPrice());
            addRow(table, "Currency:", payment.getCurrency());
            addRow(table, "Payment Status:", payment.getPaymentStatus());
            addRow(table, "Date:", payment.getPaymentDate().format(formatter));

            table.setMarginTop(15);
            document.add(table);

            // Footer
            document.add(new Paragraph("\nThank you for choosing Fit Studio!")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setMarginTop(20));

            document.close();
            log.info("PDF generation complete for payment ID: {}", payment.getPaymentId());
            return CompletableFuture.completedFuture(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("PDF generation failed for payment ID {}: {}", payment.getPaymentId(), e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    // Helper method
    private static void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-")));
    }

    @Async("pdfExecutor")
    public CompletableFuture <byte[]> generateMonthlyReviewReceipt(String month, Integer year, Double totalRevenue) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            // ====================== HEADER ======================
            Paragraph header = new Paragraph("FITNESS STUDIO GYM")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(ColorConstants.ORANGE);
            document.add(header);

            Paragraph subHeader = new Paragraph("Monthly Revenue Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(15);
            document.add(subHeader);

            LineSeparator line = new LineSeparator(new SolidLine());
            line.setMarginBottom(20);
            document.add(line);

            // ====================== DETAILS TABLE ======================
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20);

            addRow(table, "Month", month);
            addRow(table, "Year", year.toString());
            addRow(table, "Total Revenue (₹)", String.format("%,.2f", totalRevenue));
            addRow(table, "Generated On", LocalDate.now().format(formatter));

            document.add(table);

            // ====================== SUMMARY SECTION ======================
            Paragraph summary = new Paragraph(String.format(
                    "This report summarizes the total successful transactions and total revenue generated for %s %d.",
                    month, year))
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(25);
            document.add(summary);

            // ====================== SIGNATURE SECTION ======================
            LineSeparator signatureLine = new LineSeparator(new SolidLine());
            signatureLine.setMarginTop(25);
            document.add(signatureLine);

            Paragraph sign = new Paragraph("Authorized by\nFinance Department - FitStudio")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setItalic()
                    .setMarginTop(10);
            document.add(sign);

            // ====================== FOOTER ======================
            Paragraph footer = new Paragraph("© " + year + " FitStudio. All Rights Reserved.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
            return CompletableFuture.completedFuture(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("PDF generation failed for payment for  {}: {}", month, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("pdfExecutor")
    public CompletableFuture <byte[]> generateYearlyReceipt(Map<String, Double> revenueMapper) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            // ================= HEADER =================
            document.add(new Paragraph("FITNESS STUDIO")
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE));

            document.add(new Paragraph("YEARLY REVENUE SUMMARY")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            LineSeparator line = new LineSeparator(new SolidLine());
            line.setMarginBottom(15);
            document.add(line);

            // ================= TABLE SETUP =================
            float[] columnWidths = {200F, 150F, 150F};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));

            // Table Header
            addTableHeader(table, "Month");
            addTableHeader(table, "Revenue (₹)");
            addTableHeader(table, "Change (%)");

            // ================= TABLE DATA =================
            revenueMapper.forEach((monthAndIncome, percentageChange) -> {
                // Example key format: "JANUARY::10000.0"
                String[] parts = monthAndIncome.split("::");
                String month = parts[0];
                String income = parts[1];

                addTableCell(table, month, TextAlignment.LEFT);
                addTableCell(table, income, TextAlignment.CENTER);

                // Color based on change value
                Color color = percentageChange >= 0 ? ColorConstants.GREEN : ColorConstants.RED;
                String sign = percentageChange >= 0 ? "+" : "-";
                String formatted = String.format("%s%.2f%%", sign, Math.abs(percentageChange));

                addTableCell(table, formatted, color);
            });

            document.add(table);

            // ================= FOOTER =================
            document.add(new Paragraph("\nReport generated on: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("© 2025 FitStudio Gym | Internal Financial Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setMarginTop(10));

            document.close();
            return CompletableFuture.completedFuture(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("PDF generation failed for payment for  : {}" , e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    // Helper for header cell
    private void addTableHeader(Table table, String headerTitle) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(headerTitle))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5));
    }

    // Helper for normal cell
    private void addTableCell(Table table, String content, TextAlignment alignment) {
        table.addCell(new Cell()
                .add(new Paragraph(content))
                .setTextAlignment(alignment)
                .setPadding(5));
    }

    // Overloaded with color support
    private void addTableCell(Table table, String content, Color fontColor) {
        table.addCell(new Cell()
                .add(new Paragraph(content))
                .setFontColor(fontColor)
                .setTextAlignment(TextAlignment.CENTER) // fixed alignment
                .setPadding(5));
    }
}
