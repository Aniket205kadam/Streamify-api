package com.streamify.story;

import com.streamify.user.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StoryViewDto {
    private String id;
    private String storyId;
    private UserDto viewer;
    private LocalDateTime viewedAt;
}
