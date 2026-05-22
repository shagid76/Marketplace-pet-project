package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.UserDto;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean UserService userService;

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

    private UserDto userDto(String id) {
        UserDto dto = new UserDto();
        dto.setId(id);
        return dto;
    }

    @Test
    @WithMockUser
    void getCurrentUser_returnsDto() throws Exception {
        when(userService.findMeById("user-1")).thenReturn(userDto("user-1"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returnsPage() throws Exception {
        when(userService.findAll(0, 5)).thenReturn(new PageResponseDto<>());

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserById_returnsDto() throws Exception {
        when(userService.findUserById("user-2")).thenReturn(userDto("user-2"));

        mockMvc.perform(get("/api/users/user-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-2"));
    }

    @Test
    @WithMockUser
    void updateUser_multipart_returnsOk() throws Exception {
        when(userService.update(eq("user-1"), any())).thenReturn(userDto("user-1"));

        mockMvc.perform(multipart("/api/users/me")
                        .param("username", "newusername")
                        .with(request -> { request.setMethod("POST"); return request; })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"));
    }

    @Test
    @WithMockUser
    void addToWishlist_returnsNoContent() throws Exception {
        when(userService.addProductToWishList(eq("p-1"), eq("user-1"))).thenReturn(userDto("user-1"));

        mockMvc.perform(patch("/api/users/wishlist/p-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void removeFromWishlist_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/wishlist/p-1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteProductFromWishList("p-1", "user-1");
    }
}
