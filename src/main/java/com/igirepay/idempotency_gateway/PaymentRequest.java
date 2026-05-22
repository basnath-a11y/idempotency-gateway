package com.igirepay.idempotency_gateway;

public class PaymentRequest {
    private double amount;
    private String currency;

    public PaymentRequest() {}

    public PaymentRequest(double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }

    public void setAmount(double amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }

    @Override
    public String toString() {
        return "{\"amount\":" + amount + ",\"currency\":\"" + currency + "\"}";
    }
}
