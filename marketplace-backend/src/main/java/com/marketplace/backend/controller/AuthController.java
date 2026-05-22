package com.marketplace.backend.controller;

import com.marketplace.backend.dto.UserDto;
import com.marketplace.backend.dto.jwt.JwtAuthenticationDto;
import com.marketplace.backend.dto.jwt.RefreshTokenDto;
import com.marketplace.backend.exception.AuthenticationException;
import com.marketplace.backend.model.User.AuthRequest;
import com.marketplace.backend.model.User.CreateUserRequest;
import com.marketplace.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration, sign-in, and token refresh")
public class AuthController {
    private final UserService userService;

    @Operation(summary = "Register a new user account")
    @PostMapping("/registration")
    public ResponseEntity<UserDto> registerUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        UserDto userDto = userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @Operation(summary = "Sign in and receive access + refresh tokens")
    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationDto> signIn(@RequestBody @Valid AuthRequest authRequest) {
        JwtAuthenticationDto jwtAuthenticationDto = userService.signIn(authRequest);
        return ResponseEntity.ok(jwtAuthenticationDto);
    }

    @Operation(summary = "Exchange a refresh token for a new token pair")
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationDto> refresh(@RequestBody @Valid RefreshTokenDto refreshTokenDto)
            throws AuthenticationException {
        return ResponseEntity.ok(userService.refreshToken(refreshTokenDto));
    }
}