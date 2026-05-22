package com.marketplace.backend.exception;

import com.marketplace.backend.model.PromoCode.PromoCodeErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResponseStatusException_withReason_returnsReasonInBody() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found");
        ResponseEntity<Object> response = handler.handleResponseStatusException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("item not found");
    }

    @Test
    void handleResponseStatusException_nullReason_returnsDefaultMessage() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN);
        ResponseEntity<Object> response = handler.handleResponseStatusException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("Error occurred");
    }

    @Test
    void handleAuthenticationException_returns401() {
        AuthenticationException ex = new AuthenticationException("bad token");
        ResponseEntity<Object> response = handler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("bad token");
    }

    @Test
    void handleBadRequest_returns400() {
        BadRequestException ex = new BadRequestException("invalid input");
        ResponseEntity<Object> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("invalid input");
    }

    @Test
    void handleNotFoundException_returns404() {
        NotFoundException ex = new NotFoundException("user not found");
        ResponseEntity<Object> response = handler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("user not found");
    }

    @Test
    void handleIllegalStateException_returns409() {
        IllegalStateException ex = new IllegalStateException("item already sold");
        ResponseEntity<Object> response = handler.handleIllegalStateException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("item already sold");
    }

    @Test
    void handleIllegalArgumentException_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("blank target id");
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("blank target id");
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("unexpected");
        ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message")).isEqualTo("Internal server error");
    }

    @Test
    void handlePromoCodeException_returns400WithErrorCode() {
        PromoCodeException ex = new PromoCodeException(PromoCodeErrorCode.EXPIRED, "promo code expired");
        ResponseEntity<Object> response = handler.handlePromoCodeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("message")).isEqualTo("promo code expired");
        assertThat(body.get("error")).isEqualTo("EXPIRED");
    }

    @Test
    void handleValidationExceptions_returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "must be a valid email");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<?, ?> errors = (Map<?, ?>) response.getBody().get("errors");
        assertThat(errors.get("email")).isEqualTo("must be a valid email");
    }

    @Test
    void handleValidationExceptions_nullFieldMessage_usesDefaultFallback() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "name", null, false, null, null, null);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        Map<?, ?> errors = (Map<?, ?>) response.getBody().get("errors");
        assertThat(errors.get("name")).isEqualTo("Invalid value");
    }

    @Test
    void handleMaxSizeException_returns413() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024);
        ResponseEntity<Map<String, Object>> response = handler.handleMaxSizeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody().get("message")).isEqualTo("File size exceeds the maximum permitted limit");
        assertThat(response.getBody().get("error")).isEqualTo("Payload Too Large");
    }

    @Test
    void handleAccessDeniedException_returns403() {
        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("forbidden");
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDeniedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("message"))
                .isEqualTo("You do not have permission to access this resource");
    }
}
