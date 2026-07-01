package com.hospital.medicalexam.domain.view;

public record ExamLabWorkbenchStatsView(
        long allCount,
        long pendingCount,
        long progressCount,
        long publishedCount,
        long activeCount
) {
}
