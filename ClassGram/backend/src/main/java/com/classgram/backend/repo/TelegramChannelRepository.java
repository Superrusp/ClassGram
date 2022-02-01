package com.classgram.backend.repo;

import com.classgram.backend.model.TelegramChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChannelRepository extends JpaRepository<TelegramChannel, Long> {

}
