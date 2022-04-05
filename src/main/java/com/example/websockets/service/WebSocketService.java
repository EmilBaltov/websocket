package com.example.websockets.service;

import com.example.websockets.client.WebSocketClient;
import com.example.websockets.model.KrakenMessageEventPublicDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

@Service
public class WebSocketService {

    private Logger logger = LoggerFactory.getLogger(WebSocketClient.class);

    private final WebSocketClient webSocketClient;
    private final String krakenUrl;
    private WebSocket ws = null;

    public WebSocketService(WebSocketClient webSocketClient, @Value("${kraken.websocket.public-url}") String krakenUrl) {
        this.webSocketClient = webSocketClient;
        this.krakenUrl = krakenUrl;
    }

    public String establishConnection() {

        if (isConnectionEstablished()) {
            return "The Connection is already established";
        }

        ws = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(krakenUrl), webSocketClient)
                .join();

        return "Connection Established";
    }

    public String sendMessage() {

        if (!isConnectionEstablished()) {
            return "You have to establish a connection first!!!";
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            ws.sendText(mapper.writeValueAsString(new KrakenMessageEventPublicDTO()), true);
            webSocketClient.await();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return "";
    }

    public String closeConnection() {
        if (ws == null || (ws.isInputClosed() || ws.isOutputClosed())) {
            return "The Connection is already closed";
        }
        ws.sendClose(1000, "");
        return "The Connection closed Successfully";
    }

    private boolean isConnectionEstablished() {
        return ws != null && (!ws.isOutputClosed() || !ws.isInputClosed());
    }
}
