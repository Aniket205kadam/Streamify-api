package com.streamify.message;

import com.streamify.Storage.FileUtils;
import com.streamify.Storage.MediaService;
import com.streamify.chat.Chat;
import com.streamify.chat.ChatRepository;
import com.streamify.notification.Notification;
import com.streamify.notification.NotificationService;
import com.streamify.notification.NotificationType;
import com.streamify.user.User;
import com.streamify.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final NotificationService notificationService;
    private final MediaService mediaService;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, ChatRepository chatRepository, NotificationService notificationService, MediaService mediaService, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.notificationService = notificationService;
        this.mediaService = mediaService;
        this.userRepository = userRepository;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User is not found with username: " + username));
    }

    @Transactional
    public void saveMessage(MessageRequest request) {
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        User sender = findUserByUsername(request.getSenderUsername());
        User receiver = findUserByUsername(request.getReceiverUsername());
        Message message = new Message();
        message.setContent(request.getContent());
        message.setChat(chat);
        message.setSenderId(sender.getId());
        message.setReceiverId(receiver.getId());
        message.setType(request.getType());
        message.setState(MessageState.SENT);
        messageRepository.save(message);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(request.getType())
                .content(request.getContent())
                .senderId(sender.getId())
                .receiverId(receiver.getId())
                .type(NotificationType.MESSAGE)
                .chatName(chat.getTargetChatName(message.getSenderId()))
                .build();
        notificationService.sendNotification(receiver.getId(), notification);
    }

    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication connectedUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        String senderId = getSenderId(chat, (User) connectedUser.getPrincipal());
        String receiverId = getReceiverId(chat, (User) connectedUser.getPrincipal());
        final String filePath = mediaService.uploadChatContent(file, senderId);

        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setSenderId(senderId);
        message.setState(MessageState.SENT);
        message.setType(MessageType.IMAGE);
        message.setMediaFilePath(filePath);
        message.setChat(chat);
        messageRepository.save(message);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .type(NotificationType.IMAGE)
                .senderId(senderId)
                .receiverId(receiverId)
                .messageType(MessageType.IMAGE)
                .media(FileUtils.readFileFromLocation(filePath))
                .build();
        notificationService.sendNotification(receiverId, notification);
    }

    @Transactional
    public void setMessageToSeen(String chatId, Authentication connectedUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        final String recipientId = getReceiverId(chat, (User) connectedUser.getPrincipal());
        messageRepository.setMessageToSeenByChatId(chatId, MessageState.SEEN);
        Notification notification = Notification.builder()
                .chatId(chatId)
                .type(NotificationType.SEEN)
                .receiverId(recipientId)
                .senderId(getSenderId(chat, (User) connectedUser.getPrincipal()))
                .build();
        notificationService.sendNotification(recipientId, notification);
    }

    private String getReceiverId(Chat chat, User connectedUser) {
        if (chat.getSender().getId().equals(connectedUser.getId())) {
            return chat.getRecipient().getId();
        }
        return chat.getSender().getId();
    }

    private String getSenderId(Chat chat, User connectedUser) {
        if (chat.getSender().getId().equals(connectedUser.getId())) {
            return chat.getSender().getId();
        }
        return chat.getRecipient().getId();
    }
}
