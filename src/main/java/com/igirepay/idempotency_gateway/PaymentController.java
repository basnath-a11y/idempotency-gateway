package com.igirepay.idempotency_gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process-payment")
    public ResponseEntity<String> processPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody PaymentRequest request) {

        // Check if Idempotency-Key header is present
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"Missing Idempotency-Key header\"}");
        }

        try {
            PaymentResult result = paymentService.processPayment(idempotencyKey, request);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // Add cache hit header if duplicate request
            if (result.isCacheHit()) {
                headers.set("X-Cache-Hit", "true");
            }

            // Handle conflict — same key, different body
            if (result.getStatusCode() == 422) {
                return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .headers(headers)
                    .body("{\"error\":\"" + result.getResponseBody() + "\"}");
            }

            return ResponseEntity
                .status(result.getStatusCode())
                .headers(headers)
                .body(result.getResponseBody());

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }
}