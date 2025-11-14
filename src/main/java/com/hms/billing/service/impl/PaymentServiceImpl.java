package com.hms.billing.service.impl;

import com.hms.billing.dto.PaymentDto;
import com.hms.billing.dto.PaymentRequest;
import com.hms.billing.entity.*;
import com.hms.billing.exception.NotFoundException;
import com.hms.billing.messaging.InvoicePaidPublisher;
import com.hms.billing.repository.InvoiceRepository;
import com.hms.billing.repository.PaymentRepository;
import com.hms.billing.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoicePaidPublisher publisher;

    @Override
    @Transactional
    @CacheEvict(value = {"invoices", "invoicePdfs"}, key = "#invoiceId")
    public PaymentDto recordCashPayment(Long invoiceId, PaymentRequest request) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        if (invoice.getBalance() <= 0) {
            throw new IllegalStateException("Invoice already paid or has no outstanding balance.");
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(request.getAmount());
        payment.setMethod(PaymentMethod.CASH);
        payment.setStatus(PaymentStatus.SUCCEEDED);

        paymentRepository.save(payment);

        // Update invoice balance
        double newBalance = invoice.getBalance() - request.getAmount();
        invoice.setBalance(newBalance);

        if (newBalance <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);

            //Trigger Rabbit Message
            publisher.publish(invoice.getId(), payment.getAmount());
        }

        invoiceRepository.save(invoice);

        return new PaymentDto(
                payment.getId(),
                invoice.getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getExternalRef()
        );
    }

    @Override
    @Transactional
    public PaymentDto startCardPayment(Long invoiceId, PaymentRequest request) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(request.getAmount());
        payment.setMethod(PaymentMethod.CARD);

        // Simulating a successful external gateway charge
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setExternalRef("MOCK_GATEWAY_REF_123");

        paymentRepository.save(payment);

        // Reduce invoice balance
        double newBalance = invoice.getBalance() - request.getAmount();
        invoice.setBalance(newBalance);

        if (newBalance <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        }

        invoiceRepository.save(invoice);

        return new PaymentDto(
                payment.getId(),
                invoice.getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getExternalRef()
        );
    }
}
