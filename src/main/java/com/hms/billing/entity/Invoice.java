package com.hms.billing.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FHIR reference - we store reference not patient details
    private String patientReference;   // ex: "Patient/12345"

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    // Invoice line items
    @ElementCollection
    @CollectionTable(name = "invoice_line_item", joinColumns = @JoinColumn(name = "invoice_id"))
    private List<InvoiceItem> items = new ArrayList<>();

    private Double subtotal = 0.0;
    private Double tax = 0.0;
    private Double total = 0.0;
    private Double balance = 0.0;

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
