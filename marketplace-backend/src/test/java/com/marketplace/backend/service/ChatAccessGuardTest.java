package com.marketplace.backend.service;

import com.marketplace.backend.model.Chat.Chat;
import com.marketplace.backend.repository.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class ChatAccessGuardTest {

	@Mock ChatRepository chatRepository;
	@InjectMocks ChatAccessGuard guard;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void isParticipant_user1_returnsTrue() {
		when(chatRepository.findById("chat-1")).thenReturn(Optional.of(
				Chat.builder().id("chat-1").user1Id("alice").user2Id("bob").build()
		));
		assertThat(guard.isParticipant("chat-1", "alice")).isTrue();
	}

	@Test
	void isParticipant_user2_returnsTrue() {
		when(chatRepository.findById("chat-1")).thenReturn(Optional.of(
				Chat.builder().id("chat-1").user1Id("alice").user2Id("bob").build()
		));
		assertThat(guard.isParticipant("chat-1", "bob")).isTrue();
	}

	@Test
	void isParticipant_strangerUser_returnsFalse() {
		when(chatRepository.findById("chat-1")).thenReturn(Optional.of(
				Chat.builder().id("chat-1").user1Id("alice").user2Id("bob").build()
		));
		assertThat(guard.isParticipant("chat-1", "eve")).isFalse();
	}

	@Test
	void isParticipant_chatNotFound_returnsFalse() {
		when(chatRepository.findById("ghost-chat")).thenReturn(Optional.empty());
		assertThat(guard.isParticipant("ghost-chat", "alice")).isFalse();
	}

	@Test
	void isParticipant_nullChatId_returnsFalse() {
		assertThat(guard.isParticipant(null, "alice")).isFalse();
	}

	@Test
	void isParticipant_nullUserId_returnsFalse() {
		assertThat(guard.isParticipant("chat-1", null)).isFalse();
	}

	@Test
	void ensureParticipant_validParticipant_doesNotThrow() {
		when(chatRepository.findById("chat-1")).thenReturn(Optional.of(
				Chat.builder().id("chat-1").user1Id("alice").user2Id("bob").build()
		));
		guard.ensureParticipant("chat-1", "alice"); // no exception expected
	}

	@Test
	void ensureParticipant_stranger_throwsAccessDenied() {
		when(chatRepository.findById("chat-1")).thenReturn(Optional.of(
				Chat.builder().id("chat-1").user1Id("alice").user2Id("bob").build()
		));
		assertThatThrownBy(() -> guard.ensureParticipant("chat-1", "eve"))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("do not have access");
	}
}
