package com.igirepay.idempotency_gateway;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "request_body", nullable = false, columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_processing")
    private boolean isProcessing;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String requestBody, String responseBody, int statusCode) {
        this.idempotencyKey = idempotencyKey;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.createdAt = LocalDateTime.now();
        this.isProcessing = false;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestBody() { return requestBody; }
    public String getResponseBody() { return responseBody; }
    public int getStatusCode() { return statusCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isProcessing() { return isProcessing; }

    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public void setProcessing(boolean processing) { this.isProcessing = processing; }
}