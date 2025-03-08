package com.streamify.chat;

import com.streamify.common.Mapper;
import com.streamify.exception.OperationNotPermittedException;
import com.streamify.message.MessageResponse;
import com.streamify.user.User;
import com.streamify.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository, Mapper mapper) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public String createChat(String senderId, String receiverId) {
        Optional<Chat> existingChat = chatRepository.findChatBySenderAndReceiver(senderId, receiverId);
        if (existingChat.isPresent()) {
            return existingChat.get().getId();
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender is not found with Id: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found with Id: " + receiverId));
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRecipient(receiver);
        Chat savedChat = chatRepository.save(chat);
        return savedChat.getId();
    }

    public List<ChatResponse> getChatsByReceiver(Authentication connectedUser) {
        final String userId = ((User) connectedUser.getPrincipal()).getId();
        return chatRepository.findChatBySenderId(userId)
                .stream()
                .map(chat -> mapper.toChatResponse(chat, userId))
                .toList();
    }

    public List<MessageResponse> findChatMessages(String chatId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat is not found"));
        // only chat sender and receiver can read the chat
        if (!(chat.getSender().getId().equals(user.getId()) ||
                chat.getRecipient().getId().equals(user.getId()))
        ) {
            throw new OperationNotPermittedException("You are not sender or recipient in this chat, so you can't read the chat message!");
        }
        return chat.getMessages()
                .stream()
                .map(mapper::toMessageResponse)
                .toList();
    }
}
