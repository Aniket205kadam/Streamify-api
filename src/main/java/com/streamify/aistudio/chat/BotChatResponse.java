package com.streamify.aistudio.chat;

import com.streamify.aistudio.bot.BotResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotChatResponse {
    private String id;
    private String chatName;
    private BotResponse bot;
}
