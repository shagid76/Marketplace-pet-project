package com.marketplace.backend.repository;

import com.marketplace.backend.model.Message.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByChatId(String chatId);

    void deleteAllByChatId(String chatId);
}
