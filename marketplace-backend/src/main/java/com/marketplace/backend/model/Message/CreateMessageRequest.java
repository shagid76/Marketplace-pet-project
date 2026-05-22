package com.marketplace.backend.model.Message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMessageRequest {

    @NotBlank(message = "Chat ID is required")
    private String chatId;

    @NotBlank(message = "Message text cannot be empty")
    @Size(max = 2000, message = "Message text must be at most 2000 characters")
    private String text;
}
