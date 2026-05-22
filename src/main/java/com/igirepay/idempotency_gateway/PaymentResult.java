package com.igirepay.idempotency_gateway;

public class PaymentResult {
    private String responseBody;
    private int statusCode;
    private boolean cacheHit;

    public PaymentResult(String responseBody, int statusCode, boolean cacheHit) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.cacheHit = cacheHit;
    }

    public String getResponseBody() { return responseBody; }
    public int getStatusCode() { return statusCode; }
    public boolean isCacheHit() { return cacheHit; }
}
