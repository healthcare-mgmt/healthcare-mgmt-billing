# Billing Service – Architecture & ER Diagrams

Welcome to the documentation for the **Billing Service**.  
This page shows two critical views of the system:

1. **Overall Service Architecture** (Spring Boot + RabbitMQ + Kubernetes + DB)  
2. **Entity Relationship Diagram (ERD)** for the billing domain

---

# 🚀 Architecture Diagram

```mermaid
flowchart LR

  subgraph User["Client / Frontend"]
    UI["Web UI / Mobile App"]
  end

  subgraph K8s["Kubernetes Cluster"]
    
    subgraph BillingPod["Billing Service Pod"]
      Controller["Spring Boot<br/>REST Controller"]
      Service["Service Layer"]
      Repo["JPA Repository<br/>Layer"]
    end

    subgraph RabbitMQ["RabbitMQ Cluster / Pod"]
      Queue["invoice.events Queue"]
    end

    DB[(PostgreSQL / MySQL Database)]

    Ingress["Ingress Controller"]
    Svc["Billing Service - ClusterIP Service"]

  end

  %% Connections
  UI -->|"HTTPS"| Ingress
  Ingress -->|"Routes to"| Svc
  Svc -->|"Forwards to"| Controller

  Controller --> Service
  Service --> Repo
  Repo --> DB

  Service -->|"Publish events"| Queue
  Queue -->|"Async consumers"| BillingPod
```

---

# 📘 Entity Relationship Diagram (ERD)

```mermaid
erDiagram
  INVOICE {
    bigint id PK
    string patient_reference
    string status
    double subtotal
    double tax
    double total
    double balance
    datetime created_at
  }

  INVOICE_LINE_ITEM {
    bigint invoice_id FK
    string code
    string description
    int quantity
    double unit_price
    double amount
  }

  PAYMENT {
    bigint id PK
    bigint invoice_id FK
    double amount
    string method
    string status
    string external_ref
    datetime created_at
  }

  INVOICE ||--o{ INVOICE_LINE_ITEM : "has line items"
  INVOICE ||--o{ PAYMENT : "has payments"
```

---

## ✅ Notes

- This documentation is generated for GitHub Pages and fully supports Mermaid diagrams.
- Update this page anytime your architecture or domain entities change.
