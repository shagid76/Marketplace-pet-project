package com.marketplace.backend.model.PromoCode;

public enum PromoCodeErrorCode {
    NOT_ACTIVE,
    EXPIRED,
    NOT_STARTED,
    NOT_APPLICABLE,
    MIN_ORDER_AMOUNT_NOT_MET,
    MIN_PRODUCTS_NOT_MET,
    USAGE_LIMIT_EXCEEDED,
    NO_PRODUCTS,
    INVALID_CODE,
}
