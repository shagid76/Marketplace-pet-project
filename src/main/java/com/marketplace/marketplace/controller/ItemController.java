package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.model.Item;
import com.marketplace.marketplace.model.ItemCategory;
import com.marketplace.marketplace.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items")
    public List<Item> findAll() {
        return itemService.findAll();
    }

    @GetMapping("/items/{id}")
    public Item findById(@PathVariable("id") String id) {
        return itemService.findById(id);
    }

    @GetMapping("/items/{category}")
    public List<Item> findByCategory(@PathVariable("category") ItemCategory category) {
        return itemService.findByCategory(category);
    }
}