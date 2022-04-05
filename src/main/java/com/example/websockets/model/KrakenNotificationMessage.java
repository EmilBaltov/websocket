package com.example.websockets.model;

public class KrakenNotificationMessage {
    
    private String content;

    public KrakenNotificationMessage() {
    }

    public KrakenNotificationMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
