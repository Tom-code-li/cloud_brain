package com.hospital.medicalexam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.medicalexam.entity.ExamLabReportEntity;
import com.hospital.medicalexam.mapper.ExamLabReportMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ReportNoGenerator {
    private static final String PREFIX = "REP";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int SEQUENCE_LENGTH = 4;
    private final ExamLabReportMapper reportMapper;

    public ReportNoGenerator(ExamLabReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    public synchronized String next() {
        String todayPrefix = PREFIX + LocalDate.now().format(DATE_FORMATTER);
        ExamLabReportEntity latest = reportMapper.selectOne(new LambdaQueryWrapper<ExamLabReportEntity>()
                .select(ExamLabReportEntity::getReportNo)
                .likeRight(ExamLabReportEntity::getReportNo, todayPrefix)
                .orderByDesc(ExamLabReportEntity::getReportNo)
                .last("LIMIT 1"));
        int nextSequence = latest == null ? 1 : parseSequence(latest.getReportNo(), todayPrefix) + 1;
        return todayPrefix + String.format("%0" + SEQUENCE_LENGTH + "d", nextSequence);
    }

    private int parseSequence(String reportNo, String todayPrefix) {
        if (reportNo == null || reportNo.length() <= todayPrefix.length()) {
            return 0;
        }
        try {
            return Integer.parseInt(reportNo.substring(todayPrefix.length()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
