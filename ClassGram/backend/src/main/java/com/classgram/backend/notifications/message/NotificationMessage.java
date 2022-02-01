package com.classgram.backend.notifications.message;

import com.classgram.backend.notifications.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class NotificationMessage {
    private final NotificationType type;
}
