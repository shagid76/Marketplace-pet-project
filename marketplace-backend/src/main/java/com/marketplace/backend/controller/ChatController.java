package com.marketplace.backend.controller;

import com.marketplace.backend.dto.ChatDto;
import com.marketplace.backend.dto.ChatPreviewDto;
import com.marketplace.backend.model.Chat.CreateChatRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
@Tag(name = "Chat", description = "Direct message chat rooms")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Create a new chat room with another user")
    @PostMapping()
    public ResponseEntity<ChatDto> createChat(@RequestBody @Valid CreateChatRequest createChatRequest) {
        ChatDto chatDto = chatService.createChat(createChatRequest, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.status(HttpStatus.CREATED).body(chatDto);
    }

    @Operation(summary = "Get a chat room and its message history")
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDto> getChat(@PathVariable String chatId) {
        ChatDto chatDto = chatService.getChat(chatId, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.status(HttpStatus.OK).body(chatDto);
    }

    @Operation(summary = "List all chat rooms the authenticated user belongs to")
    @GetMapping()
    public ResponseEntity<List<ChatPreviewDto>> getMyChats() {
        return ResponseEntity.ok(chatService.getMyChats(SecurityUtils.getCurrentUserIdOrThrow()));
    }

    @Operation(summary = "Delete a chat room (participants only)")
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable String chatId) {
        chatService.deleteChat(chatId, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.noContent().build();
    }
}
