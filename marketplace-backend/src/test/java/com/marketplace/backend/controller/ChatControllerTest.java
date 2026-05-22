package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.ChatDto;
import com.marketplace.backend.model.Chat.CreateChatRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ChatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class,
		excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class ChatControllerTest {

	@Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
	@Autowired ObjectMapper objectMapper;

	@MockitoBean ChatService chatService;

	private MockedStatic<SecurityUtils> securityUtils;

	@BeforeEach
	void setUp() {
		securityUtils = mockStatic(SecurityUtils.class);
		securityUtils.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn("alice");
	}

	@AfterEach
	void tearDown() {
		securityUtils.close();
	}

	@Test
	@WithMockUser
	void createChat_returns201() throws Exception {
		CreateChatRequest req = new CreateChatRequest();
		req.setUser2Id("bob");
		ChatDto dto = new ChatDto();
		dto.setId("chat-1");
		when(chatService.createChat(any(), eq("alice"))).thenReturn(dto);

		mockMvc.perform(post("/api/chats")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("chat-1"));
	}

	@Test
	@WithMockUser
	void getChat_returnsDto() throws Exception {
		ChatDto dto = new ChatDto();
		dto.setId("chat-1");
		when(chatService.getChat(eq("chat-1"), eq("alice"))).thenReturn(dto);

		mockMvc.perform(get("/api/chats/chat-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("chat-1"));
	}

	@Test
	@WithMockUser
	void getMyChats_returnsList() throws Exception {
		when(chatService.getMyChats(eq("alice"))).thenReturn(List.of());

		mockMvc.perform(get("/api/chats"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	void deleteChat_returns204() throws Exception {
		mockMvc.perform(delete("/api/chats/chat-1"))
				.andExpect(status().isNoContent());
	}
}
