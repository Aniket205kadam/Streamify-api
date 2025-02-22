package com.streamify.story;

import com.streamify.user.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class StoryReplyResponse {
    private String id;
    private String content;
    private String storyId;
    private UserDto user;
    private LocalDateTime createdAt;
}
