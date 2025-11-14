# Billing Service – Architecture & ER Diagrams

This page documents the high-level architecture and the billing domain model.

---

## Architecture Diagram

```mermaid
graph LR

    User[Client]

    subgraph K8sCluster["Kubernetes Cluster"]
        Ingress["Ingress Controller"]
        ServiceNode["Billing Service (ClusterIP)"]

        subgraph BillingPod["Billing Service Pod"]
            Controller["REST Controller"]
            ServiceLayer["Service Layer"]
            Repository["JPA Repository"]
        end

        subgraph RabbitMQ["RabbitMQ Cluster"]
            Queue["invoice.events Queue"]
        end

        DB[(Database)]
    end

    User --> Ingress
    Ingress --> ServiceNode
    ServiceNode --> Controller
    Controller --> ServiceLayer
    ServiceLayer --> Repository
    Repository --> DB

    ServiceLayer --> Queue
    Queue --> BillingPod
```

---

## Entity Relationship Diagram (ERD)

```mermaid
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
```
