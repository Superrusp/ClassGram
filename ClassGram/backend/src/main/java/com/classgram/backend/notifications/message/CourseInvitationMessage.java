package com.classgram.backend.notifications.message;

import com.classgram.backend.model.Course;
import com.classgram.backend.notifications.NotificationType;
import lombok.Getter;

@Getter
public class CourseInvitationMessage extends NotificationMessage{

    private final Course course;

    public CourseInvitationMessage(Course course) {
        super(NotificationType.COURSE_INVITATION);
        this.course = course;
    }
}
