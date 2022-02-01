package com.classgram.backend.config.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelegramProperties {
    private int apiId;
    private String apiHash;
    private String systemLanguageCode;
    private String deviceModel;
    private String applicationVersion;
    private boolean enableStorageOptimizer;
}
