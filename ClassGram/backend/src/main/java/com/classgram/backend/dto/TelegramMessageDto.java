package com.classgram.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TelegramMessageDto {

    private long id;
    private String text;
}
