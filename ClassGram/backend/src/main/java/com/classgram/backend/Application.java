package com.classgram.backend;

import it.tdlight.common.Init;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
@EnableAsync
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        loadTelegramNativeLibrary();
        SpringApplication.run(Application.class, args);
    }

    @SneakyThrows
    private static void loadTelegramNativeLibrary() {
        Init.start();
    }
}
