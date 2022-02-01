package com.classgram.backend.job;

import com.classgram.backend.dto.TelegramMessageDto;
import com.classgram.backend.model.*;
import com.classgram.backend.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class UpdateCourseEntryJob {

    private final UserService userService;
    private final CourseService courseService;
    private final EntryService entryService;
    private final CommentService commentService;
    private final TelegramChannelService telegramChannelService;
    private final TelegramMessageService telegramMessageService;

    @Autowired
    public UpdateCourseEntryJob(UserService userService, CourseService courseService, EntryService entryService,
                                CommentService commentService, TelegramChannelService telegramChannelService,
                                TelegramMessageService telegramMessageService) {
        this.userService = userService;
        this.courseService = courseService;
        this.entryService = entryService;
        this.commentService = commentService;
        this.telegramChannelService = telegramChannelService;
        this.telegramMessageService = telegramMessageService;
    }

    @Scheduled(cron = "0 0/3 * * * *")
    @Transactional
    public void addEntryCommentsFromTelegramThread() {
        log.debug("Run Job: add entry comments from telegram thread");
        System.out.println("\"Run Job: add entry comments from telegram thread\"");
        List<Course> allCourses = courseService.getRepo().findAll();

        for (Course course : allCourses) {
            TelegramChannel telegramChannel = course.getTelegramChannel();
            if (telegramChannel == null) {
                continue;
            }

            long discussionGroupId = telegramChannel.getDiscussionGroupId();

            Forum forum = course.getCourseDetails().getForum();
            if (forum == null) {
                continue;
            }

            List<Entry> entries = forum.getEntries();

            for (Entry entry : entries) {
                List<Comment> comments = entry.getComments();
                Comment lastEntryComment = comments
                        .stream()
                        .max(Comparator.comparingLong(Comment::getDate))
                        .orElse(null);
                long lastThreadMessageId = lastEntryComment != null ? lastEntryComment.getTelegramThreadMessage().getThreadMessageId() : 0;
                long threadChannelId = entry.getTelegramThreadChannel().getThreadChannelId();

                List<TelegramMessageDto> messages = telegramChannelService.getChannelThreadMessages(discussionGroupId, threadChannelId, lastThreadMessageId);
                for (TelegramMessageDto messageDto : messages) {
                    TelegramThreadMessage telegramMessage = telegramMessageService.getRepo().findById(messageDto.getId());
                    if (telegramMessage != null) {
                        continue;
                    }

                    User user = userService.getRepo().findByName("teacher@gmail.com");
                    Comment comment = new Comment(messageDto.getText(), System.currentTimeMillis(), user);
                    comment.setTelegramThreadMessage(new TelegramThreadMessage(messageDto.getId(), comment));
                    commentService.save(comment);

                    entry.getComments().add(comment);
                    entryService.save(entry);
                }
            }

        }
    }
}
