package com.hospital.medicalexam.domain.view;

import java.util.List;

public record ExamLabWorkbenchResponse(
        ExamLabWorkbenchStatsView stats,
        List<ExamLabWorkbenchItemView> items
) {
}
