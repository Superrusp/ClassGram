package com.classgram.backend.service;

import com.classgram.backend.config.properties.TelegramProperties;
import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.tdlight.ClientManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.classgram.backend.service.TelegramApiExecutorService.AuthMessage.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramApiExecutorService {

    public static final AtomicBoolean next = new AtomicBoolean(false);

    public final AtomicBoolean nextSp = new AtomicBoolean(false);

    private static final ConcurrentHashMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();

    private static volatile String authResult;

    private final TelegramProperties telegramProperties;

    private TelegramClient client;

    private TdApi.AuthorizationState authorizationState;

    private volatile String queryParam;

    @PostConstruct
    private synchronized void createClient() {
        client = ClientManager.create();
        client.initialize(new UpdateHandler(), null, null);
        client.execute(new TdApi.SetLogVerbosityLevel(0));
    }

    private class UpdateHandler implements ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR -> onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                case TdApi.UpdateNewChat.CONSTRUCTOR -> {
                    var updateNewChat = (TdApi.UpdateNewChat) object;
                    var chat = updateNewChat.chat;
                    chats.put(chat.id, chat);
                }

                default -> {
                }
            }
        }
    }

    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null)
            this.authorizationState = authorizationState;

        switch (this.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                var parameters = new TdApi.TdlibParameters();

                parameters.apiId = telegramProperties.getApiId();
                parameters.apiHash = telegramProperties.getApiHash();
                parameters.systemLanguageCode = telegramProperties.getSystemLanguageCode();
                parameters.deviceModel = telegramProperties.getDeviceModel();
                parameters.applicationVersion = telegramProperties.getApplicationVersion();
                parameters.enableStorageOptimizer = telegramProperties.isEnableStorageOptimizer();

                client.send(new TdApi.SetTdlibParameters(parameters), new TelegramApiExecutorService.AuthorizationRequestHandler());
                return;
            }
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                return;
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                if (queryParam == null) {
                    setAuthResultAndGoNext(ENTER_PHONE_NUMBER);
                    break;
                }
                client.send(new TdApi.SetAuthenticationPhoneNumber(queryParam, null), new TelegramApiExecutorService.AuthorizationRequestHandler());
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                if (queryParam == null) {
                    setAuthResultAndGoNext(ENTER_AUTHENTICATION_CODE);
                    break;
                }
                client.send(new TdApi.CheckAuthenticationCode(queryParam), new AuthorizationRequestHandler());
            }

            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                if (queryParam == null) {
                    setAuthResultAndGoNext(ENTER_PASSWORD);
                    break;
                }
                client.send(new TdApi.CheckAuthenticationPassword(queryParam), new TelegramApiExecutorService.AuthorizationRequestHandler());
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR -> setAuthResultAndGoNext(YOU_ARE_AUTHORIZED);
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> setAuthResultAndGoNext(YOU_ARE_LOGGED_OUT);
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                createClient();
                setAuthResultAndGoNext(YOU_ARE_LOGGED_OUT);
            }

            default -> setAuthResultAndGoNext(UNSUPPORTED_STATE);
        }

        queryParam = null;
    }

    @SneakyThrows
    public String authorize(String param) {
        queryParam = param;

        next.set(false);
        if (authorizationState != null)
            onAuthorizationStateUpdated(authorizationState);

        while (!next.get())
            Thread.onSpinWait();

        queryParam = null;

        return authResult;
    }

    private static void setAuthResultAndGoNext(AuthMessage message) {
        authResult = message.toString();
        next.set(true);
    }

    enum AuthMessage {
        ENTER_PHONE_NUMBER,
        ENTER_AUTHENTICATION_CODE,
        ENTER_PASSWORD,
        YOU_ARE_AUTHORIZED,
        YOU_ARE_LOGGED_OUT,
        UNSUPPORTED_STATE
    }

    private class AuthorizationRequestHandler implements ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    log.error(String.format("Receive an error: %s", object));
                    onAuthorizationStateUpdated(null); // repeat last action
                }
                case TdApi.Ok.CONSTRUCTOR -> {
                    // result already received through UpdateAuthorizationState, nothing to do
                }
                default -> log.error(String.format("Receive wrong response from TDLib: %s", object));
            }
        }
    }

    public TelegramClient getClient() {
        return client;
    }
}
