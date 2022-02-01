package com.classgram.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TelegramThreadChannel {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long threadChannelId;

    @OneToOne(mappedBy = "telegramThreadChannel")
    private Entry entry;

    public TelegramThreadChannel(long threadChannelId, Entry entry) {
        this.threadChannelId = threadChannelId;
        this.entry = entry;
    }
}
