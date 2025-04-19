package com.streamify.aistudio.chat;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotMessageResponse {
    private Long id;
    private String content;
    private String userId;
    private String botId;
    private boolean isBotMessage;
    private LocalDateTime createdAt;
}
