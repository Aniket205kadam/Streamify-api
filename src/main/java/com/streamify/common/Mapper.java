package com.streamify.common;

import com.streamify.Storage.FileUtils;
import com.streamify.chat.Chat;
import com.streamify.chat.ChatResponse;
import com.streamify.comment.Comment;
import com.streamify.comment.CommentResponse;
import com.streamify.message.Message;
import com.streamify.message.MessageResponse;
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
                .user(toUserDto(comment.getUser()))
                .likeCount(comment.getLikeCount())
                .replies(comment.getReplies().size())
                .build();
    }

    public CommentResponse toCommentRelyResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(toUserDto(comment.getUser()))
                .likeCount(comment.getLikeCount())
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
                .fullName(user.getFullName())
                .followerCount(user.getFollowerCount())
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

    public ChatResponse toChatResponse(Chat chat, String senderId) {
        return ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getChatName(senderId))
                .unreadCount(chat.getUnreadMessages(senderId))
                .lastMessage(chat.getLastMessage())
                .lastMessageTime(chat.getLastMessageTime())
                //.isRecipientOnline(chat.getRecipient().isUserOnline())
                .isRecipientOnline(false)
                .senderId(chat.getSender().getId())
                .receiverId(chat.getRecipient().getId())
                .build();
    }

    public MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageType(message.getType())
                .state(message.getState())
                .media(FileUtils.readFileFromLocation(message.getMediaFilePath()))
                .createdAt(message.getCreatedDate())
                .receiverId(message.getReceiverId())
                .senderId(message.getSenderId())
                .build();
    }
}
