# Smart Inventory & Supply Chain Intelligence System

A Java desktop application for intelligent inventory management across multi-location warehouse networks. The system processes real retail and procurement data to provide AI-driven demand forecasting, supplier performance evaluation, dead stock detection, automated stock rebalancing, and a tamper-proof audit ledger under role-based access control.

---

## Features

| Module | Description |
|----------|-------------|
| **Live Dashboard** | Real-time stock levels across all warehouses, low-stock alerts, and inventory ledger logs |
| **AI Demand Forecasting** | Seasonal Moving Average algorithm trained on 73,100 retail transaction records |
| **Supplier Scoring** | Weighted KPI engine using Reliability (40%), Lead Time (30%), Defect Rate (20%), and Cost (10%) |
| **Dead Stock Detection** | Flags products idle for 60+ days and recommends markdown strategies |
| **Auto Rebalancing** | Identifies and resolves stock imbalances across warehouse locations |
| **Immutable Ledger** | Append-only audit trail for every stock movement |
| **PDF Reports** | Inventory, supplier performance, and dead stock reports |
| **Role-Based Access** | Admin, Manager, and Staff roles with warehouse-level permissions |

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java (JDK) | 17 LTS | Core application development |
| Apache Maven | 3.9.x | Build and dependency management |
| MySQL | 8.0 CE | Relational database |
| Java Swing | Built-in | Desktop GUI framework |
| JFreeChart | 1.5.4 | Demand forecast visualization |
| OpenCSV | 5.7.1 | CSV data processing |
| iText PDF | 5.5.13.3 | PDF report generation |
| JUnit 5 | 5.10.x | Unit testing |
| Mockito | 5.x | Mock-based testing |

---

## Project Highlights

- Multi-warehouse inventory management
- AI-based demand forecasting
- Supplier performance evaluation
- Dead stock identification and recommendations
- Automated inventory rebalancing
- Secure audit logging
- PDF report generation
- Role-based authentication and authorization

---

## Architecture

```text
Presentation Layer (Java Swing UI)
            │
            ▼
Service Layer (Business Logic & AI Models)
            │
            ▼
Data Access Layer (DAO Pattern)
            │
            ▼
MySQL Database
```

**Design Patterns Used**
- MVC
- DAO
- Service Layer
- Singleton
- Event Sourcing

---

## Algorithms Implemented

### Demand Forecasting
1. Aggregate transaction data into monthly sales.
2. Calculate a 3-month moving average.
3. Generate seasonal indices.
4. Forecast future demand using seasonal adjustments.

### Supplier Scoring

Score Formula:

```text
Score =
(Reliability × 0.40) +
(Lead Time × 0.30) +
(Defect Rate × 0.20) +
(Cost Efficiency × 0.10)
```

Grades:

| Score | Grade |
|---------|---------|
| 90+ | A+ |
| 80–89 | A |
| 70–79 | B |
| 60–69 | C |
| 50–59 | D |
| Below 50 | F |

### Dead Stock Detection

Products are marked as dead stock when:
- No sales activity for 60+ days
- Positive inventory remains in stock

Markdown recommendations:

| Days Unsold | Discount |
|-------------|-----------|
| 60+ | 20% |
| 90+ | 30% |
| 120+ | 40% |
| 180+ | 50% |

---

## Testing

| Test Type | Count |
|------------|--------|
| Unit Tests | 54 |
| Service Tests | 61 |
| End-to-End Tests | 13 |
| Smoke Tests | 1 |
| **Total** | **129** |

---

## Contributors

- **Devica Vasan**
- **Aarzoo Gupta**

---

## License

Developed as an academic capstone project.
