package com.streamify.post;

import com.streamify.comment.Comment;
import com.streamify.user.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private String id;
    private String caption;
    private LocalDateTime createdAt;
    private PostVisibility visibility;
    private boolean isArchived;
    private String location;
    private boolean isReel;
    private Set<String> collaborators;
    private boolean hideLikesAndViewCounts;
    private boolean allowComments;
    private List<PostMedia> postMedia;
}
