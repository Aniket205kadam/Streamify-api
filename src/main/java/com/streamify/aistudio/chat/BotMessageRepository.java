package com.streamify.aistudio.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotMessageRepository extends JpaRepository<BotMessage, String> {
}
