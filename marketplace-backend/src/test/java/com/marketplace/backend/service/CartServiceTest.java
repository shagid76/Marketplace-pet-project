package com.marketplace.backend.service;

import com.marketplace.backend.dto.CartDto;
import com.marketplace.backend.exception.BadRequestException;
import com.marketplace.backend.exception.ConflictException;
import com.marketplace.backend.exception.ForbiddenException;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.CartMapper;
import com.marketplace.backend.mapper.ProductMapper;
import com.marketplace.backend.model.Cart.Cart;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductStatus;
import com.marketplace.backend.repository.CartRepository;
import com.marketplace.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock CartRepository cartRepository;
	@Mock ProductRepository productRepository;
	@Mock MinioService minioService;
	@Mock CartMapper cartMapper;
	@Mock ProductMapper productMapper;

	@InjectMocks CartService cartService;

	@Test
	void create_whenNoExistingCart_savesAndReturnsDto() {
		when(cartRepository.findByUserId("user-1")).thenReturn(Optional.empty());
		when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
		when(cartMapper.mapToDto(any(Cart.class), any())).thenReturn(new CartDto());

		CartDto result = cartService.create("user-1");

		assertThat(result).isNotNull();
		verify(cartRepository).save(any(Cart.class));
	}

	@Test
	void create_whenCartAlreadyExists_throwsBadRequest() {
		when(cartRepository.findByUserId("user-1"))
				.thenReturn(Optional.of(Cart.builder().id("cart-1").userId("user-1").build()));

		assertThatThrownBy(() -> cartService.create("user-1"))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	void addProduct_happyPath_addsToCart() {
		Product product = activeInStockProduct("p-1");
		Cart cart = Cart.builder().id("c-1").userId("user-1").products(new ArrayList<>()).build();

		when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
		when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
		when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
		when(cartMapper.mapToDto(any(Cart.class), any())).thenReturn(new CartDto());

		cartService.addProduct("p-1", "user-1");

		assertThat(cart.getProducts()).containsExactly("p-1");
		verify(cartRepository).save(cart);
	}

	@Test
	void addProduct_bannedProduct_rejectedWith403() {
		Product banned = Product.builder()
				.id("p-1")
				.productStatus(ProductStatus.BANNED)
				.inStock(true)
				.build();
		when(productRepository.findById("p-1")).thenReturn(Optional.of(banned));

		assertThatThrownBy(() -> cartService.addProduct("p-1", "user-1"))
				.isInstanceOf(ForbiddenException.class)
				.hasMessageContaining("banned");

		verify(cartRepository, never()).save(any());
	}

	@Test
	void addProduct_soldOut_rejected() {
		Product sold = Product.builder()
				.id("p-1")
				.productStatus(ProductStatus.ACTIVE)
				.inStock(false)
				.build();
		when(productRepository.findById("p-1")).thenReturn(Optional.of(sold));

		assertThatThrownBy(() -> cartService.addProduct("p-1", "user-1"))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("already sold");
	}

	@Test
	void addProduct_alreadyInCart_throwsConflict() {
		Product product = activeInStockProduct("p-1");
		Cart cart = Cart.builder()
				.id("c-1")
				.userId("user-1")
				.products(new ArrayList<>(List.of("p-1")))
				.build();
		when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
		when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));

		assertThatThrownBy(() -> cartService.addProduct("p-1", "user-1"))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("already in cart");
	}

	@Test
	void removeProduct_inCart_removes() {
		Cart cart = Cart.builder()
				.id("c-1")
				.userId("user-1")
				.products(new ArrayList<>(List.of("p-1", "p-2")))
				.build();
		when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
		when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
		when(cartMapper.mapToDto(any(Cart.class), any())).thenReturn(new CartDto());

		cartService.removeProduct("p-1", "user-1");

		assertThat(cart.getProducts()).containsExactly("p-2");
	}

	@Test
	void removeProduct_notInCart_throwsBadRequest() {
		Cart cart = Cart.builder()
				.id("c-1")
				.userId("user-1")
				.products(new ArrayList<>())
				.build();
		when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));

		assertThatThrownBy(() -> cartService.removeProduct("p-1", "user-1"))
				.isInstanceOf(BadRequestException.class);
	}

	@Test
	void clearCart_emptiesProducts() {
		Cart cart = Cart.builder()
				.id("c-1")
				.userId("user-1")
				.products(new ArrayList<>(List.of("p-1", "p-2")))
				.build();
		when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));

		cartService.clearCart("user-1");

		assertThat(cart.getProducts()).isEmpty();
		verify(cartRepository).save(cart);
	}

	@Test
	void getCart_notFound_throwsNotFound() {
		when(cartRepository.findByUserId("ghost")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> cartService.getCart("ghost"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void getCartLength_returnsSize() {
		Cart cart = Cart.builder()
				.products(new ArrayList<>(List.of("p-1", "p-2", "p-3")))
				.build();
		when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));

		assertThat(cartService.getCartLength("user-1")).isEqualTo(3);
	}

	private Product activeInStockProduct(String id) {
		return Product.builder()
				.id(id)
				.productStatus(ProductStatus.ACTIVE)
				.inStock(true)
				.build();
	}
}
