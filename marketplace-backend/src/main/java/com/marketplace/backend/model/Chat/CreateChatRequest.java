package com.marketplace.backend.model.Chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRequest {

    @NotBlank(message = "Target user ID is required")
    private String user2Id;
}
