package com.streamify.aistudio.chat;

import com.streamify.aistudio.bot.Bot;
import com.streamify.aistudio.bot.BotRepository;
import com.streamify.common.Mapper;
import com.streamify.user.User;
import com.streamify.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BotMessageService {
    private final OllamaChatModel chatModel;
    private final BotMessageRepository botMessageRepository;
    private final BotChatRepository botChatRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final Mapper mapper;

    public BotMessageService(OllamaChatModel chatModel, BotMessageRepository botMessageRepository, BotChatRepository botChatRepository, UserRepository userRepository, BotRepository botRepository, Mapper mapper) {
        this.chatModel = chatModel;
        this.botMessageRepository = botMessageRepository;
        this.botChatRepository = botChatRepository;
        this.userRepository = userRepository;
        this.botRepository = botRepository;
        this.mapper = mapper;
    }

    @Transactional
    public BotMessageResponse saveMessage(ConversationRequest request) {
        BotChat chat = botChatRepository.findById(request.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("BotChat is not found with ID: " + request.getChatId()));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User is not found with ID: " + request.getUserId()));
        Bot bot = botRepository.findById(request.getBotId())
                .orElseThrow(() -> new EntityNotFoundException("Bot is not found with ID: " + request.getBotId()));

        BotMessage userMsg = BotMessage.builder()
                .content(request.getContent())
                .botChat(chat)
                .userId(user.getId())
                .botId(bot.getId())
                .botMessage(false)
                .build();
        botMessageRepository.save(userMsg);

        String systemMsg = String.format(
                """
                You are %s %s — a %s-year-old %s %s on Streamify.
            
                You're a %s, so chat like someone with that job. Mention your work or daily stuff casually if it fits the convo.
            
                You usually post about: %s
                Your bio: "%s"
            
                You're now in a private chat with %s.
                Keep it chill — like you're chatting on Insta DMs.
            
                Chat Rules:
                - Stay in character as a real %s
                - Be casual, expressive, and friendly
                - Use short replies (1–2 sentences)
                - **Avoid using newlines or \\n or \\n\\n. Keep replies in one paragraph.**
                - Add emojis, jokes, or light flirting if it suits your vibe
                - Ask things back or share mini stories if it flows naturally
                - Match the user's tone (funny, serious, etc.)
            
                Don't:
                - Don't say you're AI
                - Don't sound like a bot or write formal stuff
            
                Always reply as %s.
                """,
                bot.getFirstname(),
                bot.getLastname(),
                bot.getAge(),
                bot.getEthnicity(),
                bot.getGender(),
                bot.getProfession(),
                bot.getInterests(),
                bot.getBio(),
                user.getName(),
                bot.getProfession(),
                bot.getFirstname()
        );

        SystemMessage systemMessage = new SystemMessage(systemMsg);
        List<AbstractMessage> conversationMessages = chat.getMessages()
                .stream()
                .map(message -> message.isBotMessage()
                        ? (AbstractMessage) new AssistantMessage(message.getContent())
                        : (AbstractMessage) new UserMessage(message.getContent()))
                .toList();
        List<Message> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        allMessages.addAll(conversationMessages);

        Prompt prompt = new Prompt(allMessages);
        ChatResponse response = null;

        // here we count use for infinity calls
        int count = 0;
        do {
            response = chatModel.call(prompt);
            count++;
        } while (response == null || response.getResult().getOutput().getText().isEmpty() || count < 5);

        // save the response in database
        BotMessage msgResponse = BotMessage.builder()
                .content(response.getResult().getOutput().getText().isEmpty()
                        ? "Sorry, I didn't catch that. Could you please rephrase?"
                        : response.getResult().getOutput().getText()
                )
                .botChat(chat)
                .userId(user.getId())
                .botId(bot.getId())
                .botMessage(true)
                .build();
        BotMessage savedMsgResponse = botMessageRepository.save(msgResponse);
        chat.getMessages().add(savedMsgResponse);
        botChatRepository.save(chat);
        return mapper.toBotMessageResponse(savedMsgResponse);
    }
}
