package com.classgram.backend.service;

import com.classgram.backend.model.Comment;
import com.classgram.backend.repo.CommentRepository;
import com.classgram.backend.struct.FTService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Getter
@Slf4j
public class CommentService implements FTService<Comment, Long> {
    private final CommentRepository repo;

    @Autowired
    public CommentService(CommentRepository repo) {
        this.repo = repo;
    }

    public Collection<Comment> getChildren(Comment comment){
        return this.repo.getAllByCommentParent(comment);
    }
}
