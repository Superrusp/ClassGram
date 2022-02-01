package com.classgram.backend.service;

import com.classgram.backend.model.*;
import com.classgram.backend.repo.CourseInvitationNotificationRepo;
import com.classgram.backend.repo.CourseRepository;
import com.classgram.backend.repo.SessionNotificationRepo;
import com.classgram.backend.security.user.UserComponent;
import com.classgram.backend.struct.FTService;
import com.classgram.backend.struct.Role;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Getter
@Service
@Slf4j
public class CourseService implements FTService<Course, Long> {

    private final CourseRepository repo;
    private final UserService userService;
    private final UserComponent userComponent;
    private final FileService fileService;
    private final TelegramChannelService telegramChannelService;
    private final CourseInvitationNotificationRepo courseInvitationNotificationRepo;
    private final SessionNotificationRepo sessionNotificationRepo;

    @Autowired
    public CourseService(CourseRepository repo, UserService userService, UserComponent userComponent, FileService fileService,
                         TelegramChannelService telegramChannelService, CourseInvitationNotificationRepo courseInvitationNotificationRepo,
                         SessionNotificationRepo sessionNotificationRepo) {
        this.repo = repo;
        this.userService = userService;
        this.userComponent = userComponent;
        this.fileService = fileService;
        this.telegramChannelService = telegramChannelService;
        this.courseInvitationNotificationRepo = courseInvitationNotificationRepo;
        this.sessionNotificationRepo = sessionNotificationRepo;
    }

    public Collection<Course> getCoursesFilteringHiddenFiles(User user) {
        Collection<Course> courses = this.repo.findAllByAttendersIn(Collections.singleton(user));
        if (!user.isRole(Role.TEACHER)) {
            for (Course course : courses) {
                CourseDetails courseDetails = course.getCourseDetails();
                if (Objects.nonNull(courseDetails)) {
                    List<FileGroup> files = courseDetails.getFiles();
                    if (Objects.nonNull(files)) {
                        for (FileGroup fileGroup : courseDetails.getFiles()) {
                            fileGroup.getFiles().removeIf(file -> file.isHidden() || (Objects.nonNull(file.getHiddenUntil()) && file.getHiddenUntil().before(new Date())));
                        }
                    }
                }
            }
        }
        return courses;
    }

    public Collection<User> removeAttender(User attender, Course c) {
        c.getAttenders().remove(attender);
        attender.getCourses().remove(c);
        this.save(c);
        this.userService.save(attender);
        return c.getAttenders();
    }

    @Override
    public Course getFromId(Long id) {
        Course course = this.repo.findById(id).orElse(null);

        // must check if the files of this course have date restriction
        if (Objects.nonNull(course) && Objects.nonNull(this.userComponent.getLoggedUser())
                && !this.userComponent.getLoggedUser().isRole(Role.TEACHER)) {
            CourseDetails courseDetails = course.getCourseDetails();
            if (Objects.nonNull(courseDetails)) {

                // check all file groups
                for (FileGroup fileGroup : courseDetails.getFiles()) {

                    // check all files
                    for (Iterator<File> iterator = fileGroup.getFiles().iterator(); iterator.hasNext(); ) {
                        File file = iterator.next();
                        Date hiddenUntil = file.getHiddenUntil();

                        // file is hidden
                        if (Objects.nonNull(hiddenUntil)) {
                            Date now = new Date();

                            // file was hidden until a date lower than now
                            if (hiddenUntil.before(now)) {
                                file.setHiddenUntil(null);
                                fileService.save(file);
                            }

                            // file is still hidden, dont retrieve it
                            else {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }

        return course;
    }

    @Override
    public Course save(Course course) {
        if (course.getTelegramChannel() == null) {
            CountDownLatch countDownLatch = new CountDownLatch(2);
            long channelId = telegramChannelService.createSuperGroupChatByCourse(course, true);
            countDownLatch.countDown();
            long discussionGroupId = telegramChannelService.createSuperGroupChatByCourse(course, false);
            countDownLatch.countDown();

            try {
                countDownLatch.await();
                telegramChannelService.linkChatDiscussionGroupToChannel(channelId, discussionGroupId);
                course.setTelegramChannel(new TelegramChannel(channelId, discussionGroupId, course));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.getRepo().save(course);
    }

    @Override
    public void deleteById(Long id) {
        this.courseInvitationNotificationRepo.deleteAllByCourse_Id(id);
        this.sessionNotificationRepo.deleteAllByCourse_Id(id);
        this.repo.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<Course> entities) {
        for (Course course : entities) {
            this.delete(course);
        }
    }

    @Override
    public void delete(Course entity) {
        this.deleteById(entity.getId());
    }
}
