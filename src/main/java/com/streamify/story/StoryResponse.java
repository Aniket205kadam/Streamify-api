package com.streamify.story;

import com.streamify.user.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
public class StoryResponse {
    private String id;
    private String caption;
    private StoryType type;
    private UserDto user;
    private Set<StoryViewDto> viewer;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime expiredAt;
}
