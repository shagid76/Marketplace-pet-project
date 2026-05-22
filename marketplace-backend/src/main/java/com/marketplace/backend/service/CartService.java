package com.marketplace.backend.service;

import com.marketplace.backend.dto.CartDto;
import com.marketplace.backend.dto.ProductDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final MinioService minioService;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartDto getCart(String userId) {
        Cart cart = findCartByUserId(userId);
        return cartMapper.mapToDto(cart, findCartProducts(cart.getProducts()));
    }

    public Integer getCartLength(String userId) {
        Cart cart = findCartByUserId(userId);
        return cart.getProducts().size();
    }

    private List<ProductDto> findCartProducts(List<String> products) {
        return productRepository.findAllById(products).stream()
                .map(product -> productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages())))
                .toList();
    }

    public CartDto create(String userId) {
        checkIfCartExist(userId);
        Cart cart = createCart(userId);
        return cartMapper.mapToDto(cart, findCartProducts(cart.getProducts()));
    }

    private void checkIfCartExist(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            throw new BadRequestException("Cart for this user already exists");
        });
    }

    private Cart createCart(String userId) {
        Cart cart = Cart.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .products(new ArrayList<>())
                .build();
        return cartRepository.save(cart);
    }

    private Product findProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    private Cart findCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + userId));
    }

    public CartDto addProduct(String productId, String userId) {
        Product product = findProductById(productId);
        validateAddedToCart(product);
        Cart cart = findCartByUserId(userId);
        checkProductNotInCart(cart, productId);
        cart.getProducts().add(productId);
        cartRepository.save(cart);
        return cartMapper.mapToDto(cart, findCartProducts(cart.getProducts()));
    }

    private void validateAddedToCart(Product product) {
        if (product.getProductStatus().equals(ProductStatus.BANNED)) {
            throw new ForbiddenException("Action impossible: product is banned.");
        }
        if (!product.isInStock()) {
            throw new BadRequestException("Product already sold");
        }
    }

    private void checkProductNotInCart(Cart cart, String productId) {
        if (cart.getProducts().contains(productId)) {
            throw new ConflictException("Product already in cart with id: " + productId);
        }
    }

    public CartDto removeProduct(String productId, String userId) {
        Cart cart = findCartByUserId(userId);
        checkProductInCart(cart, productId);
        cart.getProducts().remove(productId);
        cartRepository.save(cart);
        return cartMapper.mapToDto(cart, findCartProducts(cart.getProducts()));
    }

    private void checkProductInCart(Cart cart, String productId) {
        if (!cart.getProducts().contains(productId))
            throw new BadRequestException("Product not in cart with id: " + productId);
    }

    public void clearCart(String userId) {
        Cart cart = findCartByUserId(userId);
        cart.getProducts().clear();
        cartRepository.save(cart);
    }
}