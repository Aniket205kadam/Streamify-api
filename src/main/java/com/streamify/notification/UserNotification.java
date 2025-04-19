package com.streamify.notification;

import com.streamify.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_notification")
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    private User receiver;

    @Enumerated(EnumType.STRING)
    private UserNotificationType type;

    @Column(nullable = true)
    private String postId;

    // here stored the image in Base64 encoding format, if the notification type is like then sender profile, otherwise the post image
    @Column(nullable = false, length = 999999999)
    private String notificationImage;

    // if user not see the notification then true, otherwise false
    private boolean unseen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
