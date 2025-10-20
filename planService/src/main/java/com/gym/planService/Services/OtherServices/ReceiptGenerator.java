package com.gym.planService.Services.OtherServices;

import com.gym.planService.Models.PlanPayment;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;

import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public class ReceiptGenerator {

    public static byte[] generatePlanPaymentReceipt(PlanPayment payment) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);


        // Header
        document.add(new Paragraph("FITNESS STUDIO")
                .setBold()
                .setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE));

        document.add(new Paragraph("Payment Receipt")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        // Divider line (this replaces your SolidBorder add)
        LineSeparator line = new LineSeparator((ILineDrawer) new SolidBorder(1));
        document.add(line);

        // ======================
        // Payment Information Section
        // ======================
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        Table table = new Table(2);
        table.setWidth(100);

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

        // ======================
        // Footer
        // ======================
        document.add(new Paragraph("\nThank you for choosing Fitness Studio!")
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(11)
                .setMarginTop(20));

        document.close();
        return outputStream.toByteArray();
    }

    // Helper method for clean rows
    private static void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-")));
    }
}
