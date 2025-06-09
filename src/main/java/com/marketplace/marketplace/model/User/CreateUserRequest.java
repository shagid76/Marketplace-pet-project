package com.marketplace.marketplace.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateUserRequest {
    @NotBlank(message = "Email can't be empty!")
    @Email(message = "Enter a valid email address!")
    private String email;

    @NotBlank(message = "Password can't be empty!")
    @Size(min = 8, message = "Password must be at least 8 characters long!")
    private String password;

    @NotBlank(message = "Username can't be empty!")
    @Size(min = 4, max = 64, message = "Username must be between 4 and 64 characters!")
    private String username;
}