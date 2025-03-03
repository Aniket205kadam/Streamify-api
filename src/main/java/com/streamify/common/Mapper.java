package com.streamify.common;

import com.streamify.comment.Comment;
import com.streamify.comment.CommentResponse;
import com.streamify.story.StoryReply;
import com.streamify.story.StoryReplyResponse;
import com.streamify.user.User;
import com.streamify.user.UserDto;
import com.streamify.user.UserRepository;
import com.streamify.user.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class Mapper {
    private final UserRepository userRepository;

    public Mapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

    public UserDto toSearchedUser(User user, User connectedUser) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .avtarUrl(user.getProfilePictureUrl())
                .followerCount(user.getFollowingCount())
                .isFollowedByCurrentUser(/*connectedUser
                        .getFollowing()
                        .stream()
                        .anyMatch(followUser ->
                                followUser.getId().equals(user.getId())
                        )*/
                        userRepository.isFollowing(connectedUser.getId(), user.getUsername())
                )
                .build();
    }

    public UserResponse toUserResponse(User request) {
        return UserResponse.builder()
                .id(request.getId())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .bio(request.getBio())
                .profilePictureUrl(request.getProfilePictureUrl())
                .website(request.getWebsite())
                .gender(request.getGender())
                .followerCount(request.getFollowerCount())
                .followingCount(request.getFollowingCount())
                .postsCount(request.getPostsCount())
                .build();
    }
}
