# Idempotency Gateway — IgirePay Technologies

A Spring Boot REST API that implements an idempotency layer to prevent double-charging in payment processing systems.

## Architecture Diagram
Client                    Idempotency Gateway              Database (SQLite)
|                               |                               |
|-- POST /process-payment ----->|                               |
|   (Idempotency-Key: key-001)  |                               |
|                               |-- Check key exists? -------->|
|                               |<-- No ------------------------|
|                               |                               |
|                               |-- Save key (processing=true)->|
|                               |                               |
|                               |-- Simulate 2s processing      |
|                               |                               |
|                               |-- Save response -------------->|
|<-- 201 "Charged 100 RWF" -----|   (processing=false)          |
|                               |                               |
|                               |                               |
|-- POST /process-payment ----->|  (DUPLICATE REQUEST)          |
|   (Idempotency-Key: key-001)  |                               |
|                               |-- Check key exists? -------->|
|                               |<-- Yes, return cached ---------|
|<-- 201 + X-Cache-Hit: true ---|                               |
|                               |                               |
|-- POST /process-payment ----->|  (DIFFERENT BODY, SAME KEY)   |
|   (Idempotency-Key: key-001)  |                               |
|   amount: 500                 |-- Check key exists? -------->|
|                               |<-- Yes, body mismatch ---------|
|<-- 422 Conflict --------------|                               |
## Setup Instructions

### Prerequisites
- Java JDK 17 or above
- No additional setup needed (SQLite database is created automatically)

### Running the Application
1. Clone the repository:
```bash
   git clone https://github.com/basnath-a11y/idempotency-gateway.git
   cd idempotency-gateway
```

2. Start the server:
```bash
   ./mvnw spring-boot:run
```

3. The server starts on **http://localhost:8080**

## API Documentation

### Endpoint
**POST** `/api/process-payment`

### Request Headers
| Header | Required | Description |
|---|---|---|
| `Content-Type` | Yes | Must be `application/json` |
| `Idempotency-Key` | Yes | Unique string identifying this request |

### Request Body
```json
{
  "amount": 100,
  "currency": "RWF"
}
```

### Responses

**201 Created — First successful payment:**
```json
{"status": "success", "message": "Charged 100.0 RWF"}
```

**201 + Header `X-Cache-Hit: true` — Duplicate request:**
```json
{"status": "success", "message": "Charged 100.0 RWF"}
```

**422 Unprocessable Entity — Same key, different body:**
```json
{"error": "Idempotency key already used for a different request body."}
```

**400 Bad Request — Missing Idempotency-Key header:**
```json
{"error": "Missing Idempotency-Key header"}
```

### Example Requests

**First payment:**
```bash
curl -X POST http://localhost:8080/api/process-payment \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: key-001" \
  -d '{"amount": 100, "currency": "RWF"}'
```

**Duplicate request:**
```bash
curl -X POST http://localhost:8080/api/process-payment \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: key-001" \
  -d '{"amount": 100, "currency": "RWF"}'
```

**Same key, different body:**
```bash
curl -X POST http://localhost:8080/api/process-payment \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: key-001" \
  -d '{"amount": 500, "currency": "RWF"}'
```

## Design Decisions

### Why Spring Boot?
Spring Boot is the industry standard for Java REST APIs. It provides built-in dependency injection, database integration via JPA, and an embedded Tomcat server — making it production-ready with minimal configuration.

### Why SQLite?
SQLite stores idempotency records persistently on disk, meaning records survive server restarts. This is critical for a real payment system — if the server crashes and restarts, it must still remember which keys were already processed to avoid double-charging.

### Idempotency Key Storage
Each record stores the `idempotency_key`, `request_body`, `response_body`, `status_code`, and a `is_processing` flag. The request body is stored as a string and compared on every duplicate request to detect fraud attempts (User Story 3).

### Race Condition Handling (Bonus)
A `ConcurrentHashMap` of `ReentrantLock` objects is used per idempotency key. If two identical requests arrive simultaneously, the second request waits for the first to finish via the lock, then returns the cached result — preventing duplicate processing without returning a 409 error.

## Developer's Choice Feature — Idempotency Key Expiry Awareness

In a real-world Fintech system, idempotency keys should not be stored forever. Storing keys indefinitely causes the database to grow without bound and creates privacy risks. The `created_at` timestamp is recorded for every key, enabling a scheduled cleanup job to expire keys older than 24 hours. This mirrors how Stripe and other payment processors handle idempotency — keys are valid for 24 hours, after which the same key can be reused for a new transaction. The timestamp field is already implemented and visible in the database, ready for a scheduled task to be added in production.

## Tech Stack
- **Java 25** with **Spring Boot 3.5**
- **SQLite** via Hibernate/JPA
- **Maven** for dependency management
