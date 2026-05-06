package com.careerconnect.controller;

import com.careerconnect.config.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Provides a Server-Sent Events (SSE) stream endpoint.
 * Job-seeker pages subscribe to /api/jobs/stream; this long-lived
 * connection receives "job-update" events whenever the job board changes.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobEventSseController {

    private final SseEmitterRegistry registry;

    /**
     * Opens a long-lived SSE connection (timeout = 5 min).
     * The browser will auto-reconnect if the connection drops.
     */
    @GetMapping(value = "/jobs/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        // 5-minute timeout; browser reconnects automatically via EventSource
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        registry.addEmitter(emitter);

        // Send an immediate "connected" heartbeat so the client knows it's live
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("ok"));
        } catch (Exception e) {
            emitter.complete();
        }
        return emitter;
    }
}
