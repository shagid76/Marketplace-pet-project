package com.marketplace.marketplace.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateUserRequest {
    @Size(min = 4, max = 64, message = "Username must be between 4 and 64 characters!")
    private String username;
    private String address;
}