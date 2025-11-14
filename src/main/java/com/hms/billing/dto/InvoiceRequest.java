package com.hms.billing.dto;

import lombok.Data;
import java.util.List;

@Data
public class InvoiceRequest {
    private String patientReference;        // FHIR reference
    private List<InvoiceItemRequest> items;
    private Double taxRatePercent;
}
