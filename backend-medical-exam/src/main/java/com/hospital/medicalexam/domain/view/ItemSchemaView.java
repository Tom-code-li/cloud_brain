package com.hospital.medicalexam.domain.view;

import java.util.List;

public record ItemSchemaView(
        String itemName,
        String schemaType,
        List<ItemFieldDefView> fields,
        List<String> findingOptions,
        List<String> rhythmOptions,
        List<String> axisOptions
) {
}