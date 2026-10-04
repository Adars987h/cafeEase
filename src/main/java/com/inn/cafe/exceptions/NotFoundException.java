package com.inn.cafe.exceptions;

/** The requested record does not exist, or the caller may not see it. Maps to 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
