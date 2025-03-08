package com.streamify.message;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("messages")
@Tag(name = "Message")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveMessage(
            @RequestBody MessageRequest request
    ) {
        messageService.saveMessage(request);
    }

    @PostMapping(value = "/upload-media", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadMedia(
            @RequestParam("chat-id") String chatId,
            @Parameter()
            @RequestParam("file") MultipartFile file,
            Authentication connectedUser
    ) {
        messageService.uploadMediaMessage(chatId, file, connectedUser);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setMessageToSeen(
            @RequestParam("chat-id") String chatId,
            Authentication connectedUser
    ) {
        messageService.setMessageToSeen(chatId, connectedUser);
    }
}
