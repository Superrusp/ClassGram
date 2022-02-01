package com.classgram.backend.repo;

import com.classgram.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment findById(long id);
    Collection<Comment> getAllByCommentParent(Comment commentParent);
}
