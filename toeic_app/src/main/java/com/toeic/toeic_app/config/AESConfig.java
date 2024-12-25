package com.toeic.toeic_app.config;

import com.toeic.toeic_app.util.AESUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class AESConfig {

    private static final String PRIVATE_KEY = "Tuandz99"; // Khóa do bạn tự định nghĩa

    @Bean
    public SecretKey secretKey() {
        return AESUtil.generateKeyFromString(PRIVATE_KEY);
    }
}

