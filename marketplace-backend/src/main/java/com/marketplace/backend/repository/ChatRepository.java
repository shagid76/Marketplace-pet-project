package com.marketplace.backend.repository;

import com.marketplace.backend.dto.ChatPreviewDto;
import com.marketplace.backend.model.Chat.Chat;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {
    Optional<Chat> findByUser1IdAndUser2Id(String user1Id, String user2Id);
    @Aggregation(pipeline = {
            """
            { $match: {
                $or: [
                    { user1Id: ?0 },
                    { user2Id: ?0 }
                ]
            }}
            """,
            """
            { $lookup: {
                from: "messages",
                localField: "_id",
                foreignField: "chatId",
                as: "allMessages"
            }}
            """,
            """
            { $addFields: {
                sortedMessages: {
                    $sortArray: {
                        input: "$allMessages",
                        sortBy: { createdAt: -1 }
                    }
                }
            }}
            """,
            """
            { $addFields: {
                lastMessageDoc: { $arrayElemAt: ["$sortedMessages", 0] },
                lastMessageTime: { $arrayElemAt: ["$sortedMessages.createdAt", 0] }
            }}
            """,
            """
            { $addFields: {
                lastMessageText: "$lastMessageDoc.text",
                lastMessageSenderId: "$lastMessageDoc.senderId"
            }}
            """,
            """
            { $project: {
                allMessages: 0,
                sortedMessages: 0,
                lastMessageDoc: 0
            }}
            """,
            """
            { $sort: {
                lastMessageTime: -1,
                _id: -1
            }}
            """
    })
    List<ChatPreviewDto> findChatsWithLastMessage(String userId);
}