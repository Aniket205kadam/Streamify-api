package com.streamify.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.streamify.comment.Comment;
import com.streamify.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 2200)
    private String caption;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private PostVisibility visibility;

    @Column(nullable = false)
    private boolean isArchived;
    private String location;
    private boolean isReel;

    // todo -> likes impl

    @ManyToMany
    @JoinTable(
            name = "post_collaborators",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> collaborators = new LinkedHashSet<>();

    @Column(nullable = false)
    private boolean hideLikesAndViewCounts;
    private boolean allowComments;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true) // todo -> make it LAZY
    private List<PostMedia> postMedia;
}
