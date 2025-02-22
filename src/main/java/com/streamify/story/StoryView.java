package com.streamify.story;

import com.streamify.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "story_views")
@EntityListeners(AuditingEntityListener.class)
public class StoryView {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "story_id")
    private Story story;

    @ManyToOne
    @JoinColumn(name = "viewer_id")
    private User viewer;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime viewedAt;
}
