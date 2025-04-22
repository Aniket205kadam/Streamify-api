package com.streamify.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {
    @Query("""
            SELECT notification
            FROM UserNotification notification
            WHERE notification.unseen = true
            AND notification.receiver.id = :userId
            ORDER BY notification.createdAt DESC
            """)
    List<UserNotification> findAllUnseenNotification(@Param("userId") String userId);

    @Query("""
            SELECT notification
            FROM UserNotification notification
            WHERE notification.unseen = false
            AND notification.receiver.id = :userId
            ORDER BY notification.createdAt DESC
            """)
    List<UserNotification> findSeenNotification(@Param("userId") String userId);
}
