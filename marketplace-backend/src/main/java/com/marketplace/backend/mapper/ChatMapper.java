package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.ChatDto;
import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.model.Chat.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMapper {
    public ChatDto mapToDto(Chat chat) {
        if (chat == null) return null;

        return ChatDto.builder()
                .id(chat.getId())
                .user1Id(chat.getUser1Id())
                .user2Id(chat.getUser2Id())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .build();
    }

    public ChatDto mapToDto(Chat chat, List<MessageDto> messages) {
        if (chat == null) return null;

        List<MessageDto> messageDtos = messages == null
                ? List.of()
                : messages.stream()
                .sorted(Comparator.comparing(MessageDto::getCreatedAt))
                .toList();

        return ChatDto.builder()
                .id(chat.getId())
                .user1Id(chat.getUser1Id())
                .user2Id(chat.getUser2Id())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .messages(messageDtos)
                .build();
    }
}