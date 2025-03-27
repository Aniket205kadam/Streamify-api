package com.streamify.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private String content;
    private MessageType messageType;
    private MessageState state;
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private LocalDateTime createdAt;
    //private byte[] media;
    private String mediaBase64;
    private String mediaType;
}
