package com.hospital.medicalexam.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptBuilderTest {
    private final AiPromptBuilder builder = new AiPromptBuilder();

    @Test
    void buildIncludesStructuredMedicalContext() {
        String prompt = builder.build(new AiDraftRequest(
                1L,
                101L,
                201L,
                "检查",
                " 胸部DR正位片 ",
                " 张晓雨 ",
                " 女 ",
                " 双肺纹理稍增多 "
        ));

        assertThat(prompt)
                .contains("医院检查检验报告辅助系统")
                .contains("患者姓名：张晓雨")
                .contains("性别：女")
                .contains("项目类型：检查")
                .contains("项目名称：胸部DR正位片")
                .contains("医生输入结果：双肺纹理稍增多")
                .contains("\"riskLevel\": \"LOW|MEDIUM|HIGH|UNKNOWN\"");
    }

    @Test
    void buildUsesFallbackTextForBlankFields() {
        String prompt = builder.build(new AiDraftRequest(
                1L,
                101L,
                201L,
                null,
                "",
                "   ",
                null,
                ""
        ));

        assertThat(prompt).contains(
                "患者姓名：未提供",
                "性别：未提供",
                "项目类型：未提供",
                "项目名称：未提供",
                "医生输入结果：未提供"
        );
    }
}
