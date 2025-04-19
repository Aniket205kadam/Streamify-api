package com.streamify.aistudio.chat;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/chat")
@Tag(name = "BotChat")
public class BotChatController {
    private final BotChatService service;

    public BotChatController(BotChatService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> createChatWithBot(
            @RequestParam("bot_id") String botId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.createChat(botId, connectedUser));
    }

    @GetMapping
    public ResponseEntity<List<BotChatResponse>> getChats(Authentication connectedUser) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.getChatsByUser(connectedUser));
    }

    @GetMapping("/a/{chat-id}")
    public ResponseEntity<List<BotMessageResponse>> getAllMessage(
            @PathVariable("chat-id") String chatId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.findChatMessages(chatId, connectedUser));
    }
}
