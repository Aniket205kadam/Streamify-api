package com.streamify.notification;

import com.streamify.common.Mapper;
import com.streamify.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final UserNotificationRepository repository;
    private final Mapper mapper;

    public NotificationService(SimpMessagingTemplate messagingTemplate, UserNotificationRepository repository, Mapper mapper) {
        this.messagingTemplate = messagingTemplate;
        this.repository = repository;
        this.mapper = mapper;
    }

    public void sendNotification(String userId, Notification notification) {
        log.info("Sending WS notification to {} with payload {}", userId, notification);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/chat",
                notification
        );
    }

    public List<UserNotificationResponse> getUnseenNotification(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        List<UserNotification> notifications = repository.findAllUnseenNotification(user.getId());
        return notifications
                .stream()
                .map(notification -> {
                    try {
                        return mapper.toUserNotificationResponse(notification);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
}
