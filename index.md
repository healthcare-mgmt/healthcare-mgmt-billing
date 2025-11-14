---
title: Billing Service – Architecture & ER Diagrams
---

<div class="page-wrapper">

# Billing Service – Architecture & ER Diagrams

This page documents the high-level architecture and the billing domain model.

</div>

<!-- Load Mermaid for GitHub Pages -->
<script src="https://unpkg.com/mermaid@10.9.0/dist/mermaid.min.js"></script>
<script>
  mermaid.initialize({
    startOnLoad: true,
    theme: "neutral"
  });
</script>

<style>
  .page-wrapper {
    max-width: 1000px;
    margin: 40px auto;
    padding: 0 16px 40px;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    line-height: 1.5;
  }
  .page-wrapper h1, .page-wrapper h2 {
    font-weight: 600;
  }
  .diagram-block {
    max-width: 1000px;
    margin: 0 auto 48px;
    padding: 16px;
    background: #f9fafb;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
  }
  .diagram-block h2 {
    margin-top: 0;
  }
</style>

<div class="diagram-block">
<h2>Architecture Diagram</h2>

<div class="mermaid">
flowchart LR
  user[Client Application]

  subgraph k8s[Kubernetes Cluster]
    ingress[Ingress Controller]
    svc[Billing Service (Service)]

    subgraph pod[Billing Service Pod]
      controller[REST Controller]
      service[Billing Service]
      repo[JPA Repositories]
    end

    subgraph rabbit[RabbitMQ]
      queue[invoice.events queue]
    end

    db[(Billing Database)]
  end

  user -->|HTTPS| ingress
  ingress --> svc
  svc --> controller
  controller --> service
  service --> repo
  repo --> db

  service -->|publish events| queue
  queue -->|async consumers| pod
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
