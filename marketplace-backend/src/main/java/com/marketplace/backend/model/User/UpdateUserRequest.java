package com.marketplace.backend.model.User;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    @Size(min = 5, max = 128, message = "Username must be between 5 and 128 characters")
    private String username;

    private MultipartFile avatar;
    private Boolean removeAvatar;
}