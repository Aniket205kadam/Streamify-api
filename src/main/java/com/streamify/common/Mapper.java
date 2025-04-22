package com.streamify.common;

import com.streamify.Storage.FileUtils;
import com.streamify.aistudio.bot.Bot;
import com.streamify.aistudio.bot.BotResponse;
import com.streamify.aistudio.chat.BotChat;
import com.streamify.aistudio.chat.BotChatResponse;
import com.streamify.aistudio.chat.BotMessage;
import com.streamify.aistudio.chat.BotMessageResponse;
import com.streamify.chat.Chat;
import com.streamify.chat.ChatResponse;
import com.streamify.comment.Comment;
import com.streamify.comment.CommentResponse;
import com.streamify.ffmpeg.FfmpegService;
import com.streamify.message.Message;
import com.streamify.message.MessageResponse;
import com.streamify.notification.UserNotification;
import com.streamify.notification.UserNotificationResponse;
import com.streamify.notification.UserNotificationType;
import com.streamify.post.Post;
import com.streamify.post.PostRepository;
import com.streamify.story.StoryReply;
import com.streamify.story.StoryReplyResponse;
import com.streamify.story.StoryView;
import com.streamify.story.StoryViewDto;
import com.streamify.user.User;
import com.streamify.user.UserDto;
import com.streamify.user.UserRepository;
import com.streamify.user.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class Mapper {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FfmpegService ffmpegService;

    @Value("${application.file.upload.content-base-url.thumbnail}")
    private String thumbnailUrl;

    public Mapper(UserRepository userRepository, PostRepository postRepository, FfmpegService ffmpegService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.ffmpegService = ffmpegService;
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
            avtar = "data:image/" + "png" + ";base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation("D:\\Spring Boot Project\\streamify\\profile-assets\\common-avatar\\no-profile-image.jpg"));
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
        byte[] content = FileUtils
                .readFileFromLocation(
                        request.getProfilePictureUrl() == null
                                ? "D:\\Spring Boot Project\\streamify\\profile-assets\\common-avatar\\no-profile-image.jpg"
                                : request.getProfilePictureUrl()
                );
        String extension = "";
        if (request.getProfilePictureUrl() != null) {
            extension = request.getProfilePictureUrl().substring(request.getProfilePictureUrl().lastIndexOf("."));
        } else {
            extension = "jpg";
        }
        return UserResponse.builder()
                .id(request.getId())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .bio(request.getBio())
                .profilePictureUrl(request.getProfilePictureUrl())
                .website(request.getWebsite())
                .gender(request.getGender())
                .avtar("data:image/" + extension + ";base64," + Base64.getEncoder().encodeToString(content))
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

    public UserNotificationResponse toUserNotificationResponse(UserNotification userNotification) throws IOException, InterruptedException {
        // this for only the posts
        String notificationImage = null;
        if (userNotification.getType() == UserNotificationType.LIKE) {
            if (userNotification.getPostId() != null) {
                Post post = postRepository.findById(userNotification.getPostId())
                        .orElseThrow(() -> new EntityNotFoundException("Post is not found with ID: " + userNotification.getPostId()));
                if (post.getPostMedia().getFirst().getType().startsWith("video/")) {
                    Path targetPath = Paths.get(thumbnailUrl, post.getPostMedia().getFirst().getId() + ".jpg");

                    if (Files.exists(targetPath)) {
                        notificationImage = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation(targetPath.toString()));
                    } else {
                        Files.createDirectories(Paths.get(thumbnailUrl));
                        ffmpegService.generateThumbnail(
                                post.getPostMedia().getFirst().getMediaUrl(),
                                targetPath.toString()
                        );
                        System.out.println("Filename: " + targetPath.toString());
                        notificationImage = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation(targetPath.toString()));
                    }
                } else {
                    notificationImage = "data:image/" + getImageType(post.getPostMedia().getFirst().getMediaUrl()) + ";base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation(post.getPostMedia().getFirst().getMediaUrl()));
                }
            } else {
                throw new RuntimeException("Post is needed for notification!");
            }
        }
        return UserNotificationResponse.builder()
                .id(userNotification.getId())
                .sender(toUserDto(userNotification.getSender()))
                .receiver(toUserDto(userNotification.getReceiver()))
                .type(userNotification.getType())
                .notificationImage(notificationImage)
                .unseen(userNotification.isUnseen())
                .createAt(userNotification.getCreatedAt())
                .build();
    }

    public BotResponse toBotResponse(Bot bot, String avtar) {
        return BotResponse.builder()
                .id(bot.getId())
                .name(bot.getFirstname() + " " + bot.getLastname())
                .avtar("data:image/" + getImageType(avtar) + ";base64," + Base64.getEncoder().encodeToString(FileUtils.readFileFromLocation(avtar)))
                .build();
    }

    public BotChatResponse toBotChatResponse(BotChat chat) {
        return BotChatResponse.builder()
                .id(chat.getId())
                .chatName(chat.getChatName())
                .bot(toBotResponse(chat.getBot(), chat.getBot().getAvtar()))
                .build();
    }

    public BotMessageResponse toBotMessageResponse(BotMessage message) {
        return BotMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .userId(message.getUserId())
                .botId(message.getBotId())
                .isBotMessage(message.isBotMessage())
                .createdAt(message.getCreatedDate())
                .build();
    }

}
