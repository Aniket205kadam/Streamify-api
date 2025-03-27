package com.streamify.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {
    @Query("""
            SELECT chat
            FROM Chat chat
            WHERE recipient.id = :receiverId
            AND sender.id = :senderId
            """)
    Optional<Chat> findChatBySenderAndReceiver(
            @Param("senderId") String senderId,
            @Param("receiverId") String receiverId
    );

    @Query("""
            SELECT chat
            FROM Chat chat
            WHERE sender.id = :userId
            OR recipient.id = :userId
            """)
    List<Chat> findChatBySenderId(@Param("userId") String userId);
}
