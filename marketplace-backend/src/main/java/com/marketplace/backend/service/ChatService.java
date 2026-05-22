package com.marketplace.backend.service;

import com.marketplace.backend.dto.ChatDto;
import com.marketplace.backend.dto.ChatPreviewDto;
import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.ChatMapper;
import com.marketplace.backend.mapper.MessageMapper;
import com.marketplace.backend.model.Chat.Chat;
import com.marketplace.backend.model.Chat.CreateChatRequest;
import com.marketplace.backend.model.Message.Message;
import com.marketplace.backend.repository.ChatRepository;
import com.marketplace.backend.repository.MessageRepository;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;

    public ChatDto getChat(String id, String currentUserId) {
        Chat chat = findById(id);
        ensureParticipant(chat, currentUserId);
        return chatMapper.mapToDto(chat, findAllMessages(id));
    }

    public List<ChatPreviewDto> getMyChats(String currentUserId) {
        return chatRepository
                .findChatsWithLastMessage(currentUserId)
                .stream()
                .toList();
    }

    @Transactional
    public ChatDto createChat(CreateChatRequest request, String currentUserId) {
        validateCreateRequest(request, currentUserId);
        Pair users = normalizeUsers(currentUserId, request.getUser2Id());
        Optional<Chat> existing = chatRepository.findByUser1IdAndUser2Id(users.user1(), users.user2());
        if (existing.isPresent()) {
            return chatMapper.mapToDto(existing.get());
        }
        Chat chat = create(users);
        try {
            Chat saved = chatRepository.save(chat);
            return chatMapper.mapToDto(saved);
        } catch (DuplicateKeyException e) {
            return chatRepository.findByUser1IdAndUser2Id(users.user1(), users.user2())
                    .map(chatMapper::mapToDto)
                    .orElseThrow(() -> e);
        }
    }

    private Chat create(Pair users) {
        return Chat.builder()
                .id(UUID.randomUUID().toString())
                .user1Id(users.user1())
                .user2Id(users.user2())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public void deleteChat(String id, String currentUserId) {
        Chat chat = findById(id);
        ensureParticipant(chat, currentUserId);
        messageRepository.deleteAllByChatId(id);
        chatRepository.delete(chat);
    }

    private Chat findById(String id) {
        return chatRepository.findById(id).orElseThrow(() -> new NotFoundException("Chat with id " + id + " not found"));
    }

    private void ensureParticipant(Chat chat, String currentUserId) {
        if (!Objects.equals(chat.getUser1Id(), currentUserId) && !Objects.equals(chat.getUser2Id(), currentUserId)) {
            throw new AccessDeniedException("You do not have access to this chat");
        }
    }

    private void validateCreateRequest(CreateChatRequest request, String currentUserId) {
        if (request.getUser2Id() == null || request.getUser2Id().isBlank()) {
            throw new IllegalArgumentException("Target user ID cannot be blank");
        }

        if (currentUserId.equals(request.getUser2Id())) {
            throw new IllegalArgumentException("You cannot create a chat with yourself");
        }
    }

    private Pair normalizeUsers(String userA, String userB) {
        return userA.compareTo(userB) < 0 ? new Pair(userA, userB) : new Pair(userB, userA);
    }

    private List<MessageDto> findAllMessages(String chatId) {
        return messageRepository.findByChatId(chatId).stream()
                .sorted(Comparator.comparing(Message::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Message::getId, Comparator.nullsLast(String::compareTo)))
                .map(message -> messageMapper.mapToDto(message, false))
                .toList();
    }

    private record Pair(String user1, String user2) {
    }
}