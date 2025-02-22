package com.streamify.common;

import com.streamify.comment.Comment;
import com.streamify.comment.CommentResponse;
import com.streamify.story.StoryReply;
import com.streamify.story.StoryReplyResponse;
import com.streamify.user.User;
import com.streamify.user.UserDto;
import org.springframework.stereotype.Service;

@Service
public class Mapper {
    public CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .likes(comment.getLikes())
                .replies(comment.getReplies().size())
                .build();
    }

    public CommentResponse toCommentRelyResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .likes(comment.getLikes())
                .build();
    }

    public StoryReplyResponse toStoryReplyResponse(StoryReply storyReply) {
        return StoryReplyResponse.builder()
                .id(storyReply.getId())
                .content(storyReply.getContent())
                .user(UserDto.builder()
                        .id(storyReply.getUser().getId())
                        .username(storyReply.getUser().getUsername())
                        .avtarUrl(null).build())
                .storyId(storyReply.getStory().getId())
                .createdAt(storyReply.getCreatedAt())
                .build();
    }

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avtarUrl(null)
                .build();
    }
}
