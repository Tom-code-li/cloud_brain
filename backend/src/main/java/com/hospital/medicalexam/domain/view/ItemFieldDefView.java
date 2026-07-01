package com.hospital.medicalexam.domain.view;

import java.util.List;

public record ItemFieldDefView(
        String key,
        String label,
        String unit,
        String type,
        String low,
        String high,
        String def,
        List<String> options,
        String normal
) {
}
