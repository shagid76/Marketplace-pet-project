package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.model.User.CreateUserRequest;
import com.marketplace.marketplace.model.User.UpdateUserRequest;
import com.marketplace.marketplace.model.User.User;
import com.marketplace.marketplace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}