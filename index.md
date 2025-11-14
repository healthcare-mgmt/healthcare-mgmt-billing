---
title: Billing Service – Architecture & ER Diagrams
---

# Billing Service – Architecture & ER Diagrams

This page documents the high-level architecture and the billing domain model.

<!-- Load Mermaid for GitHub Pages -->
<script src="https://unpkg.com/mermaid@10.9.0/dist/mermaid.min.js"></script>
<script>
  mermaid.initialize({
    startOnLoad: true,
    theme: "neutral"
  });
</script>

<style>
  .diagram-block {
    max-width: 1000px;
    margin: 32px auto;
    padding: 16px;
    background: #f9fafb;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  }
  .diagram-block h2 {
    margin-top: 0;
    font-weight: 600;
  }
</style>

<div class="diagram-block">
<h2>Architecture Diagram</h2>

<div class="mermaid">
graph LR
  Client[Client]
  Ingress[Ingress]
  ServiceSvc[Billing Service]
  Pod[Billing Pod]
  Controller[REST Controller]
  ServiceLayer[Service Layer]
  Repo[JPA Repositories]
  Rabbit[RabbitMQ]
  Queue[invoice_events_queue]
  DB[Billing Database]
  Client --> Ingress
  Ingress --> ServiceSvc
  ServiceSvc --> Pod
  Pod --> Controller
  Controller --> ServiceLayer
  ServiceLayer --> Repo
  Repo --> DB
  ServiceLayer --> Rabbit
  Rabbit --> Queue
</div>
</div>

<div class="diagram-block">
<h2>Entity Relationship Diagram (ERD)</h2>

<div class="mermaid">
erDiagram
  INVOICE {
    bigint id
    string patient_reference
    string status
    double subtotal
    double tax
    double total
    double balance
    datetime created_at
  }

  INVOICE_LINE_ITEM {
    bigint invoice_id
    string code
    string description
    int quantity
    double unit_price
    double amount
  }

  PAYMENT {
    bigint id
    bigint invoice_id
    double amount
    string method
    string status
    string external_ref
    datetime created_at
  }

  INVOICE ||--o{ INVOICE_LINE_ITEM : has_line_items
  INVOICE ||--o{ PAYMENT : has_payments
</div>
</div>
