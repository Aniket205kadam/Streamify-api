package com.streamify.story;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowingsStoryResponse {
    private String id;
    private String username;
    private boolean allStoriesSeen;
}
