package com.careerconnect.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe registry of active SSE emitters.
 * When a job is created / updated / deleted, call broadcastJobUpdate()
 * to notify all connected browser clients instantly.
 */
@Component
public class SseEmitterRegistry {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(()    -> emitters.remove(emitter));
        emitter.onError(e       -> emitters.remove(emitter));
    }

    /**
     * Broadcast a "job-update" event to every connected client.
     * Dead emitters are pruned automatically.
     *
     * @param payload  a short string the client receives (e.g. "refresh")
     */
    public void broadcastJobUpdate(String payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("job-update")
                        .data(payload));
            } catch (IOException | IllegalStateException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
