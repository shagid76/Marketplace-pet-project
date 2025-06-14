package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.model.Item.Item;
import com.marketplace.marketplace.model.Item.ItemCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends MongoRepository<Item, String> {
    List<Item> findByCategory(ItemCategory category);
}