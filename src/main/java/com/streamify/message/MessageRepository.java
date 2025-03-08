package com.streamify.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Modifying
    @Transactional
    @Query("""
            UPDATE Message message
            SET message.state = :newState
            WHERE message.chat.id = :chatId
            """)
    void setMessageToSeenByChatId(@Param("chatId") String chatId, @Param("newState") MessageState newState);
}
