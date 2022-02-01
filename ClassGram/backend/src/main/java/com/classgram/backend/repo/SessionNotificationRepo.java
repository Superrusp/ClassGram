package com.classgram.backend.repo;

import com.classgram.backend.model.SessionStartedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface SessionNotificationRepo extends JpaRepository<SessionStartedNotification, Long> {

    @Transactional
    void deleteAllBySession_Id(Long sessionId);

    @Transactional
    void deleteAllByCourse_Id(Long courseId);

}
