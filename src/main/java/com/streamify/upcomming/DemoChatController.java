package com.streamify.upcomming;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DemoChatController {

    private List<String> messages = new ArrayList<>();

    @MessageMapping("/chat.sendDemoMessage")
    @SendTo("/user/demoMessages")
    public String sendMessage(@Payload String message,
                              Principal principal) {
        String formatted = principal.getName() + ": " + message;
        messages.add(formatted);
        return formatted;
    }

    @GetMapping("/demo/messages")
    public ResponseEntity<List<String>> getMessages() {
        return ResponseEntity.ok(messages);
    }
}
