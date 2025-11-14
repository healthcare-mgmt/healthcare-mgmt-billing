package com.hms.billing.controller;

import com.hms.billing.dto.InvoiceDto;
import com.hms.billing.dto.InvoiceRequest;
import com.hms.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /** Only users with BILLING or ADMIN role can create invoices */
    @PreAuthorize("hasAnyRole('BILLING', 'ADMIN')")
    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.createInvoice(request));
    }

    /** Only admins can finalize invoice */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{invoiceId}/finalize")
    public ResponseEntity<InvoiceDto> finalizeInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(invoiceService.finalizeInvoice(invoiceId));
    }

    /** Anyone authenticated in Billing team can view invoice */
    @PreAuthorize("hasAnyRole('BILLING', 'ADMIN')")
    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(invoiceService.getInvoice(invoiceId));
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long invoiceId) {

        byte[] pdfBytes = invoiceService.generateInvoicePdf(invoiceId);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=invoice_" + invoiceId + ".pdf")
                .body(pdfBytes);
    }
}
