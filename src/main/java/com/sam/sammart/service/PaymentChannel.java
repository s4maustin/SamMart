package com.sam.sammart.service;

/**
 * Strategy pattern: mock payment channel used at checkout.
 */
public interface PaymentChannel {
    PaymentResult charge(java.math.BigDecimal amount);

    class PaymentResult {
        private final boolean success;
        private final String reference;

        public PaymentResult(boolean success, String reference) {
            this.success = success;
            this.reference = reference;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getReference() {
            return reference;
        }
    }
}
