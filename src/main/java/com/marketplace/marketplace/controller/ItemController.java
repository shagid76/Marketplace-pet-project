package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.model.Item.CreateItemRequest;
import com.marketplace.marketplace.model.Item.Item;
import com.marketplace.marketplace.model.Item.ItemCategory;
import com.marketplace.marketplace.model.Item.UpdateItemRequest;
import com.marketplace.marketplace.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping()
    public List<Item> findAll() {
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public Item findById(@PathVariable("id") String id) {
        return itemService.findById(id);
    }

    @GetMapping("/{category}")
    public List<Item> findByCategory(@PathVariable("category") ItemCategory category) {
        return itemService.findByCategory(category);
    }

    @PutMapping("/put")
    public Item putItem(@RequestBody @Valid CreateItemRequest createItemRequest){
        return itemService.createItem(createItemRequest);
    }

    @PatchMapping("/sold")
    public Item soldItem(@RequestBody String id){
        return itemService.soldedItem(id);
    }

    @PatchMapping("/update")
    public Item updateItem(@RequestBody @Valid UpdateItemRequest updateItemRequest,
                           @RequestBody String id){
        return itemService.updateItem(updateItemRequest, id);
    }

    @PatchMapping("/update")
    public Item addItemTo(@RequestBody @Valid UpdateItemRequest updateItemRequest,
                           @RequestBody String id){
        return itemService.updateItem(updateItemRequest, id);
    }

    @PatchMapping("/ban")
    public Item banItem(@RequestBody String id){
        return itemService.banItem(id);
    }
}