package com.streamify.comment;

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
    private String userId;
    private int likes;
    private int replies;
}
