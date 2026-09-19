package com.sam.sammart.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock payment confirmation — no third-party gateway.
 */
public class MockPaymentChannel implements PaymentChannel {
    @Override
    public PaymentResult charge(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return new PaymentResult(false, "FAIL-" + UUID.randomUUID());
        }
        return new PaymentResult(true, "MOCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    }
}
