package com.inn.cafe.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    ORDER_PLACED("Order is Placed");

    private final String statusMessage;

    OrderStatus(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    // Without @JsonValue, Jackson serialises the enum by its constant name
    // (ORDER_PLACED) rather than this human-readable message, so every API
    // response showed the raw enum to the client instead of readable text.
    @JsonValue
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public String toString() {
        return statusMessage;
    }

}
