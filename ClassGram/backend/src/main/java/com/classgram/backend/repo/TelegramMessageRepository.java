package com.classgram.backend.repo;

import com.classgram.backend.model.TelegramThreadMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramMessageRepository extends JpaRepository<TelegramThreadMessage, Long> {

    TelegramThreadMessage findById(long id);

}
