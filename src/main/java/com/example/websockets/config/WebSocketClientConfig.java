package com.example.websockets.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

@Configuration
public class WebSocketClientConfig {

    private final int countDownLatchCount;

    public WebSocketClientConfig(@Value("${count-down-latch.count}") int countDownLatchCount) {
        this.countDownLatchCount = countDownLatchCount;
    }

    @Bean
    public CountDownLatch countDownLatch() {
        return new CountDownLatch(countDownLatchCount);
    }
}