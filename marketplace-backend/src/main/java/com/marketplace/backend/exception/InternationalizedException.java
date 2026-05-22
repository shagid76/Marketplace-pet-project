package com.marketplace.backend.exception;

public class InternationalizedException extends RuntimeException {
    private final String[] params;

    public InternationalizedException(String message, String... params) {
        super(message);
        this.params = params;
    }
}
