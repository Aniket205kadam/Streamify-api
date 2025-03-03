package com.streamify.post;

import com.streamify.comment.Comment;
import com.streamify.user.User;
import com.streamify.user.UserDto;
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
    private UserDto user;
    private boolean isArchived;
    private String location;
    private boolean isReel;
    private Set<String> collaborators;
    private int likeCount;
    private int commentCount;
    private boolean hideLikesAndViewCounts;
    private boolean allowComments;
    private List<PostMedia> postMedia;
}
