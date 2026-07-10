package com.neu.patient.service;

import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.DoctorSchedule;
import com.neu.patient.mapper.DoctorScheduleMapper;
import com.neu.patient.service.impl.AppointmentScheduleRepairService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentScheduleRepairServiceTest {

    @Test
    void createsFutureSchedulesFromExpiredTemplatesWhenNoFutureQuotaExists() {
        DoctorScheduleMapper mapper = mock(DoctorScheduleMapper.class);
        when(mapper.countFutureAvailableSchedules()).thenReturn(0);
        when(mapper.findScheduleRepairTemplates()).thenReturn(List.of(template(1L, 10L, EnumValues.SCHEDULE_MORNING)));

        AppointmentScheduleRepairService service = new AppointmentScheduleRepairService(mapper);

        int created = service.repairIfNoFutureAvailableSchedules(LocalDate.of(2026, 7, 2));

        assertThat(created).isEqualTo(1);
        verify(mapper).insert(any(DoctorSchedule.class));
    }

    @Test
    void doesNothingWhenFutureQuotaAlreadyExists() {
        DoctorScheduleMapper mapper = mock(DoctorScheduleMapper.class);
        when(mapper.countFutureAvailableSchedules()).thenReturn(3);

        AppointmentScheduleRepairService service = new AppointmentScheduleRepairService(mapper);

        int created = service.repairIfNoFutureAvailableSchedules(LocalDate.of(2026, 7, 2));

        assertThat(created).isZero();
        verify(mapper, never()).findScheduleRepairTemplates();
        verify(mapper, never()).insert(any(DoctorSchedule.class));
    }

    @Test
    void usesOneTemplatePerDoctorAndTimePeriod() {
        DoctorScheduleMapper mapper = mock(DoctorScheduleMapper.class);
        when(mapper.countFutureAvailableSchedules()).thenReturn(0);
        when(mapper.findScheduleRepairTemplates()).thenReturn(List.of(
                template(1L, 10L, EnumValues.SCHEDULE_MORNING),
                template(1L, 10L, EnumValues.SCHEDULE_MORNING),
                template(1L, 10L, EnumValues.SCHEDULE_AFTERNOON)
        ));

        AppointmentScheduleRepairService service = new AppointmentScheduleRepairService(mapper);

        int created = service.repairIfNoFutureAvailableSchedules(LocalDate.of(2026, 7, 2));

        assertThat(created).isEqualTo(2);
        verify(mapper, times(2)).insert(any(DoctorSchedule.class));
    }

    private DoctorSchedule template(Long doctorId, Long deptId, String period) {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(doctorId);
        schedule.setDeptId(deptId);
        schedule.setWorkDate(LocalDate.of(2026, 6, 29));
        schedule.setTimePeriod(period);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(11, 30));
        schedule.setTotalQuota(20);
        schedule.setRemainQuota(12);
        schedule.setRegistrationFee(new BigDecimal("20.00"));
        schedule.setStatus(EnumValues.SCHEDULE_AVAILABLE);
        return schedule;
    }
}
