package com.classgram.backend.repo;

import com.classgram.backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Session findById(long id);
}
