package com.streamify.post;

import com.streamify.user.User;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class PostMapper {
    public PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .visibility(post.getVisibility())
                .isArchived(post.isArchived())
                .location(post.getLocation())
                .isReel(post.isReel())
                .collaborators(
                        post
                                .getCollaborators()
                                .stream()
                                .map(User::getUsername)
                                .collect(Collectors.toSet())
                )
                .hideLikesAndViewCounts(post.isHideLikesAndViewCounts())
                .allowComments(post.isAllowComments())
                .postMedia(post.getPostMedia())
                .build();
    }
}
