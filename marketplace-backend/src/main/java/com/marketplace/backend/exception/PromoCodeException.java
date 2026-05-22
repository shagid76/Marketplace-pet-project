package com.marketplace.backend.exception;

import com.marketplace.backend.model.PromoCode.PromoCodeErrorCode;

public class PromoCodeException extends RuntimeException {
    private final PromoCodeErrorCode errorCode;

    public PromoCodeException(PromoCodeErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode.name();
    }
}