package com.hms.billing.controller;

import com.hms.billing.dto.PaymentDto;
import com.hms.billing.dto.PaymentRequest;
import com.hms.billing.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices/{invoiceId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /** CASH payment — allowed for BILLING users */
    @PreAuthorize("hasAnyRole('BILLING', 'ADMIN')")
    @PostMapping("/cash")
    public ResponseEntity<PaymentDto> recordCashPayment(
            @PathVariable Long invoiceId,
            @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(paymentService.recordCashPayment(invoiceId, request));
    }

    /** CARD payment — can be limited to ADMIN or PAYMENT role */
    @PreAuthorize("hasAnyRole('ADMIN', 'PAYMENT')")
    @PostMapping("/card")
    public ResponseEntity<PaymentDto> startCardPayment(
            @PathVariable Long invoiceId,
            @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(paymentService.startCardPayment(invoiceId, request));
    }
}
