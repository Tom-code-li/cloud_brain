package com.hospital.medicalexam;

import com.hospital.medicalexam.ai.AiDraftRequest;
import com.hospital.medicalexam.ai.FallbackAiDraftService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiDraftServiceTest {
    @Test
    void fallbackGeneratesReadableExamDraft() {
        FallbackAiDraftService service = new FallbackAiDraftService();

        String draft = service.generate(new AiDraftRequest(
                1L,
                2L,
                3L,
                "检查",
                "胸部DR正位片",
                "张晓雨",
                "女",
                "胸部DR示双肺纹理稍增多"
        ));

        assertThat(draft).contains("AI辅助草稿");
        assertThat(draft).contains("胸部DR正位片");
        assertThat(draft).contains("医生确认");
    }
}
