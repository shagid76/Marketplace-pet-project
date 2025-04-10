package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.model.CreateUserRequest;
import com.marketplace.marketplace.model.UpdateUserRequest;
import com.marketplace.marketplace.model.User;
import com.marketplace.marketplace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atmosphere.config.service.Post;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public List<User> findAll(){
        return userService.findAll();
    }

    @GetMapping("/users/{id}")
    public User findById(@PathVariable("id") String id){
        return userService.findById(id);
    }

    @PutMapping("/users/put")
    public User putUser(@RequestBody @Valid CreateUserRequest createUserRequest){
        return userService.createUser(createUserRequest);
    }

    @PatchMapping("/users/update")
    public User updateUser(@RequestBody @Valid UpdateUserRequest updateUserRequest,
                           @RequestBody String id){
        return userService.updateUser(updateUserRequest, id);
    }
}