package com.classgram.backend.controller;

import com.classgram.backend.annotation.RoleFilter;
import com.classgram.backend.model.*;
import com.classgram.backend.service.*;
import com.classgram.backend.entry.NewEntryCommentResponse;
import com.classgram.backend.security.AuthorizationService;
import com.classgram.backend.security.user.UserComponent;
import com.classgram.backend.struct.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api-entries")
@Slf4j
public class EntryController extends SecureController {

    private final ForumService forumService;
    private final EntryService entryService;
    private final CommentService commentService;
    private final CourseDetailsService courseDetailsService;
    private final TelegramChannelService telegramChannelService;

    @Value("${server.url}")
    private String baseUrl;

    public EntryController(ForumService forumService, EntryService entryService, CommentService commentService, CourseDetailsService courseDetailsService,
                           UserComponent user, AuthorizationService authorizationService, TelegramChannelService telegramChannelService) {
        super(user, authorizationService);
        this.forumService = forumService;
        this.entryService = entryService;
        this.commentService = commentService;
        this.courseDetailsService = courseDetailsService;
        this.telegramChannelService = telegramChannelService;
    }

    @RequestMapping(value = "/forum/{id}", method = RequestMethod.POST)
    public ResponseEntity<Object> newEntry(@RequestBody Entry entry, @PathVariable(value = "id") long courseDetailsId) {

        log.info("CRUD operation: Adding new entry");

        CourseDetails cd = courseDetailsService.getFromId(courseDetailsId);

        ResponseEntity<Object> userAuthorized = authorizationService.checkAuthorizationUsers(cd, cd.getCourse().getAttenders());
        if (userAuthorized != null) { // If the user is not an attender of the course
            return userAuthorized;
        } else {

            Forum forum = cd.getForum();

            //Setting the author of the entry
            User userLogged = user.getLoggedUser();
            entry.setUser(userLogged);

            //Setting the author and date of its first comment
            Comment comment = entry.getComments().get(0);
            comment.setUser(userLogged);
            comment.setDate(System.currentTimeMillis());

            //Setting the date of the entry
            entry.setDate(System.currentTimeMillis());

            comment = commentService.save(comment);
            entry = entryService.save(entry);

            forum.getEntries().add(entry);
            forumService.save(forum);


            TelegramChannel telegramChannel = cd.getCourse().getTelegramChannel();
            if (telegramChannel != null) {
                String message = "Додана нова тема до форуму: " + "\"" + entry.getTitle() + "\"" +
                    String.format("\nПосилання на тему: %s" + "courses/%s", baseUrl, forum.getId());
                long messageThreadId = 0;
                try {
                    telegramChannelService.sendTextMessageToTelegramChannel(telegramChannel.getChannelId(), message);
                    Thread.sleep(5000);
                    messageThreadId = telegramChannelService.sendMessageToExactThreadOfDiscussionGroup(telegramChannel.getDiscussionGroupId(), comment.getMessage());
                    long messageId = telegramChannelService.getPostedMessageId(telegramChannel.getDiscussionGroupId());
                    comment.setTelegramThreadMessage(new TelegramThreadMessage(messageId, comment));
                    comment = commentService.save(comment);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                entry.setTelegramThreadChannel(new TelegramThreadChannel(messageThreadId, entry));
            }
            entry = entryService.save(entry);
            log.info("New entry successfully added: {}", entry.toString());
			
			/*Entire forum is returned in order to have the new entry ID available just
			in case the author wants to add to it a new comment without refreshing the page*/
            return new ResponseEntity<>(new NewEntryCommentResponse(entry, comment), HttpStatus.CREATED);
        }
    }


    @RoleFilter(role = Role.TEACHER)
    @RequestMapping(value = "/remove/{id}/{cd-id}", method = RequestMethod.POST)
    public ResponseEntity<?> removeEntry(@PathVariable Long id, @PathVariable("cd-id") Long courseDetailsId) {


        log.info("Performing entry deletion!");
        CourseDetails cd = courseDetailsService.getFromId(courseDetailsId);
        Entry entry = this.entryService.getFromId(id);


        // Authorize teacher
        ResponseEntity<Object> teacherUnAuthorized = authorizationService.checkAuthorization(entry, cd.getCourse().getTeacher());
        if (teacherUnAuthorized != null) {
            log.info("The user is not a teacher of this course!");
            return teacherUnAuthorized;
        }


        //delete the entry
        log.info("Removing entry {} with course details id: {}", id, courseDetailsId);
        this.forumService.removeEntry(entry, cd.getForum());
        this.entryService.delete(entry);
        log.info("Entry removed successfully!");
        return ResponseEntity.ok(true);

    }

}
