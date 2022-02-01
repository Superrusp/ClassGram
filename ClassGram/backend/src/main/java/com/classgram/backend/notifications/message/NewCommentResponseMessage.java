package com.classgram.backend.notifications.message;

import com.classgram.backend.model.Comment;
import com.classgram.backend.model.Course;
import com.classgram.backend.model.Entry;
import com.classgram.backend.model.User;
import com.classgram.backend.notifications.NotificationType;
import lombok.Getter;

@Getter
public class NewCommentResponseMessage extends NotificationMessage{
    private final Comment comment;
    private final User replier;
    private final Course commentCourse;
    private final Entry commentEntry;

    public NewCommentResponseMessage(Comment comment, User replier, Course commentCourse, Entry commentEntry) {
        super(NotificationType.COMMENT_REPLY);
        this.comment = comment;
        this.replier = replier;
        this.commentCourse = commentCourse;
        this.commentEntry = commentEntry;
    }
}
