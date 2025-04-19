package com.streamify.aistudio.chat;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bot/messages")
@Tag(name = "BotMessage")
public class BotMessageController {
    private final BotMessageService service;

    public BotMessageController(BotMessageService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BotMessageResponse> saveMessage(
            @RequestBody ConversationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.saveMessage(request));
    }
}
