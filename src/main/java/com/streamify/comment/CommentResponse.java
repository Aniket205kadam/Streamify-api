package com.streamify.comment;

import com.streamify.user.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentResponse {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private UserDto user;
    private Integer likeCount;
    private int replies;
}
