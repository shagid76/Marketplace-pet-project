package com.marketplace.backend.controller;

import com.marketplace.backend.dto.CartDto;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get the number of items in the cart")
    @GetMapping("/length")
    public ResponseEntity<Integer> length() {
        Integer cartLength = cartService.getCartLength(SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(cartLength);
    }

    @Operation(summary = "Get the full cart with product details")
    @GetMapping()
    public ResponseEntity<CartDto> getCart() {
        CartDto myCart = cartService.getCart(SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(myCart);
    }

    @Operation(summary = "Add a product to the cart")
    @PatchMapping("/add/{productId}")
    public ResponseEntity<CartDto> addProduct(@PathVariable String productId) {
        CartDto cart = cartService.addProduct(productId, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Remove a product from the cart")
    @DeleteMapping("/{productId}")
    public ResponseEntity<CartDto> deleteProduct(@PathVariable String productId) {
        CartDto cart = cartService.removeProduct(productId, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(cart);
    }
}