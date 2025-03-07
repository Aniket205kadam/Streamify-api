package com.streamify.story;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.streamify.user.User;
import com.streamify.user.UserDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stories")
@EntityListeners(AuditingEntityListener.class)
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = true)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private StoryType type;

    private String caption;
    private boolean isArchived;
    private int likeCount = 0;
    private int ReplyCount = 0;

    @JsonIgnore
    @Embedded
    @ElementCollection(fetch = FetchType.LAZY)
    private Set<UserDto> likedUsers = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoryView> views = new HashSet<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryReply> replies = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime modifiedAt;


    @Column(nullable = false)
    private LocalDateTime expiredAt;
}
