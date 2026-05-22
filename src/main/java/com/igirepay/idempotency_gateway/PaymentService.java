package com.igirepay.idempotency_gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PaymentService {

    @Autowired
    private IdempotencyRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // For handling race conditions (bonus user story)
    private final Map<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    public PaymentResult processPayment(String idempotencyKey, PaymentRequest request) throws Exception {

        String requestBody = request.toString();

        // Get or create a lock for this key
        keyLocks.putIfAbsent(idempotencyKey, new ReentrantLock());
        ReentrantLock lock = keyLocks.get(idempotencyKey);
        lock.lock();

        try {
            // Check if key already exists
            Optional<IdempotencyRecord> existing = repository.findById(idempotencyKey);

            if (existing.isPresent()) {
                IdempotencyRecord record = existing.get();

                // If still processing, wait (already handled by lock above)

                // Check if same request body
                if (!record.getRequestBody().equals(requestBody)) {
                    return new PaymentResult(
                        "Idempotency key already used for a different request body.",
                        422,
                        false
                    );
                }

                // Return cached response
                return new PaymentResult(record.getResponseBody(), record.getStatusCode(), true);
            }

            // Mark as processing
            IdempotencyRecord record = new IdempotencyRecord(
                idempotencyKey, requestBody, "", 0
            );
            record.setProcessing(true);
            repository.save(record);

            // Simulate payment processing (2 second delay)
            Thread.sleep(2000);

            // Build response
            String responseBody = "{\"status\":\"success\",\"message\":\"Charged " 
                + request.getAmount() + " " + request.getCurrency() + "\"}";

            // Save final response
            record.setResponseBody(responseBody);
            record.setStatusCode(201);
            record.setProcessing(false);
            repository.save(record);

            return new PaymentResult(responseBody, 201, false);

        } finally {
            lock.unlock();
        }
    }
}