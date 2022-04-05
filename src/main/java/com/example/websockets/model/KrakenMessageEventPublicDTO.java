package com.example.websockets.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KrakenMessageEventPublicDTO {

    private String event = "subscribe";
    private List<String> pair = new ArrayList<>(List.of("XBT/USD", "ETH/USD"));
    private Map<String, Object> subscription = new HashMap<>(Map.of("depth",10, "name", "book"));

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<String> getPair() {
        return pair;
    }

    public void setPair(List<String> pair) {
        this.pair = pair;
    }

    public Map<String, Object> getSubscription() {
        return subscription;
    }

    public void setSubscription(Map<String, Object> subscription) {
        this.subscription = subscription;
    }
}
