package com.streamify.aistudio.bot;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/ai")
@Tag(name = "Bot")
public class BotController{
    private final BotService service;

    public BotController(BotService service) {
        this.service = service;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public void createBot(
            @RequestBody @Valid BotRequest request,
            Authentication connectedUser
    ) {
        service.createBot(request, connectedUser);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadBotAvtar(
            @RequestPart(name = "avtar") MultipartFile avtar,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(service.uploadBotAvtar(avtar, connectedUser));
    }

    @GetMapping("/your-bots")
    public ResponseEntity<List<BotResponse>> getMyBots(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.getMyBots(connectedUser));
    }
}
