package com.hms.billing.dto;

import com.hms.billing.entity.PaymentMethod;
import com.hms.billing.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private Long id;
    private Long invoiceId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String externalRef;  // optional (card gateway ref or tracking ID)
}
