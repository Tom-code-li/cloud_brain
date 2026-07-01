package com.hospital.medicalexam.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessExceptionKeepsCustomCodeAndMessage() {
        R<Void> response = handler.handleBusinessException(new BusinessException(4101, "报告不存在"));

        assertThat(response.getCode()).isEqualTo(4101);
        assertThat(response.getMessage()).isEqualTo("报告不存在");
        assertThat(response.getData()).isNull();
    }

    @Test
    void businessExceptionDefaultsToBusinessErrorCode() {
        BusinessException exception = new BusinessException("医生类型不能为空");

        assertThat(exception.getCode()).isEqualTo(4000);
        assertThat(exception).hasMessage("医生类型不能为空");
    }
}
