package com.streamify.aistudio.chat;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationRequest {
    private String content;
    private String userId;
    private String botId;
    private String chatId;
}
