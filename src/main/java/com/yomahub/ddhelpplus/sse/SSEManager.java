package com.yomahub.ddhelpplus.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

public class SSEManager {

    private static Map<String, SseEmitter> sseMap = new HashMap<>();

    public static void addSse(String clientId, SseEmitter sseEmitter){
        sseMap.put(clientId, sseEmitter);
    }

    public static SseEmitter getSse(String clientId){
        return sseMap.get(clientId);
    }

    public static boolean contain(String clientId){
        return sseMap.containsKey(clientId);
    }

    public static void close(String clientId){
        SseEmitter sseEmitter = sseMap.get(clientId);
        if (sseEmitter != null) {
            sseEmitter.complete();
            sseMap.remove(clientId);
        }
    }

    public static void send(String message){
        Iterator<Map.Entry<String, SseEmitter>> it = sseMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, SseEmitter> entry = it.next();
            try {
                entry.getValue().send(message);
            } catch (Throwable t) {
            }
        }
    }
}
