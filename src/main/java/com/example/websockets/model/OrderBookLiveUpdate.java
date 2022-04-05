package com.example.websockets.model;

public class OrderBookLiveUpdate {

    private String price;
    private String volume;

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "[ " + price + ", " + volume + " ]";
    }
}
