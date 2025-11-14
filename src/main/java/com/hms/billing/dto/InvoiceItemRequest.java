package com.hms.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemRequest {
    private String code;
    private String description;
    private Integer quantity;
    private Double unitPrice;
}
