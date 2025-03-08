package com.streamify.chat;

import com.streamify.message.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("chats")
@Tag(name = "Chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<StringResponse> createChat(
            @RequestParam("sender_id") String senderId,
            @RequestParam("receiver_id") String receiverId
    ) {
        final String chatId = chatService.createChat(senderId, receiverId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StringResponse.builder()
                        .response(chatId)
                        .build()
                );
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getChatsByReceiver(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(chatService.getChatsByReceiver(connectedUser));
    }

    @GetMapping("/chat/{chat-id}")
    public ResponseEntity<List<MessageResponse>> getAllMessage(
            @PathVariable("chat-id") String chatId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(chatService.findChatMessages(chatId, connectedUser));
    }
}
