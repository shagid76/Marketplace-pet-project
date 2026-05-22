package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.CartDto;
import com.marketplace.backend.dto.ProductDto;
import com.marketplace.backend.model.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class CartMapper {
    public CartDto mapToDto(Cart cart, List<ProductDto> products) {
        return CartDto.builder()
                .id(cart.getId())
                .userid(cart.getUserId())
                .products(products)
                .build();
    }
}