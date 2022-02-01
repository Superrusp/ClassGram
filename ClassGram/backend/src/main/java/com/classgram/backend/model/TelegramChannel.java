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
public class TelegramChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long channelId;

    private long discussionGroupId;

    @OneToOne(mappedBy = "telegramChannel")
    private Course course;

    public TelegramChannel(long channelId, long discussionGroupId, Course course) {
        this.channelId = channelId;
        this.discussionGroupId = discussionGroupId;
        this.course = course;
    }
}
