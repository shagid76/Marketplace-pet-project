package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.UserDto;
import com.marketplace.backend.dto.jwt.JwtAuthenticationDto;
import com.marketplace.backend.exception.AuthenticationException;
import com.marketplace.backend.model.User.AuthRequest;
import com.marketplace.backend.model.User.CreateUserRequest;
import com.marketplace.backend.service.UserService;
import org.junit.jupiter.api.Test;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
		excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class AuthControllerTest {

	@Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
	@Autowired ObjectMapper objectMapper;
	@MockitoBean UserService userService;

	@Test
	@WithMockUser
	void register_validInput_returns201() throws Exception {
		CreateUserRequest req = CreateUserRequest.builder()
				.username("alice")
				.email("alice@example.com")
				.password("password123")
				.build();
		UserDto dto = new UserDto();
		dto.setId("user-1");
		when(userService.createUser(any())).thenReturn(dto);

		mockMvc.perform(post("/api/auth/registration")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("user-1"));
	}

	@Test
	@WithMockUser
	void register_invalidEmail_returns400() throws Exception {
		CreateUserRequest req = CreateUserRequest.builder()
				.username("alice")
				.email("not-an-email")
				.password("password123")
				.build();

		mockMvc.perform(post("/api/auth/registration")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	void register_shortPassword_returns400() throws Exception {
		CreateUserRequest req = CreateUserRequest.builder()
				.username("alice")
				.email("alice@example.com")
				.password("short")
				.build();

		mockMvc.perform(post("/api/auth/registration")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	void signIn_validCredentials_returnsTokenPair() throws Exception {
		AuthRequest req = AuthRequest.builder()
				.email("alice@example.com")
				.password("password123")
				.build();
		JwtAuthenticationDto tokens = JwtAuthenticationDto.builder()
				.token("access").refreshToken("refresh").build();
		when(userService.signIn(any())).thenReturn(tokens);

		mockMvc.perform(post("/api/auth/sign-in")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("access"))
				.andExpect(jsonPath("$.refreshToken").value("refresh"));
	}

	@Test
	@WithMockUser
	void signIn_wrongPassword_returns401() throws Exception {
		AuthRequest req = AuthRequest.builder()
				.email("alice@example.com")
				.password("password123")
				.build();
		when(userService.signIn(any())).thenThrow(new AuthenticationException("Email or password incorrect!"));

		mockMvc.perform(post("/api/auth/sign-in")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isUnauthorized());
	}
}
