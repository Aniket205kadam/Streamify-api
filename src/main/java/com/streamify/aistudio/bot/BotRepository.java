package com.streamify.aistudio.bot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepository extends JpaRepository<Bot, String> {

    @Query("""
            SELECT bot
            FROM Bot bot
            WHERE bot.creator.id = :userId
            """)
    List<Bot> findMyBot(@Param("userId") String userId);
}
