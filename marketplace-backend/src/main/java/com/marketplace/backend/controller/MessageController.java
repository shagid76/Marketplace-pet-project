package com.marketplace.backend.controller;

import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Chat message management")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "Delete a message and broadcast removal via WebSocket")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String id) {
        MessageDto message = messageService.deleteMessage(id, SecurityUtils.getCurrentUserIdOrThrow());
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatId(),
                message
        );

        return ResponseEntity.noContent().build();
    }
}
