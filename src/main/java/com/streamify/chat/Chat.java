package com.streamify.chat;

import com.streamify.message.Message;
import com.streamify.message.MessageState;
import com.streamify.message.MessageType;
import com.streamify.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chats")
@EntityListeners(AuditingEntityListener.class)
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
    private List<Message> messages;

    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", insertable = false)
    private LocalDateTime lastModifiedDate;

    @Transient
    public String getChatName(String senderId) {
        if (recipient.getId().equals(senderId)) {
            return sender.getFullName();
        }
        return recipient.getFullName();
    }

    @Transient
    public String getChatUsername(String senderId) {
        if (recipient.getId().equals(senderId)) {
            return sender.getUsername();
        }
        return recipient.getUsername();
    }

    @Transient
    public String getTargetChatName(String senderId) {
        if (sender.getId().equals(senderId)) {
            return sender.getFullName();
        }
        return  recipient.getFullName();
    }

    @Transient
    public long getUnreadMessages(String senderId) {
        return this.messages
                .stream()
                .filter(msg -> msg.getReceiverId().equals(senderId))
                .filter(msg -> MessageState.SENT == msg.getState())
                .count();
    }

    @Transient
    public String getLastMessage() {
        if (this.messages != null && !this.messages.isEmpty()) {
            Message lastMessage = this.messages.stream()
                    .max((m1, m2) -> m1.getCreatedDate().compareTo(m2.getCreatedDate()))
                    .orElse(null);
            if (lastMessage.getType() == MessageType.IMAGE) {
                return "Image";
            } else if (lastMessage.getType() == MessageType.VIDEO){
                return "VIDEO";
            } else if (lastMessage.getType() == MessageType.AUDIO){
                return "AUDIO";
            } else if (lastMessage.getType() == MessageType.GIF){
                return "GIF";
            }
            return lastMessage.getContent();
        }
        return null;
    }

    @Transient
    public String getLastMessageSender() {
        if (this.messages == null || this.messages.isEmpty()) {
            return null;
        }

        return this.messages.stream()
                .max(Comparator.comparing(Message::getCreatedDate))
                .map(Message::getSenderId)
                .orElse(null);
    }


    @Transient
    public LocalDateTime getLastMessageTime() {
        if (this.messages != null && !this.messages.isEmpty()) {
            Message lastMessage = this.messages.stream()
                    .max((m1, m2) -> m1.getCreatedDate().compareTo(m2.getCreatedDate()))
                    .orElse(null);
            return (
                    lastMessage.getLastModifiedDate() != null
                        ? this.messages.getLast().getLastModifiedDate()
                        : this.messages.getLast().getCreatedDate()
            );
        }
        return null;
    }
}
