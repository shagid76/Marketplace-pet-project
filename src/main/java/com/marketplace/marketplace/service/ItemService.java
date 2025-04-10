package com.marketplace.marketplace.service;

import com.marketplace.marketplace.model.CreateItemRequest;
import com.marketplace.marketplace.model.Item;
import com.marketplace.marketplace.model.ItemCategory;
import com.marketplace.marketplace.model.UpdateItemRequest;
import com.marketplace.marketplace.repository.ItemRepository;
import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public Item createItem(CreateItemRequest createItemRequest){
        Item item = new Item();
        item.setName(createItemRequest.getName());
        item.setPrice(createItemRequest.getPrice());
        item.setListedDate(LocalDate.now());
        item.setService(createItemRequest.isService());
        item.setCategory(createItemRequest.getCategory());
        item.setSolded(false);
        return itemRepository.save(item);
    }

    public Item soldedItem(String id){
        Item item = findById(id);
        item.setSolded(true);
        return itemRepository.save(item);
    }

    public Item updateItem(UpdateItemRequest updateItemRequest, String id){
        Item item = findById(id);
        item.setName(updateItemRequest.getName());
        item.setPrice(updateItemRequest.getPrice());
        item.setCategory(updateItemRequest.getCategory());
        item.setService(updateItemRequest.isService());
        return itemRepository.save(item);
    }
}