package com.hospital.common.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {
    @Test
    void successWrapsDataWithCodeZero() {
        R<String> result = R.ok("hello");

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo("hello");
    }

    @Test
    void failureWrapsBusinessMessage() {
        R<Void> result = R.fail(4001, "检查项目未缴费，暂不可执行");

        assertThat(result.getCode()).isEqualTo(4001);
        assertThat(result.getMessage()).isEqualTo("检查项目未缴费，暂不可执行");
        assertThat(result.getData()).isNull();
    }
}
