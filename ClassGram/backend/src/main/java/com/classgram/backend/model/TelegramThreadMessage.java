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
public class TelegramThreadMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long threadMessageId;

    @OneToOne(mappedBy = "telegramThreadMessage")
    private Comment comment;

    public TelegramThreadMessage(long threadMessageId, Comment comment) {
        this.threadMessageId = threadMessageId;
        this.comment = comment;
    }
}
