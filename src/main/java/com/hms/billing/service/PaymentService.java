package com.hms.billing.service;

import com.hms.billing.dto.PaymentDto;
import com.hms.billing.dto.PaymentRequest;

public interface PaymentService {

    PaymentDto recordCashPayment(Long invoiceId, PaymentRequest request);

    PaymentDto startCardPayment(Long invoiceId, PaymentRequest request);
}
