package com.streamify.aistudio.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotChatRepository extends JpaRepository<BotChat, String> {

    @Query("""
            SELECT chat
            FROM BotChat chat
            WHERE chat.user.id = :userId
            """)
    List<BotChat> findChatByUser(@Param("userId") String userId);
}
