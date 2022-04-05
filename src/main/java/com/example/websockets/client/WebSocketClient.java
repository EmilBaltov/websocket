package com.example.websockets.client;

import com.example.websockets.model.OrderBookLiveUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.http.WebSocket;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WebSocketClient implements WebSocket.Listener {

    private Logger logger = LoggerFactory.getLogger(WebSocketClient.class);

    private static final String ASKS = "asks";
    private static final String BIDS = "bids";
    private static final int MAX_SIZE = 15;

    private Map<String, Deque<OrderBookLiveUpdate>> dequeMapBTC = new HashMap<>(
            Map.of("asks", new ArrayDeque<>(), "bids", new ArrayDeque<>(), "BTC", new ArrayDeque<>()));
    private Map<String, Deque<OrderBookLiveUpdate>> dequeMapETH = new HashMap<>(
            Map.of("asks", new ArrayDeque<>(), "bids", new ArrayDeque<>()));

    private final CountDownLatch countDownLatch;

    public WebSocketClient(CountDownLatch latch) {
        this.countDownLatch = latch;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        logger.info(
                String.format("%s: Opened websocket session -> %s", LocalDateTime.now(), webSocket.getSubprotocol()));
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {

        String inputData = data.toString();
        if (inputData.contains("\"a\"") || inputData.contains("\"b\"")) {
            Map<String, Deque<OrderBookLiveUpdate>> dequeMap = updateDequeMap(inputData);
            printCurrentResultSorted(dequeMap);
        } else {
            logger.info(data.toString());
        }
        return WebSocket.Listener.super.onText(webSocket, data, false);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        error.printStackTrace();
        logger.error(error.getMessage());
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode,
                        String reason) {
        logger.info("Connection Abort " + statusCode + " " + reason);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    public void await() throws InterruptedException {
        countDownLatch.await();
    }

    private Map<String, Deque<OrderBookLiveUpdate>> updateDequeMap(String inputData) {

        String eventIndicator = !inputData.contains("\"a\"") ? "\"b\"" : "\"a\"";

        Map<String, Deque<OrderBookLiveUpdate>> dequeMap = inputData.contains("XBT/USD") ?
                dequeMapBTC : dequeMapETH;

        updateOrderBookMap(eventIndicator, getListForOrderBooksUpdate(inputData, eventIndicator), dequeMap);
        return dequeMap;
    }

    private void updateOrderBookMap(String eventIndicator, List<String> orderBooksUpdateString, Map<String, Deque<OrderBookLiveUpdate>> dequeMap) {

        Deque deque = getDequeBasedOnEventIndicator(eventIndicator, dequeMap);

        for (String string : orderBooksUpdateString) {
            List<String> preparedListForOrderBookObjects = new ArrayList<>(
                    Arrays.asList(string.replaceAll("\"", "").split(",")));

            OrderBookLiveUpdate orderBookLiveUpdate = new OrderBookLiveUpdate();
            orderBookLiveUpdate.setPrice(preparedListForOrderBookObjects.get(0));
            orderBookLiveUpdate.setVolume(preparedListForOrderBookObjects.get(1));

            deque.push(orderBookLiveUpdate);

            if (deque.size() > 15) {
                deque.pollLast();
            }
        }
    }

    private List<String> getListForOrderBooksUpdate(String inputData, String reg) {

        String orderBookUpdateString = inputData.substring(inputData.indexOf(reg) + 5, inputData.indexOf("\"c\"") - 2);

        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(orderBookUpdateString);

        List<String> orderBookFieldsString = new ArrayList<>();

        while (matcher.find()) {
            orderBookFieldsString.add(matcher.group(1));
        }
        return orderBookFieldsString;
    }

    private void printCurrentResultSorted(Map<String, Deque<OrderBookLiveUpdate>> dequeMap) {

        List<OrderBookLiveUpdate> orderBookSinglesSortAsks = dequeMap.get(ASKS)
                .stream()
                .sorted((Comparator.comparing(OrderBookLiveUpdate::getPrice)))
                .limit(MAX_SIZE)
                .toList();

        List<OrderBookLiveUpdate> orderBookSinglesSortBids = dequeMap.get(BIDS)
                .stream()
                .sorted((Comparator.comparing(OrderBookLiveUpdate::getPrice).reversed()))
                .limit(MAX_SIZE)
                .toList();

        OrderBookLiveUpdate bestAsk = orderBookSinglesSortAsks.iterator().hasNext() ?
                orderBookSinglesSortAsks.get(orderBookSinglesSortAsks.size() - 1) : null;

        OrderBookLiveUpdate bestBid = orderBookSinglesSortBids.iterator().hasNext() ?
                orderBookSinglesSortBids.iterator().next() : null;

        logger.info(getOrderBookBuilderToPrint(
                dequeMap, orderBookSinglesSortAsks, orderBookSinglesSortBids, bestAsk, bestBid)
                .toString());
    }

    private StringBuilder getOrderBookBuilderToPrint(
            Map<String, Deque<OrderBookLiveUpdate>> dequeMap,
            List<OrderBookLiveUpdate> orderBookSinglesSortAsks,
            List<OrderBookLiveUpdate> orderBookSinglesSortBids,
            OrderBookLiveUpdate bestAsk, OrderBookLiveUpdate bestBid) {

        StringBuilder builder = new StringBuilder(System.lineSeparator());
        builder.append("asks:");
        builder.append(System.lineSeparator()).append("[");

        updateBuilderWithList(orderBookSinglesSortAsks, builder);

        if (bestBid != null) {
            builder.append("best bid: ").append(bestBid).append(System.lineSeparator());
        }
        if (bestAsk != null) {
            builder.append("best ask: ").append(bestAsk).append(System.lineSeparator());
        }

        builder.append("bids:").append(System.lineSeparator()).append("[");
        updateBuilderWithList(orderBookSinglesSortBids, builder);
        builder.append(LocalDateTime.now()).append(System.lineSeparator());
        builder.append(dequeMap.containsKey("BTC") ? "BTC/USD" : "ETH/UDS").append(System.lineSeparator());


        return builder;
    }

    private void updateBuilderWithList(List<OrderBookLiveUpdate> orderBookSinglesSort, StringBuilder builder) {
        for (int i = 0; i < orderBookSinglesSort.size(); i++) {
            builder.append(" ");
            if (i > 0) {
                builder.append(" ");
            }
            builder.append(orderBookSinglesSort.get(i));
            builder.append(i < orderBookSinglesSort.size() - 1 ? "," : " ]").append(System.lineSeparator());
        }
    }

    private Deque getDequeBasedOnEventIndicator(String eventIndicator, Map<String, Deque<OrderBookLiveUpdate>> dequeMap) {
        Deque deque;
        if (eventIndicator.contains("a")) {
            deque = dequeMap.get(ASKS);
        } else {
            deque = dequeMap.get(BIDS);
        }
        return deque;
    }


}