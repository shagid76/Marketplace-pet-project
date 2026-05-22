package com.marketplace.backend.dto;

import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.User.Role;
import com.marketplace.backend.model.User.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private String avatar;
    private Set<Role> role = new HashSet<>();
    private LocalDateTime createdAt;
    private List<ProductDto> wishlist;
    private UserStatus userStatus;
}