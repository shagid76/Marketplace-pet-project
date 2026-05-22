package com.marketplace.backend.controller;

import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.MessageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MessageController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class MessageControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean MessageService messageService;
    @MockitoBean SimpMessagingTemplate messagingTemplate;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn("user-1");
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    @WithMockUser
    void deleteMessage_returnsNoContent_andSendsWebSocketEvent() throws Exception {
        MessageDto dto = new MessageDto();
        dto.setId("msg-1");
        dto.setChatId("chat-1");

        when(messageService.deleteMessage(eq("msg-1"), eq("user-1"))).thenReturn(dto);

        mockMvc.perform(delete("/api/messages/msg-1"))
                .andExpect(status().isNoContent());

        verify(messagingTemplate).convertAndSend(eq("/topic/chat/chat-1"), eq(dto));
    }
}
