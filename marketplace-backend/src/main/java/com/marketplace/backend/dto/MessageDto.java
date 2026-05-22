package com.marketplace.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String id;
    private String chatId;
    private String senderId;
    private String text;
    private boolean deleted;
    private LocalDateTime createdAt;
}
