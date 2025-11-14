package com.hms.billing.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange invoiceExchange() {
        return new TopicExchange("invoice-events");
    }

    @Bean
    public Queue invoicePaidQueue() {
        return new Queue("invoice-paid-queue");
    }

    @Bean
    public Binding binding(Queue invoicePaidQueue, TopicExchange invoiceExchange) {
        return BindingBuilder.bind(invoicePaidQueue)
                .to(invoiceExchange)
                .with("invoice.paid");
    }
}
