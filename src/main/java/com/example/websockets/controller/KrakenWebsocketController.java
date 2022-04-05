package com.example.websockets.controller;

import com.example.websockets.service.WebSocketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/kraken")
public class KrakenWebsocketController {

    private final WebSocketService webSocketService;

    public KrakenWebsocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @GetMapping("/establish")
    public String establishConnection() {
        return webSocketService.establishConnection();
    }

    @GetMapping("/send")
    public String sendMessage() {
        return webSocketService.sendMessage();
    }

    @GetMapping("/close")
    public String closeConnection() {
        return webSocketService.closeConnection();
    }
}
