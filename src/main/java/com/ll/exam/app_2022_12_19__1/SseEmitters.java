package com.ll.exam.app_2022_12_19__1;


import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SseEmitters {
    private final HashMap<String, List<SseEmitter>> emitters = new HashMap<>();

    public SseEmitter add(String groupKey, SseEmitter emitter) {

        if (!emitters.containsKey(groupKey)) {
            emitters.put(groupKey, new CopyOnWriteArrayList<>());
        }

        emitters.get(groupKey).add(emitter);

        emitter.onCompletion(() -> {
            emitters.get(groupKey).remove(emitter);
        });

        emitter.onTimeout(emitter::complete);

        return emitter;
    }

    public void noti(String groupKey, String eventName) {
        noti(groupKey, eventName, Ut.mapOf());
    }

    public void noti(String groupKey, String eventName, Map<String, Object> data) {
        emitters.get(groupKey).forEach(emitter -> {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(data)
                );
            } catch (ClientAbortException e) {

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
