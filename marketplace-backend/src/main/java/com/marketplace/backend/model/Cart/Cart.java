package com.marketplace.backend.model.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "carts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private String id;
    @Indexed(unique = true)
    private String userId;
    private List<String> products;
}
