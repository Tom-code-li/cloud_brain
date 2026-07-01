package com.hospital.medicalexam;

import com.hospital.medicalexam.common.R;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonResponseTest {
    @Test
    void okResponseUsesStableShape() {
        R<String> response = R.ok("done");

        assertThat(response.getCode()).isZero();
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("done");
    }
}
