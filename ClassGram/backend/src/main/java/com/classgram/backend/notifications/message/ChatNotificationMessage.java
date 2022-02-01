package com.classgram.backend.notifications.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.classgram.backend.model.ChatConversation;
import com.classgram.backend.notifications.NotificationType;
import lombok.Getter;

@Getter
public class ChatNotificationMessage extends NotificationMessage {
    @JsonIgnoreProperties("users")
    private final ChatConversation chatConversation;

    public ChatNotificationMessage(ChatConversation chatConversation) {
        super(NotificationType.CHAT_MESSAGE);
        this.chatConversation = chatConversation;
    }
}
