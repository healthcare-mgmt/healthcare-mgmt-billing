package com.hms.billing.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoicePaidPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(Long invoiceId, Double amount) {
        String message = invoiceId + "," + amount;
        rabbitTemplate.convertAndSend("invoice-events", "invoice.paid", message);
    }
}
