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
            if (messages.getFirst().getType() != MessageType.TEXT) {
                return "Attachment";
            }
            return this.messages.getFirst().getContent();
        }
        return null;
    }

    @Transient
    public LocalDateTime getLastMessageTime() {
        if (this.messages != null && !this.messages.isEmpty()) {
            return (
                    this.messages.getFirst().getLastModifiedDate() != null
                        ? this.messages.getFirst().getLastModifiedDate()
                        : this.messages.getFirst().getCreatedDate()
            );
        }
        return null;
    }
}
