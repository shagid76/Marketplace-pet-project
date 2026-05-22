package com.marketplace.backend.service;

import com.marketplace.backend.dto.ChatDto;
import com.marketplace.backend.dto.ChatPreviewDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.ChatMapper;
import com.marketplace.backend.mapper.MessageMapper;
import com.marketplace.backend.model.Chat.Chat;
import com.marketplace.backend.model.Chat.CreateChatRequest;
import com.marketplace.backend.repository.ChatRepository;
import com.marketplace.backend.repository.MessageRepository;
import com.mongodb.DuplicateKeyException;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock ChatRepository chatRepository;
	@Mock MessageRepository messageRepository;
	@Mock MessageMapper messageMapper;
	@Mock ChatMapper chatMapper;

	@InjectMocks ChatService chatService;

	@Test
	void createChat_existingChat_returnsExisting() {
		CreateChatRequest req = new CreateChatRequest();
		req.setUser2Id("bob");

		Chat existing = Chat.builder().id("c-1").user1Id("alice").user2Id("bob").build();
		when(chatRepository.findByUser1IdAndUser2Id(any(), any())).thenReturn(Optional.of(existing));
		when(chatMapper.mapToDto(existing)).thenReturn(new ChatDto());

		chatService.createChat(req, "alice");

		verify(chatRepository).findByUser1IdAndUser2Id(any(), any());
	}

	@Test
	void createChat_newChat_savesAndReturns() {
		CreateChatRequest req = new CreateChatRequest();
		req.setUser2Id("bob");

		when(chatRepository.findByUser1IdAndUser2Id(any(), any())).thenReturn(Optional.empty());
		when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
		when(chatMapper.mapToDto(any(Chat.class))).thenReturn(new ChatDto());

		chatService.createChat(req, "alice");

		verify(chatRepository).save(any(Chat.class));
	}

	@Test
	void createChat_duplicateKey_recovers() {
		CreateChatRequest req = new CreateChatRequest();
		req.setUser2Id("bob");

		Chat raced = Chat.builder().id("c-1").user1Id("alice").user2Id("bob").build();

		when(chatRepository.findByUser1IdAndUser2Id(any(), any()))
				.thenReturn(Optional.empty())   // first lookup
				.thenReturn(Optional.of(raced)); // retry after duplicate key
		when(chatRepository.save(any(Chat.class)))
				.thenThrow(new DuplicateKeyException(new BsonDocument(), null, null));
		when(chatMapper.mapToDto(raced)).thenReturn(new ChatDto());

		chatService.createChat(req, "alice");

		verify(chatMapper).mapToDto(raced);
	}

	@Test
	void createChat_selfChat_throwsIllegalArgument() {
		CreateChatRequest req = new CreateChatRequest();
		req.setUser2Id("alice");

		assertThatThrownBy(() -> chatService.createChat(req, "alice"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("cannot create a chat with yourself");
	}

	@Test
	void createChat_blankTargetId_throws() {
		CreateChatRequest req = new CreateChatRequest();
		req.setUser2Id(" ");

		assertThatThrownBy(() -> chatService.createChat(req, "alice"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void getChat_notParticipant_throwsAccessDenied() {
		Chat chat = Chat.builder().id("c-1").user1Id("alice").user2Id("bob").build();
		when(chatRepository.findById("c-1")).thenReturn(Optional.of(chat));

		assertThatThrownBy(() -> chatService.getChat("c-1", "eve"))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void getChat_notFound_throwsNotFound() {
		when(chatRepository.findById("ghost")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> chatService.getChat("ghost", "alice"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void deleteChat_deletesMessagesAndChat() {
		Chat chat = Chat.builder().id("c-1").user1Id("alice").user2Id("bob").build();
		when(chatRepository.findById("c-1")).thenReturn(Optional.of(chat));

		chatService.deleteChat("c-1", "alice");

		verify(messageRepository).deleteAllByChatId("c-1");
		verify(chatRepository).delete(chat);
	}

	@Test
	void getMyChats_delegatesToAggregation() {
		when(chatRepository.findChatsWithLastMessage("alice"))
				.thenReturn(List.of(new ChatPreviewDto()));

		var result = chatService.getMyChats("alice");

		verify(chatRepository).findChatsWithLastMessage("alice");
	}
}
