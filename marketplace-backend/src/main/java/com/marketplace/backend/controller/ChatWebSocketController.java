package com.marketplace.backend.controller;

import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.model.Message.CreateMessageRequest;
import com.marketplace.backend.security.CustomUserDetails;
import com.marketplace.backend.service.ChatAccessGuard;
import com.marketplace.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final ChatAccessGuard chatAccessGuard;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Valid CreateMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        String userId = resolveUserId(principal);
        chatAccessGuard.ensureParticipant(request.getChatId(), userId);

        MessageDto res = messageService.createMessage(request, userId);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getChatId(),
                res
        );
    }

    private String resolveUserId(Principal principal) {
        if (principal instanceof Authentication auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getId();
        }
        throw new AccessDeniedException("Invalid principal");
    }
}
