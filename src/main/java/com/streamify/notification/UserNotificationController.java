package com.streamify.notification;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("notification")
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
}
