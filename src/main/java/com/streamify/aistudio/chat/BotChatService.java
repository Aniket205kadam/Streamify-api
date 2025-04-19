package com.streamify.aistudio.chat;

import com.streamify.aistudio.bot.Bot;
import com.streamify.aistudio.bot.BotRepository;
import com.streamify.aistudio.bot.BotType;
import com.streamify.common.Mapper;
import com.streamify.exception.OperationNotPermittedException;
import com.streamify.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BotChatService {
    private final BotRepository botRepository;
    private final BotChatRepository botChatRepository;
    private final Mapper mapper;

    public BotChatService(BotRepository botRepository, BotChatRepository botChatRespository, Mapper mapper) {
        this.botRepository = botRepository;
        this.botChatRepository = botChatRespository;
        this.mapper = mapper;
    }

    @Transactional
    public String createChat(String botId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new EntityNotFoundException("Bot is not found with ID: " + botId));
        // if the bot is private then only creator can chat
        if (!(bot.getType() == BotType.PRIVATE && bot.getCreator().getId().equals(user.getId()))) {
            throw new OperationNotPermittedException("You are not creator of this bot, so you don't have to permission to access!");
        }
        BotChat chat = BotChat.builder()
                .user(user)
                .bot(bot)
                .build();
        return botChatRepository.save(chat).getId();
    }

    public List<BotChatResponse> getChatsByUser(Authentication connectedUser) {
        final String userId = ((User) connectedUser.getPrincipal()).getId();
        return botChatRepository.findChatByUser(userId)
                .stream()
                .map(mapper::toBotChatResponse)
                .toList();
    }

    public List<BotMessageResponse> findChatMessages(String chatId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        BotChat chat = botChatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("BotChat is not found ID: " + chatId));
        // only user can read the messages
        if (!chat.getUser().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are not user in this chat, so you can't read the chat messages!");
        }
        return chat.getMessages()
                .stream()
                .sorted(Comparator.comparing(BotMessage::getCreatedDate))
                .map(mapper::toBotMessageResponse)
                .toList();
    }
}
