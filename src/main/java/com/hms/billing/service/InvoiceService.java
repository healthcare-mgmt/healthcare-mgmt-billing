package com.hms.billing.service;

import com.hms.billing.dto.InvoiceDto;
import com.hms.billing.dto.InvoiceRequest;

public interface InvoiceService {

    InvoiceDto createInvoice(InvoiceRequest request);

    InvoiceDto finalizeInvoice(Long invoiceId);

    InvoiceDto getInvoice(Long invoiceId);

    byte[] generateInvoicePdf(Long invoiceId);
}
