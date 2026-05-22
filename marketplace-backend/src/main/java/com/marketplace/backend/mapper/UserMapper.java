package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.ProductDto;
import com.marketplace.backend.dto.UserDto;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {
    public UserDto mapToDto(User user, String avatarUrl) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(avatarUrl)
                .role(user.getRoles())
                .createdAt(user.getCreatedAt())
                .userStatus(user.getUserStatus())
                .build();
    }

    public UserDto mapMeToDto(User user, String avatarUrl, List<ProductDto> wishlist) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(avatarUrl)
                .role(user.getRoles())
                .createdAt(user.getCreatedAt())
                .wishlist(wishlist)
                .userStatus(user.getUserStatus())
                .build();
    }
}