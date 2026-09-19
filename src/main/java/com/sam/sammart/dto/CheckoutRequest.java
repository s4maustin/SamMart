package com.sam.sammart.dto;

/**
 * Builder for assembling checkout payloads without exposing entity internals.
 */
public class CheckoutRequest {
    private final boolean confirmPayment;
    private final String note;

    private CheckoutRequest(Builder builder) {
        this.confirmPayment = builder.confirmPayment;
        this.note = builder.note;
    }

    public boolean isConfirmPayment() {
        return confirmPayment;
    }

    public String getNote() {
        return note;
    }

    public static class Builder {
        private boolean confirmPayment;
        private String note;

        public Builder confirmPayment(boolean confirmPayment) {
            this.confirmPayment = confirmPayment;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public CheckoutRequest build() {
            return new CheckoutRequest(this);
        }
    }
}
