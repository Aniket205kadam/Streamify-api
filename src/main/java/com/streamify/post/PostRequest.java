package com.streamify.post;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PostRequest {
    private String id;
    private String caption;
    private PostVisibility visibility;
    private boolean isArchived;
    private String location;
    private Set<String> collaborators;
    private boolean hideLikesAndViewCounts;
    private boolean allowComments;
}

/*
* if id exist means update the Post
*/
