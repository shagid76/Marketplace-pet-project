package com.marketplace.marketplace.service;

import com.marketplace.marketplace.model.Item;
import com.marketplace.marketplace.model.ItemCategory;
import com.marketplace.marketplace.repository.ItemRepository;
import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public List<Item> findAll(){
        return itemRepository.findAll();
    }

    public Item findById(String id){
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item with id: "  + id + "not found"));
    }

    public List<Item> findByCategory(ItemCategory category){
        return itemRepository.findByCategory(category);
    }
}