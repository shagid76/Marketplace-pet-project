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
public class ChatPreviewDto {
    private String id;
    private String user1Id;
    private String user2Id;
    private String lastMessageText;
    private String lastMessageSenderId;
    private LocalDateTime lastMessageTime;
}