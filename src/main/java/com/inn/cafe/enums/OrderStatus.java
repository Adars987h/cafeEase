package com.inn.cafe.enums;

public enum OrderStatus {
    ORDER_PLACED("Order is Placed");

    private final String statusMessage;

    OrderStatus(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public String toString() {
        return statusMessage;
    }

}
