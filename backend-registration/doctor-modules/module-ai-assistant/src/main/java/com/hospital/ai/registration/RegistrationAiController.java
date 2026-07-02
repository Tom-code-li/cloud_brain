package com.hospital.ai.registration;

import com.hospital.ai.common.RoleAiRequest;
import com.hospital.common.core.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/ai/registration")
@CrossOrigin(origins = {"http://127.0.0.1:5173", "http://localhost:5173"}, allowedHeaders = "*")
public class RegistrationAiController {
    private final RegistrationAiAssistant registrationAiAssistant;
    private final TaskExecutor aiTaskExecutor;

    public RegistrationAiController(
            RegistrationAiAssistant registrationAiAssistant,
            @Qualifier("aiTaskExecutor") TaskExecutor aiTaskExecutor
    ) {
        this.registrationAiAssistant = registrationAiAssistant;
        this.aiTaskExecutor = aiTaskExecutor;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestHeader(value = "X-Doctor-Id", required = false) Long doctorId,
            @RequestParam(value = "sceneCode", required = false, defaultValue = "TRIAGE") String sceneCode,
            @RequestParam(value = "businessId", required = false) Long businessId,
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam(value = "query", required = false, defaultValue = "") String query
    ) {
        if (sceneCode.isBlank()) {
            throw new BusinessException("AI 场景不能为空");
        }
        return createEmitter(new RoleAiRequest(
                "REGISTRATION",
                sceneCode,
                businessId,
                doctorId,
                patientId,
                Map.of("query", query)
        ));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPost(
            @RequestHeader(value = "X-Doctor-Id", required = false) Long doctorId,
            @RequestBody(required = false) RegistrationAiPayload payload
    ) {
        RegistrationAiPayload request = payload == null ? new RegistrationAiPayload(null, null, null, "") : payload;
        String sceneCode = request.sceneCode() == null || request.sceneCode().isBlank() ? "TRIAGE" : request.sceneCode();
        if (sceneCode.isBlank()) {
            throw new BusinessException("AI 场景不能为空");
        }
        return createEmitter(new RoleAiRequest(
                "REGISTRATION",
                sceneCode,
                request.businessId(),
                doctorId,
                request.patientId(),
                Map.of("query", request.query() == null ? "" : request.query())
        ));
    }

    private SseEmitter createEmitter(RoleAiRequest request) {
        SseEmitter emitter = new SseEmitter(30_000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> closed.set(true));
        emitter.onError(ex -> closed.set(true));

        aiTaskExecutor.execute(() -> {
            try {
                for (String chunk : registrationAiAssistant.assist(request)) {
                    if (closed.get()) {
                        return;
                    }
                    emitter.send(SseEmitter.event().name("message").data(chunk));
                    Thread.sleep(120);
                }
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    private record RegistrationAiPayload(
            String sceneCode,
            Long businessId,
            Long patientId,
            String query
    ) {
    }
}
