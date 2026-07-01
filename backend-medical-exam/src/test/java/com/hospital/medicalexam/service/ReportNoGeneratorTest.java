package com.hospital.medicalexam.service;

import com.hospital.medicalexam.entity.ExamLabReportEntity;
import com.hospital.medicalexam.mapper.ExamLabReportMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportNoGeneratorTest {
    @Mock
    private ExamLabReportMapper reportMapper;

    @Test
    void nextStartsAtFirstSequenceWhenNoReportExistsToday() {
        when(reportMapper.selectOne(any())).thenReturn(null);

        String reportNo = new ReportNoGenerator(reportMapper).next();

        assertThat(reportNo).isEqualTo(todayPrefix() + "0001");
    }

    @Test
    void nextIncrementsLatestDailySequence() {
        when(reportMapper.selectOne(any())).thenReturn(report(todayPrefix() + "0019"));

        String reportNo = new ReportNoGenerator(reportMapper).next();

        assertThat(reportNo).isEqualTo(todayPrefix() + "0020");
    }

    @Test
    void nextTreatsMalformedExistingSequenceAsZero() {
        when(reportMapper.selectOne(any())).thenReturn(report(todayPrefix() + "LAST"));

        String reportNo = new ReportNoGenerator(reportMapper).next();

        assertThat(reportNo).isEqualTo(todayPrefix() + "0001");
    }

    private ExamLabReportEntity report(String reportNo) {
        ExamLabReportEntity entity = new ExamLabReportEntity();
        entity.setReportNo(reportNo);
        return entity;
    }

    private String todayPrefix() {
        return "REP" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
