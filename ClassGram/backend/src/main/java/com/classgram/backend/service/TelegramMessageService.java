package com.classgram.backend.service;

import com.classgram.backend.model.TelegramThreadMessage;
import com.classgram.backend.repo.TelegramMessageRepository;
import com.classgram.backend.struct.FTService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
@Slf4j
public class TelegramMessageService implements FTService<TelegramThreadMessage, Long> {

    private final TelegramMessageRepository repo;

    @Autowired
    public TelegramMessageService(TelegramMessageRepository telegramMessageRepository) {
        this.repo = telegramMessageRepository;
    }
}
