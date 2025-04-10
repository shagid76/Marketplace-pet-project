package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.model.CreateItemRequest;
import com.marketplace.marketplace.model.Item;
import com.marketplace.marketplace.model.ItemCategory;
import com.marketplace.marketplace.model.UpdateItemRequest;
import com.marketplace.marketplace.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/items/put")
    public Item putItem(@RequestBody @Valid CreateItemRequest createItemRequest){
        return itemService.createItem(createItemRequest);
    }

    @PatchMapping("/items/sold")
    public Item soldItem(@RequestBody String id){
        return itemService.soldedItem(id);
    }

    @PatchMapping("/items/update")
    public Item updateItem(@RequestBody @Valid UpdateItemRequest updateItemRequest,
                           @RequestBody String id){
        return itemService.updateItem(updateItemRequest, id);
    }

}