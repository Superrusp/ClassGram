package com.classgram.backend.controller;

import com.classgram.backend.model.Comment;
import com.classgram.backend.model.Course;
import com.classgram.backend.model.User;
import com.classgram.backend.security.AuthorizationService;
import com.classgram.backend.security.user.UserComponent;
import com.classgram.backend.service.AssetsService;
import com.classgram.backend.service.CommentService;
import com.classgram.backend.service.CourseService;
import com.classgram.backend.struct.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;


@RestController
@RequestMapping("/assets")
public class AssetsController {

    private final AssetsService assetsService;
    private final AuthorizationService authorizationService;
    private final CourseService courseService;
    private final CommentService commentService;
    private final UserComponent userComponent;

    @Autowired
    public AssetsController(AssetsService assetsService, AuthorizationService authorizationService, CourseService courseService, CommentService commentService, UserComponent userComponent) {
        this.assetsService = assetsService;
        this.authorizationService = authorizationService;
        this.courseService = courseService;
        this.commentService = commentService;
        this.userComponent = userComponent;
    }

    @GetMapping("/pictures/{file_name}")
    public ResponseEntity<Resource> downloadPicture(@PathVariable("file_name") String fileName) throws IOException {
        Resource resource = this.assetsService.downloadAsset(fileName, FileType.PICTURE);
        if (Objects.nonNull(resource)) {
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/audios/comment/{commentId}/{courseId}")
    public ResponseEntity<Resource> downloadAudioComment(@PathVariable("commentId") long commentId, @PathVariable("courseId") long courseId) throws IOException {

        Course course = this.courseService.getFromId(courseId);
        Comment comment = this.commentService.getFromId(commentId);
        User user = this.userComponent.getLoggedUser();

        if (Objects.isNull(comment) || Objects.isNull(course)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ResponseEntity unAuthorized = this.authorizationService.checkAuthorizationUsers(user, course.getAttenders());

        if (Objects.nonNull(unAuthorized)) {
            return unAuthorized;
        }

        String fileName = comment.getAudioUrl();

        Resource resource = this.assetsService.downloadAsset(fileName, FileType.AUDIO);
        if (Objects.nonNull(resource)) {
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            return ResponseEntity.noContent().build();
        }
    }


}
