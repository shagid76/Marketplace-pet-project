package com.marketplace.marketplace.service;

import com.marketplace.marketplace.model.User.CreateUserRequest;
import com.marketplace.marketplace.model.User.Role;
import com.marketplace.marketplace.model.User.UpdateUserRequest;
import com.marketplace.marketplace.model.User.User;
import com.marketplace.marketplace.repository.UserRepository;
import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public User findById(String id){
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found by id: " + id));
    }
    public User findByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with name: " + username + " not found!"));
    }

    public User createUser(CreateUserRequest createUserRequest){
        User user = User.builder()
                .email(createUserRequest.getEmail())
                .password(createUserRequest.getPassword())
                .username(createUserRequest.getUsername())
                .accountCreationDate(LocalDate.now())
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    public User updateUser(UpdateUserRequest updateUserRequest, String id){
        User user = findById(id);

        user.setUsername(updateUserRequest.getUsername());
        user.setAddress(updateUserRequest.getAddress());

        return userRepository.save(user);
    }
}