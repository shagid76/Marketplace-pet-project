package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.model.Message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    public MessageDto mapToDto(Message message, boolean setDeleted) {
        if (message == null) return null;
        return MessageDto.builder()
                .id(message.getId())
                .chatId(message.getChatId())
                .senderId(message.getSenderId())
                .text(message.getText())
                .deleted(setDeleted)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
