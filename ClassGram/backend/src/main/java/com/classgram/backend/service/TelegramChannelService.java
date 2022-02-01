package com.classgram.backend.service;

import com.classgram.backend.dto.TelegramMessageDto;
import com.classgram.backend.model.Course;
import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE,
        proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class TelegramChannelService {

    private final TelegramApiExecutorService telegramApiExecutorService;

    @Autowired
    public TelegramChannelService(TelegramApiExecutorService telegramApiExecutorService) {
        this.telegramApiExecutorService = telegramApiExecutorService;
    }

    public long createSuperGroupChatByCourse(Course course, boolean isChannel) {
        AtomicLong chatId = new AtomicLong();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        TdApi.CreateNewSupergroupChat channel = new TdApi.CreateNewSupergroupChat(course.getTitle(), isChannel,
                course.getCourseDetails().getInfo(), null, false);

        telegramApiExecutorService.getClient().send(channel, object -> {
            TdApi.Chat createdChat = (TdApi.Chat) object;
            log.debug("Successfully created Telegram group chat: {}", createdChat);
            chatId.set(createdChat.id);
            countDownLatch.countDown();
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return chatId.get();
    }

    public void linkChatDiscussionGroupToChannel(long channelId, long discussionGroupId) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        TdApi.SetChatDiscussionGroup chatDiscussionGroup = new TdApi.SetChatDiscussionGroup(channelId, discussionGroupId);
        telegramApiExecutorService.getClient().send(chatDiscussionGroup, new ResultHandler<TdApi.Ok>() {
            @Override
            public void onResult(TdApi.Object object) {
                log.debug("Successfully linked Telegram group chat to channel");
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendTextMessageToTelegramChannel(long channelId, String message) {
        sendTextMessageToTelegramChannel(channelId, 0, message);
    }

    public int sendTextMessageToTelegramChannel(long channelId, long messageThreadId, String message) {
        AtomicInteger date = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        TdApi.FormattedText messageText = new TdApi.FormattedText(message, null);
        TdApi.InputMessageContent content = new TdApi.InputMessageText(messageText, false, true);
        TdApi.SendMessage sendMessage = new TdApi.SendMessage(channelId, messageThreadId, 0, null, null, content);

        telegramApiExecutorService.getClient().send(sendMessage, new ResultHandler<TdApi.Message>() {
            @Override
            public void onResult(TdApi.Object object) {
                log.debug("Message successfully sent");
                date.set(((TdApi.Message) object).date);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return date.get();
    }

    public long sendMessageToExactThreadOfDiscussionGroup(long chatId, String message) {
        AtomicLong messageThreadId = new AtomicLong();
        AtomicLong messageId = new AtomicLong();

        messageId.set(getPostedMessageId(chatId));

        CountDownLatch countDownLatch = new CountDownLatch(1);

        TdApi.GetMessageThread messageThread = new TdApi.GetMessageThread(chatId, messageId.get());
        telegramApiExecutorService.getClient().send(messageThread, new ResultHandler<TdApi.MessageThreadInfo>() {
            @Override
            public void onResult(TdApi.Object object) {
                messageThreadId.set(((TdApi.MessageThreadInfo) object).messageThreadId);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
            sendTextMessageToTelegramChannel(chatId, messageThreadId.get(), message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return messageThreadId.get();
    }

    public void sendAudioMessageToTelegramChannel(long chatId, long messageThreadId, String audioFilePath) {
        TdApi.InputFile inputFile = new TdApi.InputFileLocal(audioFilePath);
        TdApi.FormattedText caption = new TdApi.FormattedText(null, null);
        TdApi.InputMessageAudio inputMessageAudio =
                new TdApi.InputMessageAudio(inputFile, null, 0, "Audio comment", null, caption);
        telegramApiExecutorService.getClient().send(new TdApi.SendMessage(chatId, messageThreadId, 0, null, null, inputMessageAudio),
                new ResultHandler<TdApi.Message>() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        log.debug("Audio message with id={} sent successfully", ((TdApi.Message) object).id);
                    }
                });
    }

    public void sendFileToTelegramChannel(long chatId, String filePath) {
        TdApi.InputFile inputFile = new TdApi.InputFileLocal(filePath);
        TdApi.FormattedText caption = new TdApi.FormattedText(null, null);
        TdApi.InputMessageContent content = new TdApi.InputMessageDocument(inputFile, null, true, caption);
        telegramApiExecutorService.getClient().send(new TdApi.SendMessage(chatId, 0, 0, null, null, content),
                new ResultHandler<TdApi.Message>() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        log.debug("File document send successfully to the chat(id={})", ((TdApi.Message) object).id);
                    }
                });
    }

    public long getChannelThreadMessageByDate(long chatId, long messageThreadId, long fromMessageId, long date) {
        AtomicLong messageId = new AtomicLong();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        TdApi.GetMessageThreadHistory getMessageThreadHistory = new TdApi.GetMessageThreadHistory(chatId, messageThreadId, fromMessageId, -50, 50);
        telegramApiExecutorService.getClient().send(getMessageThreadHistory, new ResultHandler<TdApi.Messages>() {
            @Override
            public void onResult(TdApi.Object object) {
                List<TdApi.Message> messages = Arrays.stream(((TdApi.Messages) object).messages).collect(Collectors.toList());
                log.debug("Thread Messages: {}", messages);
                messageId.set(messages
                        .stream()
                        .filter(message -> message.date == date)
                        .map(message -> message.id)
                        .findAny()
                        .orElse(0L));
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return messageId.get();
    }

    public List<TelegramMessageDto> getChannelThreadMessages(long chatId, long messageThreadId, long fromMessageId) {
        StringBuffer messageIdsString = new StringBuffer();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        TdApi.GetMessageThreadHistory getMessageThreadHistory = new TdApi.GetMessageThreadHistory(chatId, messageThreadId, fromMessageId, -50, 50);
        telegramApiExecutorService.getClient().send(getMessageThreadHistory, new ResultHandler<TdApi.Messages>() {
            @Override
            public void onResult(TdApi.Object object) {
                try {
                    List<TdApi.Message> messages = Arrays.stream(((TdApi.Messages) object).messages)
                            .collect(Collectors.toList());
                    log.debug("Thread Messages: {}", messages);

                    messages.stream()
                            .filter(message -> message.id != fromMessageId)
                            .forEach(message -> messageIdsString.append(message.id)
                                    .append("=")
                                    .append(((TdApi.MessageText) message.content).text.text)
                                    .append(";"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        });

        List<TelegramMessageDto> result = new ArrayList<>();
        try {
            countDownLatch.await();
            String[] messages = messageIdsString.toString().split(";");
            for (String message : messages) {
                String id = StringUtils.substringBefore(message, "=");
                String text = StringUtils.substringAfter(message, "=");
                if (!id.isEmpty()) {
                    result.add(new TelegramMessageDto(Long.parseLong(id), text));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collections.reverse(result);
        return result;
    }

    public long getPostedMessageId(long chatId) {
        AtomicLong messageId = new AtomicLong();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        int date = (int) (new Date().getTime() / 1000);

        TdApi.GetChatMessageByDate getMessageThread = new TdApi.GetChatMessageByDate(chatId, date);
        telegramApiExecutorService.getClient().send(getMessageThread, new ResultHandler<TdApi.Message>() {
            @Override
            public void onResult(TdApi.Object object) {
                messageId.set(((TdApi.Message) object).id);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return messageId.get();
    }
}
