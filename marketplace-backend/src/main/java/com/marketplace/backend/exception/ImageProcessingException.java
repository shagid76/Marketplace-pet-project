package com.marketplace.backend.exception;

public class ImageProcessingException extends InternationalizedException {
    public ImageProcessingException(String message, String... params) {
        super(message, params);
    }
}
