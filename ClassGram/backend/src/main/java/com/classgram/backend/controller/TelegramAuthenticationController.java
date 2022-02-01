package com.classgram.backend.controller;

import com.classgram.backend.service.TelegramApiExecutorService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api-telegram")
@Slf4j
public class TelegramAuthenticationController {

    private final TelegramApiExecutorService telegramApiExecutor;

    @Autowired
    public TelegramAuthenticationController(TelegramApiExecutorService telegramApiExecutor) {
        this.telegramApiExecutor = telegramApiExecutor;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> phoneNumberRequest) {
        log.info("Login request into Telegram");
        String phoneNumber = phoneNumberRequest.get("phoneNumber");
        String responseMessage = telegramApiExecutor.authorize(phoneNumber);
        return new ResponseEntity<>(new Gson().toJson(responseMessage), HttpStatus.ACCEPTED);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyConfirmationCode(@RequestBody Map<String, String> confirmationCodeRequest) {
        log.info("Verify confirmation code");
        String confirmationCode = confirmationCodeRequest.get("confirmationCode");
        String responseMessage = telegramApiExecutor.authorize(confirmationCode);
        return new ResponseEntity<>(new Gson().toJson(responseMessage), HttpStatus.ACCEPTED);
    }
}
