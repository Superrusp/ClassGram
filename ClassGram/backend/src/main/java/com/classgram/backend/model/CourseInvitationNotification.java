package com.classgram.backend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourseInvitationNotification extends Notification {
    public CourseInvitationNotification(String message, User user, Course course) {
        super(message, user, course, NotificationType.INVITED_TO_COURSE);
    }
}
