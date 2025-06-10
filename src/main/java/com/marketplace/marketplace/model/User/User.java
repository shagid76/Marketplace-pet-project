package com.marketplace.marketplace.model.User;

import com.marketplace.marketplace.model.Item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true)
    private String email;
    private String password;
    private LocalDate accountCreationDate;
    private String address;
    private Set<Role> role = new HashSet<>();
    private List<Double> rating;
    private List<Item> items;
    private List<Item> favouriteItems;
    private List<Item> basket;
    private LocalDateTime bannedUntil;
    private boolean isBanned;
}