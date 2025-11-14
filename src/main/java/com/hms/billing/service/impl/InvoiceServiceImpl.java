package com.hms.billing.service.impl;

import com.hms.billing.dto.InvoiceDto;
import com.hms.billing.dto.InvoiceItemRequest;
import com.hms.billing.dto.InvoiceRequest;
import com.hms.billing.entity.Invoice;
import com.hms.billing.entity.InvoiceItem;
import com.hms.billing.entity.InvoiceStatus;
import com.hms.billing.exception.NotFoundException;
import com.hms.billing.repository.InvoiceRepository;
import com.hms.billing.service.InvoiceService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceRequest request) {

        Invoice invoice = new Invoice();
        invoice.setPatientReference(request.getPatientReference());

        double subtotal = 0;

        for (InvoiceItemRequest reqItem : request.getItems()) {
            InvoiceItem item = new InvoiceItem(
                    reqItem.getCode(),
                    reqItem.getDescription(),
                    reqItem.getQuantity(),
                    reqItem.getUnitPrice(),
                    reqItem.getQuantity() * reqItem.getUnitPrice()
            );
            subtotal += item.getAmount();
            invoice.getItems().add(item);
        }

        invoice.setSubtotal(subtotal);

        double tax = request.getTaxRatePercent() == null ? 0 : subtotal * (request.getTaxRatePercent() / 100);
        invoice.setTax(tax);

        invoice.setTotal(subtotal + tax);
        invoice.setBalance(subtotal + tax);

        Invoice saved = invoiceRepository.save(invoice);

        return toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"invoices", "invoicePdfs"}, key = "#invoiceId")
    public InvoiceDto finalizeInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        invoice.setStatus(InvoiceStatus.FINALIZED);
        Invoice saved = invoiceRepository.save(invoice);

        return toDto(saved);
    }

    @Override
    @Cacheable(value = "invoices", key = "#invoiceId")
    public InvoiceDto getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        return toDto(invoice);
    }

    private InvoiceDto toDto(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.getPatientReference(),
                invoice.getStatus(),
                invoice.getItems().stream()
                        .map(i -> new InvoiceItemRequest(i.getCode(), i.getDescription(), i.getQuantity(), i.getUnitPrice()))
                        .toList(),
                invoice.getSubtotal(),
                invoice.getTax(),
                invoice.getTotal(),
                invoice.getBalance(),
                invoice.getCreatedAt()
        );
    }

    @Override
    @Cacheable(value = "invoicePdfs", key = "#invoiceId")
    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 36, 36, 90, 36);
            PdfWriter.getInstance(document, baos);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph header = new Paragraph("HOSPITAL MANAGEMENT SYSTEM (HMS)", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph invoiceLabel = new Paragraph("INVOICE", new Font(Font.HELVETICA, 14, Font.BOLD));
            invoiceLabel.setAlignment(Element.ALIGN_CENTER);
            invoiceLabel.setSpacingAfter(15f);
            document.add(invoiceLabel);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(makeCell("Invoice ID:", PdfPCell.ALIGN_LEFT, true));
            infoTable.addCell(makeCell(String.valueOf(invoice.getId()), PdfPCell.ALIGN_LEFT, false));
            infoTable.addCell(makeCell("Patient Reference:", PdfPCell.ALIGN_LEFT, true));
            infoTable.addCell(makeCell(invoice.getPatientReference(), PdfPCell.ALIGN_LEFT, false));
            infoTable.addCell(makeCell("Status:", PdfPCell.ALIGN_LEFT, true));
            infoTable.addCell(makeCell(invoice.getStatus().name(), PdfPCell.ALIGN_LEFT, false));
            infoTable.addCell(makeCell("Created At:", PdfPCell.ALIGN_LEFT, true));
            infoTable.addCell(makeCell(invoice.getCreatedAt().toString(), PdfPCell.ALIGN_LEFT, false));
            infoTable.setSpacingAfter(20f);
            document.add(infoTable);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            Stream.of("Code", "Description", "Qty", "Unit Price", "Amount")
                    .forEach(columnTitle -> {
                        PdfPCell headerCell = new PdfPCell(new Phrase(columnTitle, new Font(Font.BOLD)));
                        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        headerCell.setBackgroundColor(Color.LIGHT_GRAY);
                        headerCell.setPadding(6);
                        table.addCell(headerCell);
                    });

            invoice.getItems().forEach(item -> {
                table.addCell(makeBodyCell(item.getCode()));
                table.addCell(makeBodyCell(item.getDescription()));
                table.addCell(makeBodyCell(item.getQuantity().toString()));
                table.addCell(makeBodyCell("$ " + item.getUnitPrice()));
                table.addCell(makeBodyCell("$ " + item.getAmount()));
            });

            document.add(table);

            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(40);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setSpacingBefore(20f);

            totals.addCell(makeTotalCell("Subtotal:"));
            totals.addCell(makeTotalValue("$ " + invoice.getSubtotal()));
            totals.addCell(makeTotalCell("Tax:"));
            totals.addCell(makeTotalValue("$ " + invoice.getTax()));
            totals.addCell(makeTotalCell("Total:"));
            totals.addCell(makeTotalValue("$ " + invoice.getTotal()));
            totals.addCell(makeTotalCell("Balance Due:"));
            totals.addCell(makeTotalValue("$ " + invoice.getBalance()));

            document.add(totals);

            Paragraph footer = new Paragraph(
                    "This is a system-generated invoice and does not require a signature.",
                    new Font(Font.HELVETICA, 8, Font.ITALIC)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20f);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }
    }

    private PdfPCell makeCell(String text, int alignment, boolean bold) {
        Font font = bold ? new Font(Font.HELVETICA, 10, Font.BOLD) : new Font(Font.HELVETICA, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private PdfPCell makeBodyCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 10)));
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell makeTotalCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 10, Font.BOLD)));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private PdfPCell makeTotalValue(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
}
