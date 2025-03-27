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
import com.streamify.story.StoryView;
import com.streamify.story.StoryViewDto;
import com.streamify.user.User;
import com.streamify.user.UserDto;
import com.streamify.user.UserRepository;
import com.streamify.user.UserResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

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
                .user(toUserDto(storyReply.getUser()))
                .storyId(storyReply.getStory().getId())
                .createdAt(storyReply.getCreatedAt())
                .build();
    }

    private String getImageType(String fileName) {
        if (fileName == null || fileName.isEmpty())
            throw new RuntimeException("Filename is needed to find the file extensions!");
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public UserDto toUserDto(User user) {
        String avtar;
        if (user.getProfilePictureUrl() == null) {
            avtar = "data:image/" + "png" + ";base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation("D:\\Spring Boot Project\\streamify\\profile-assets\\common-avatar\\no-profile-image.png"));
        } else {
            avtar = "data:image/" + getImageType(user.getProfilePictureUrl()) + ";base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation(user.getProfilePictureUrl()));
        }
        System.out.println("String image: " + avtar);

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .followerCount(user.getFollowerCount())
                .avtarUrl(null) //todo -> this is deprecated remove after another integration
                .avtar(avtar)
                .build();
    }

    public UserDto toSearchedUser(User user, User connectedUser) {
        byte[] content = FileUtils
                .readFileFromLocation(
                        user.getProfilePictureUrl() == null
                                ? "D:\\Spring Boot Project\\streamify\\profile-assets\\common-avatar\\no-profile-image.png"
                                : user.getProfilePictureUrl()
                );
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .avtarUrl(user.getProfilePictureUrl())
                .avtar(Base64.getEncoder().encodeToString(content))
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
        String lastMessagedUsername = null;
        if (!(chat.getLastMessageSender() == null)) {
            lastMessagedUsername = userRepository.findById(chat.getLastMessageSender()).get().getUsername();
        }
        return ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getChatName(senderId))
                .username(chat.getChatUsername(senderId))
                .unreadCount(chat.getUnreadMessages(senderId))
                .lastMessage(chat.getLastMessage())
                .lastMessagedUsername(lastMessagedUsername)
                .lastMessageTime(chat.getLastMessageTime())
                //.isRecipientOnline(chat.getRecipient().isUserOnline())
                .isRecipientOnline(false)
                .senderId(chat.getSender().getId())
                .receiverId(chat.getRecipient().getId())
                .build();
    }

    private String getUsernameById(String userId) {
        return userRepository.findById(userId).get().getUsername();
    }

    private String getMediaType(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            return connection.getContentType();
        } catch (IOException e) {
            return "unknown";
        }
    }

    public MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageType(message.getType())
                .state(message.getState())
                .mediaBase64(message.getMediaFilePath() != null
                        ? Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation(message.getMediaFilePath()))
                        : null
                )
                .mediaType(message.getMediaFilePath() != null ? getMediaType(message.getMediaFilePath()) : null)
                .createdAt(message.getCreatedDate())
                .receiverId(message.getReceiverId())
                .receiverUsername(getUsernameById(message.getReceiverId()))
                .senderId(message.getSenderId())
                .senderUsername(getUsernameById(message.getSenderId()))
                .build();
    }

    public StoryViewDto toStoryViewDto(StoryView storyView) {
        return StoryViewDto.builder()
                .id(storyView.getId())
                .storyId(storyView.getStory().getId())
                .viewer(toUserDto(storyView.getViewer()))
                .viewedAt(storyView.getViewedAt())
                .build();
    }
}
