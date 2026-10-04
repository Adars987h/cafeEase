package com.inn.cafe.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Set;

/**
 * Order lifecycle: PLACED -> PREPARING -> READY -> COMPLETED, or CANCELLED from any
 * non-final state. Stored by constant name, so ORDER_PLACED keeps its name for rows
 * written before the other states existed.
 */
public enum OrderStatus {
    ORDER_PLACED("Order is Placed"),
    PREPARING("Preparing"),
    READY("Ready for pickup"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String statusMessage;

    OrderStatus(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    // Serialised as the readable message (existing clients display it). Clients that need
    // a stable value read Order.statusCode instead.
    @JsonValue
    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean canMoveTo(OrderStatus next) {
        if (isFinal() || next == null || next == this) return false;
        if (next == CANCELLED) return true;
        return switch (this) {
            case ORDER_PLACED -> Set.of(PREPARING, READY, COMPLETED).contains(next);
            case PREPARING -> Set.of(READY, COMPLETED).contains(next);
            case READY -> next == COMPLETED;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return statusMessage;
    }
}
