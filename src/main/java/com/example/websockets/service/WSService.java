package com.example.websockets.service;

import com.example.websockets.model.KrakenNotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WSService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WSService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyFrontend(final String message) {
        KrakenNotificationMessage response = new KrakenNotificationMessage(message);
        messagingTemplate.convertAndSend("/topic/messages", response);
    }

}
