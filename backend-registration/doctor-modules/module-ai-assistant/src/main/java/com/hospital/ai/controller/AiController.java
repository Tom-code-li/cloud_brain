package com.hospital.ai.controller;

import com.hospital.ai.common.RoleAiRequest;
import com.hospital.ai.domain.AiCallLogView;
import com.hospital.ai.domain.AiRequest;
import com.hospital.ai.service.SimulatedAiService;
import com.hospital.common.core.R;
import com.hospital.common.core.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/ai")
public class AiController {
    private final SimulatedAiService aiService;
    private final TaskExecutor aiTaskExecutor;

    public AiController(
            SimulatedAiService aiService,
            @Qualifier("aiTaskExecutor") TaskExecutor aiTaskExecutor
    ) {
        this.aiService = aiService;
        this.aiTaskExecutor = aiTaskExecutor;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestHeader(value = "X-Doctor-Id", required = false) Long doctorId,
            @RequestBody AiRequest request
    ) {
        if (request == null || request.businessType() == null || request.businessType().isBlank()) {
            throw new BusinessException("AI 辅助业务类型不能为空");
        }
        return createEmitter(request.businessType(), request.businessId(), request.contextData(), doctorId);
    }

    @PostMapping(value = "/{roleCode}/{sceneCode}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamByRole(
            @RequestHeader(value = "X-Doctor-Id", required = false) Long doctorId,
            @PathVariable String roleCode,
            @PathVariable String sceneCode,
            @RequestBody(required = false) AiRequest request
    ) {
        if (roleCode.isBlank()) {
            throw new BusinessException("AI 角色不能为空");
        }
        if (sceneCode.isBlank()) {
            throw new BusinessException("AI 场景不能为空");
        }
        Map<String, Object> contextData = request == null ? Map.of() : request.contextData();
        Long businessId = request == null ? null : request.businessId();
        Long patientId = extractLong(contextData, "patientId");
        return createRoleEmitter(roleCode, sceneCode, businessId, contextData, doctorId, patientId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamByQuery(
            @RequestHeader(value = "X-Doctor-Id", required = false) Long doctorId,
            @RequestParam String businessType,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false, defaultValue = "") String query
    ) {
        if (businessType.isBlank()) {
            throw new BusinessException("AI 辅助业务类型不能为空");
        }
        return createEmitter(businessType, businessId, Map.of("query", query), doctorId);
    }

    @GetMapping(value = "/{roleCode}/{sceneCode}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamByRoleQuery(
            @RequestHeader(value = "X-Doctor-Id", required = false) Long doctorId,
            @PathVariable String roleCode,
            @PathVariable String sceneCode,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false, defaultValue = "") String query
    ) {
        if (roleCode.isBlank()) {
            throw new BusinessException("AI 角色不能为空");
        }
        if (sceneCode.isBlank()) {
            throw new BusinessException("AI 场景不能为空");
        }
        return createRoleEmitter(roleCode, sceneCode, businessId, Map.of("query", query), doctorId, patientId);
    }

    private SseEmitter createEmitter(
            String businessType,
            Long businessId,
            Map<String, Object> contextData,
            Long doctorId
    ) {
        SseEmitter emitter = new SseEmitter(30_000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> closed.set(true));
        emitter.onError(ex -> closed.set(true));

        aiTaskExecutor.execute(() -> {
            try {
                List<String> chunks = aiService.assist(
                        businessType,
                        businessId,
                        contextData,
                        doctorId
                );
                for (String chunk : chunks) {
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

    private SseEmitter createRoleEmitter(
            String roleCode,
            String sceneCode,
            Long businessId,
            Map<String, Object> contextData,
            Long doctorId,
            Long patientId
    ) {
        SseEmitter emitter = new SseEmitter(30_000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> closed.set(true));
        emitter.onError(ex -> closed.set(true));

        aiTaskExecutor.execute(() -> {
            try {
                List<String> chunks = aiService.assist(new RoleAiRequest(
                        roleCode,
                        sceneCode,
                        businessId,
                        doctorId,
                        patientId,
                        contextData
                ));
                for (String chunk : chunks) {
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

    @GetMapping("/logs")
    public R<List<AiCallLogView>> logs() {
        return R.ok(aiService.logs());
    }

    private Long extractLong(Map<String, Object> contextData, String key) {
        if (contextData == null || !contextData.containsKey(key)) {
            return null;
        }
        Object value = contextData.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
