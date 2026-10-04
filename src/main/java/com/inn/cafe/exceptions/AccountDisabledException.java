package com.inn.cafe.exceptions;

/** Login attempt by an account an admin has disabled. Maps to 403 with a specific message. */
public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException(String message) {
        super(message);
    }
}
