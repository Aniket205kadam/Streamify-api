package com.streamify.message;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {
    private String content;
    // private String senderId;
    // private String receiverId;
    private String senderUsername;
    private String receiverUsername;
    private MessageType type;
    private String chatId;
}
