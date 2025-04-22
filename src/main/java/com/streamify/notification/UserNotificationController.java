package com.streamify.notification;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Tag(name = "UserNotification")
public class UserNotificationController {
    private final NotificationService service;

    public UserNotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/unseen")
    public ResponseEntity<List<UserNotificationResponse>> getUnseenNotification(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.getUnseenNotification(connectedUser));
    }

    @GetMapping("/n/seen")
    public ResponseEntity<List<UserNotificationResponse>> getSeenNotification(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.getSeenNotification(connectedUser));
    }

    @PatchMapping("/seen")
    public void seenNotification(
            Authentication connectedUser
    ) {
        service.seenNotification(connectedUser);
    }

    @GetMapping("/is-unseens")
    public ResponseEntity<Boolean> isUnseenNotificationPresent(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.isUnseenNotificationPresent(connectedUser));
    }
}
