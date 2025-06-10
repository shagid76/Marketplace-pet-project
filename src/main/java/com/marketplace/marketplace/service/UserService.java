package com.marketplace.marketplace.service;

import com.marketplace.marketplace.model.Item.Item;
import com.marketplace.marketplace.model.User.CreateUserRequest;
import com.marketplace.marketplace.model.User.Role;
import com.marketplace.marketplace.model.User.UpdateUserRequest;
import com.marketplace.marketplace.model.User.User;
import com.marketplace.marketplace.repository.UserRepository;
import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found by id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with name: " + username + " not found!"));
    }

    public User createUser(CreateUserRequest createUserRequest) {
        User user = User.builder()
                .email(createUserRequest.getEmail())
                .password(createUserRequest.getPassword())
                .username(createUserRequest.getUsername())
                .accountCreationDate(LocalDate.now())
                .rating(new ArrayList<>())
                .items(new ArrayList<>())
                .favouriteItems(new ArrayList<>())
                .basket(new ArrayList<>())
                .role(Set.of(Role.USER))
                .isBanned(false)
                .build();

        return userRepository.save(user);
    }

    public User updateUser(UpdateUserRequest updateUserRequest, String id) {
        User user = findById(id);
        user.setUsername(updateUserRequest.getUsername());
        user.setAddress(updateUserRequest.getAddress());
        return userRepository.save(user);
    }

    public User addItemToFavourite(String id, Item item) {
        User user = findById(id);
        user.getFavouriteItems().add(item);
        return userRepository.save(user);
    }

    public User removeItemFromFavourite(String id, Item item) {
        User user = findById(id);
        user.getFavouriteItems().remove(item);
        return userRepository.save(user);
    }

    public User addItem(String id, Item item) {
        User user = findById(id);
        user.getItems().add(item);
        return userRepository.save(user);
    }

    public User addItemToBasket(String id, Item item) {
        User user = findById(id);
        user.getBasket().add(item);
        return userRepository.save(user);
    }

    public User removeItemFromBasket(String id, Item item) {
        User user = findById(id);
        user.getBasket().remove(item);
        return userRepository.save(user);
    }

    public User updateRating(String id, Double rating) {
        User user = findById(id);
        user.getRating().add(rating);
        return userRepository.save(user);
    }

    public Double calculateRating(String id) {
        User user = findById(id);
        Double allRatings = user.getRating().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        return allRatings / user.getRating().size();
    }

    public User blockUntil(String id, LocalDateTime bannedUntil) {
        User user = findById(id);
        user.setBannedUntil(bannedUntil);
        return userRepository.save(user);
    }

    public User banUser(String id) {
        User user = findById(id);
        user.setBanned(true);
        return userRepository.save(user);
    }

    public User updateRole(String id) {
        User user = findById(id);
        user.setRole(updateRole(user.getRole()));
         return userRepository.save(user);
    }

    public Set<Role> updateRole(Set<Role> roles) {
        if (roles.contains(Role.ADMIN)) {
            roles.remove(Role.ADMIN);
        } else {
            roles.add(Role.ADMIN);
        }
        return roles;
    }
}