package com.classgram.backend.controller;

import com.classgram.backend.annotation.RoleFilter;
import com.classgram.backend.job.UpdateCourseEntryJob;
import com.classgram.backend.model.*;
import com.classgram.backend.notifications.NotificationDispatcher;
import com.classgram.backend.service.*;
import com.classgram.backend.entry.NewEntryCommentResponse;
import com.classgram.backend.security.AuthorizationService;
import com.classgram.backend.security.user.UserComponent;
import com.classgram.backend.struct.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.Objects;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api-comments")
@Slf4j
public final class CommentController extends SecureController{

    private final EntryService entryService;
    private final CommentService commentService;
    private final CourseService courseService;
    private final CourseDetailsService courseDetailsService;
    private final NotificationDispatcher notificationDispatcher;
    private final TelegramChannelService telegramChannelService;

    private final UpdateCourseEntryJob courseEntryJob;

    @Autowired
    public CommentController(UserComponent user, AuthorizationService authorizationService, CourseDetailsService courseDetailsService,
                             EntryService entryService, CommentService commentService, CourseService courseService,
                             NotificationDispatcher notificationDispatcher, TelegramChannelService telegramChannelService, UpdateCourseEntryJob courseEntryJob) {
        super(user, authorizationService);
        this.courseDetailsService = courseDetailsService;
        this.entryService = entryService;
        this.commentService = commentService;
        this.courseService = courseService;
        this.notificationDispatcher = notificationDispatcher;
        this.telegramChannelService = telegramChannelService;
        this.courseEntryJob = courseEntryJob;
    }


    @RoleFilter(role = Role.TEACHER)
    @PostMapping(value = "/comment/delete/{commentId}/{courseId}/{entryId}")
    public ResponseEntity<?> removeComment(@PathVariable long commentId, @PathVariable long courseId, @PathVariable() long entryId) {

        Course course = this.courseService.getFromId(courseId);
        Entry entry = this.entryService.getFromId(entryId);
        ResponseEntity<?> unAuthorized = this.authorize(course);

        if (Objects.isNull(unAuthorized)) {
            ResponseEntity<?> responseEntity;
            Comment comment = this.commentService.getFromId(commentId);

            if (Objects.nonNull(comment) && Objects.nonNull(entry)) {
                Entry response = this.entryService.removeCommentAndChildren(entry, comment);
                responseEntity = ResponseEntity.ok(response);
            } else {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return responseEntity;
        } else {
            return unAuthorized;
        }
    }

    @RequestMapping(value = "/entry/{entryId}/forum/{courseDetailsId}", method = RequestMethod.POST)
    public ResponseEntity<?> newComment(
            @RequestBody Comment comment,
            @PathVariable(value = "entryId") String entryId,
            @PathVariable(value = "courseDetailsId") String courseDetailsId
    ) {

        log.info("CRUD operation: Adding new comment");

        long id_entry = -1;
        long id_courseDetails = -1;
        try {
            id_entry = Long.parseLong(entryId);
            id_courseDetails = Long.parseLong(courseDetailsId);
        } catch (NumberFormatException e) {
            log.error("Entry ID '{}' or CourseDetails ID '{}' are not of type Long", entryId, courseDetailsId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        CourseDetails cd = this.courseDetailsService.getFromId(id_courseDetails);

        ResponseEntity<?> userAuthorized = authorizationService.checkAuthorizationUsers(cd, cd.getCourse().getAttenders());
        if (userAuthorized != null) { // If the user is not an attender of the course
            return userAuthorized;
        } else {

            //Setting the author of the comment
            User userLogged = user.getLoggedUser();
            comment.setUser(userLogged);
            //Setting the date of the comment
            comment.setDate(System.currentTimeMillis());

            //The comment is a root comment
            if (comment.getCommentParent() == null) {
                log.info("Adding new root comment");
                Entry entry = entryService.getFromId(id_entry);
                if (entry != null) {

                    comment = commentService.save(comment);

                    entry.getComments().add(comment);
					/*Saving the modified entry: Cascade relationship between entry and comments
					  will add the new comment to CommentRepository*/
                    Entry savedEntry = entryService.save(entry);

                    TelegramChannel telegramChannel = cd.getCourse().getTelegramChannel();
                    if (telegramChannel != null) {
                        long currentCommentId = comment.getId();

                        Comment lastEntryComment = entry.getComments()
                                .stream()
                                .filter(comment1 -> comment1.getId() != currentCommentId)
                                .max(Comparator.comparingLong(Comment::getDate))
                                .orElse(null);
                        long lastThreadMessageId = lastEntryComment != null ? lastEntryComment.getTelegramThreadMessage().getThreadMessageId() : 0;

                        long discussionGroupId = telegramChannel.getDiscussionGroupId();
                        long threadChannelId = savedEntry.getTelegramThreadChannel().getThreadChannelId();

                        int dateMessageSent = telegramChannelService.sendTextMessageToTelegramChannel(discussionGroupId, threadChannelId, comment.getMessage());
                        long messageId = telegramChannelService.getChannelThreadMessageByDate(discussionGroupId, threadChannelId, lastThreadMessageId, dateMessageSent);

                        comment.setTelegramThreadMessage(new TelegramThreadMessage(messageId, comment));
                        comment = commentService.save(comment);
                    }

                    log.info("New comment succesfully added: {}", comment.toString());

                    return new ResponseEntity<>(new NewEntryCommentResponse(entry, comment), HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            //The comment is a replay to another existing comment
            else {
                log.info("Adding new comment reply");
                Comment cParent = commentService.getFromId(comment.getCommentParent().getId());
                if (cParent != null) {

                    comment = commentService.save(comment);

                    cParent.getReplies().add(comment);
					/*Saving the modified parent comment: Cascade relationship between comment and 
					 its replies will add the new comment to CommentRepository*/
                    commentService.save(cParent);
                    Entry entry = entryService.getFromId(id_entry);

                    log.info("New comment succesfully added: {}", comment.toString());

                    User entryOwner = entry.getUser();
                    User commentOwner = comment.getUser();
                    User parentOwner = cParent.getUser();

                    TelegramChannel telegramChannel = cd.getCourse().getTelegramChannel();
                    if (telegramChannel != null) {

                        Comment lastEntryComment = entry.getComments().stream()
                                .max(Comparator.comparingLong(Comment::getDate))
                                .orElse(null);
                        long lastThreadMessageId = lastEntryComment != null ? lastEntryComment.getTelegramThreadMessage().getThreadMessageId() : 0;

                        long discussionGroupId = telegramChannel.getDiscussionGroupId();
                        long threadChannelId = entry.getTelegramThreadChannel().getThreadChannelId();

                        int dateMessageSent = telegramChannelService.sendTextMessageToTelegramChannel(discussionGroupId, threadChannelId, comment.getMessage());
                        long messageId = telegramChannelService.getChannelThreadMessageByDate(discussionGroupId, threadChannelId, lastThreadMessageId, dateMessageSent);

                        comment.setTelegramThreadMessage(new TelegramThreadMessage(messageId, comment));
                        comment = commentService.save(comment);
                    }


                    // send new comment in your entry notification
                    if(!commentOwner.equals(entryOwner)){
                        this.notificationDispatcher.notifyCommentAdded(entry, comment, commentOwner, cd.getCourse());
                    }

                    // send new reply to your comment notification
                    if(!parentOwner.equals(entryOwner) && !parentOwner.equals(commentOwner)){
                        this.notificationDispatcher.notifyCommentReply(cParent, commentOwner, cd.getCourse(), entry, comment);
                    }


                    return new ResponseEntity<>(new NewEntryCommentResponse(entry, comment), HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
        }
    }
}
