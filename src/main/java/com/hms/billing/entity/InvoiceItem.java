package com.hms.billing.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class InvoiceItem {
    private String code;
    private String description;
    private Integer quantity;
    private Double unitPrice;
    private Double amount;
}
