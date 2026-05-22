package com.marketplace.backend.controller;

import com.marketplace.backend.dto.CartDto;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CartController.class,
		excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class CartControllerTest {

	@Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
	@MockitoBean CartService cartService;

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
	void getCartLength_returnsSize() throws Exception {
		when(cartService.getCartLength("user-1")).thenReturn(7);

		mockMvc.perform(get("/api/carts/length"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(7));
	}

	@Test
	@WithMockUser
	void getCart_returnsDto() throws Exception {
		CartDto dto = new CartDto();
		dto.setId("cart-1");
		when(cartService.getCart("user-1")).thenReturn(dto);

		mockMvc.perform(get("/api/carts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("cart-1"));
	}

	@Test
	@WithMockUser
	void addProduct_delegatesToService() throws Exception {
		when(cartService.addProduct(eq("p-1"), eq("user-1"))).thenReturn(new CartDto());

		mockMvc.perform(patch("/api/carts/add/p-1"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	void deleteProduct_delegatesToService() throws Exception {
		when(cartService.removeProduct(eq("p-1"), eq("user-1"))).thenReturn(new CartDto());

		mockMvc.perform(delete("/api/carts/p-1"))
				.andExpect(status().isOk());
	}
}
