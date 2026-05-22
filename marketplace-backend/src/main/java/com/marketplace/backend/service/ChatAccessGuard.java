package com.marketplace.backend.service;

import com.marketplace.backend.model.Chat.Chat;
import com.marketplace.backend.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ChatAccessGuard {

    private final ChatRepository chatRepository;

    public boolean isParticipant(String chatId, String userId) {
        if (chatId == null || userId == null) {
            return false;
        }
        return chatRepository.findById(chatId)
                .map(chat -> isParticipant(chat, userId))
                .orElse(false);
    }

    public void ensureParticipant(String chatId, String userId) {
        if (!isParticipant(chatId, userId)) {
            throw new AccessDeniedException("You do not have access to this chat");
        }
    }

    private boolean isParticipant(Chat chat, String userId) {
        return Objects.equals(chat.getUser1Id(), userId)
                || Objects.equals(chat.getUser2Id(), userId);
    }
}
