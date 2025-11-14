package com.hms.billing.dto;

import com.hms.billing.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDto {

    private Long id;
    private String patientReference;
    private InvoiceStatus status;
    private List<InvoiceItemRequest> items;
    private Double subtotal;
    private Double tax;
    private Double total;
    private Double balance;
    private OffsetDateTime createdAt;
}
