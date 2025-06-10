package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.model.Item.Item;
import com.marketplace.marketplace.model.User.CreateUserRequest;
import com.marketplace.marketplace.model.User.UpdateUserRequest;
import com.marketplace.marketplace.model.User.User;
import com.marketplace.marketplace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("users")
public class UserController {
    private final UserService userService;

    @GetMapping()
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable("id") String id) {
        return userService.findById(id);
    }

    @PutMapping("/put")
    public User putUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @PatchMapping("/update")
    public User updateUser(@RequestBody @Valid UpdateUserRequest updateUserRequest,
                           @RequestBody String id) {
        return userService.updateUser(updateUserRequest, id);
    }

    @PatchMapping("/add-to-favourite")
    public User addItemToFavourite(@RequestBody String id, Item item) {
        return userService.addItemToFavourite(id, item);
    }

    @PatchMapping("/remove-from-favourite")
    public User removeItemFromFavourite(@RequestBody String id, Item item) {
        return userService.removeItemFromFavourite(id, item);
    }

    @PatchMapping("/add-item")
    public User addItem(@RequestBody String id, Item item) {
        return userService.addItem(id, item);
    }

    @PatchMapping("/add-to-basket")
    public User addItemToBasket(@RequestBody String id, Item item) {
        return userService.addItemToBasket(id, item);
    }

    @PatchMapping("/remove-from-basket")
    public User removeItemFromBasket(@RequestBody String id, Item item) {
        return userService.removeItemFromBasket(id, item);
    }

    @PatchMapping("/add-review")
    public User addReview(@RequestBody String id, Double rating) {
        return userService.updateRating(id, rating);
    }

    @GetMapping("/rating")
    public Double getRating(@RequestBody String id) {
        return userService.calculateRating(id);
    }

    @PatchMapping("/update-role")
    public User updateRole(@RequestBody String id) {
        return userService.updateRole(id);
    }

    @PatchMapping("/block")
    public User block(@RequestBody String id, @RequestBody LocalDateTime bannedUntil) {
        return userService.blockUntil(id, bannedUntil);
    }

    @PatchMapping("/ban")
    public User banUser(@RequestBody String id) {
        return userService.banUser(id);
    }
}