package com.hms.billing.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** this links the payment to the invoice */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;  // CASH / CARD

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String externalRef; // for card gateway tracking (optional now)

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
