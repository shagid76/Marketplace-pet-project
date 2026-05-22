package com.marketplace.backend.service;

import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.MessageMapper;
import com.marketplace.backend.model.Message.CreateMessageRequest;
import com.marketplace.backend.model.Message.Message;
import com.marketplace.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    private Message findById(String id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found message with id: " + id));
    }

    public MessageDto createMessage(CreateMessageRequest createMessageRequest, String currentUserId) {
        Message message = create(createMessageRequest, currentUserId);
        messageRepository.save(message);
        return messageMapper.mapToDto(message, false);
    }


    private Message create(CreateMessageRequest createMessageRequest, String currentUserId) {
        return Message.builder()
                .id(UUID.randomUUID().toString())
                .chatId(createMessageRequest.getChatId())
                .senderId(currentUserId)
                .text(createMessageRequest.getText())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public MessageDto deleteMessage(String id, String currentUserId) {
        Message message = findById(id);
        ensureOwner(message, currentUserId);
        messageRepository.delete(message);
        return messageMapper.mapToDto(message, true);
    }

    private void ensureOwner(Message message, String currentUserId) {
        if (!message.getSenderId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot delete this message");
        }
    }
}