package com.streamify.notification;

import com.streamify.user.User;
import com.streamify.user.UserDto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationResponse {
    private String id;
    private UserDto sender;
    private UserDto receiver;
    private UserNotificationType type;
    private String notificationImage;
    private boolean unseen;
    private LocalDateTime createAt;
}
