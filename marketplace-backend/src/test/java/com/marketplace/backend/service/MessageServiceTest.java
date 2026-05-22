package com.marketplace.backend.service;

import com.marketplace.backend.dto.MessageDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.MessageMapper;
import com.marketplace.backend.model.Message.CreateMessageRequest;
import com.marketplace.backend.model.Message.Message;
import com.marketplace.backend.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock MessageRepository messageRepository;
	@Mock MessageMapper messageMapper;

	@InjectMocks MessageService messageService;

	@Test
	void createMessage_savesAndReturnsDto() {
		CreateMessageRequest req = new CreateMessageRequest();
		req.setChatId("c-1");
		req.setText("hello");

		when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
		when(messageMapper.mapToDto(any(Message.class), anyBoolean())).thenReturn(new MessageDto());

		messageService.createMessage(req, "user-1");

		ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
		verify(messageRepository).save(captor.capture());
		assertThat(captor.getValue().getChatId()).isEqualTo("c-1");
		assertThat(captor.getValue().getText()).isEqualTo("hello");
		assertThat(captor.getValue().getSenderId()).isEqualTo("user-1");
		assertThat(captor.getValue().getId()).isNotBlank();
	}

	@Test
	void deleteMessage_whenOwner_deletes() {
		Message msg = Message.builder().id("m-1").senderId("user-1").chatId("c-1").build();
		when(messageRepository.findById("m-1")).thenReturn(Optional.of(msg));
		when(messageMapper.mapToDto(msg, true)).thenReturn(new MessageDto());

		messageService.deleteMessage("m-1", "user-1");

		verify(messageRepository).delete(msg);
	}

	@Test
	void deleteMessage_notOwner_throwsAccessDenied() {
		Message msg = Message.builder().id("m-1").senderId("user-1").build();
		when(messageRepository.findById("m-1")).thenReturn(Optional.of(msg));

		assertThatThrownBy(() -> messageService.deleteMessage("m-1", "intruder"))
				.isInstanceOf(AccessDeniedException.class);

		verify(messageRepository, never()).delete(any());
	}

	@Test
	void deleteMessage_notFound_throws() {
		when(messageRepository.findById("ghost")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> messageService.deleteMessage("ghost", "user-1"))
				.isInstanceOf(NotFoundException.class);
	}
}
