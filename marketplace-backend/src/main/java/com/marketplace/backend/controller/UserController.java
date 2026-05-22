package com.marketplace.backend.controller;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.UserDto;
import com.marketplace.backend.model.User.UpdateUserRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profiles, wishlist, and search")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get the authenticated user's own profile")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto user = userService.findMeById(SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Operation(summary = "List all users (admin/moderator only)")
    @GetMapping()
    public ResponseEntity<PageResponseDto<UserDto>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {
        PageResponseDto<UserDto> users = userService.findAll(page, size);
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search users by username")
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String username) {
        List<UserDto> users = userService.searchByUsername(username);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get a public user profile by ID")
    @GetMapping("/{targetId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String targetId) {
        UserDto user = userService.findUserById(targetId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update the authenticated user's profile and avatar")
    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUser(@ModelAttribute @Valid UpdateUserRequest updateUserRequest) {
        UserDto user = userService.update(SecurityUtils.getCurrentUserIdOrThrow(), updateUserRequest);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Add a product to the authenticated user's wishlist")
    @PatchMapping("/wishlist/{productId}")
    public ResponseEntity<Void> addToWishlist(@PathVariable String productId) {
        userService.addProductToWishList(productId, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove a product from the authenticated user's wishlist")
    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable String productId) {
        userService.deleteProductFromWishList(productId, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.noContent().build();
    }
}