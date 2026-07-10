package com.neu.patient.service.impl;

import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.DoctorSchedule;
import com.neu.patient.mapper.DoctorScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentScheduleRepairService {
    private final DoctorScheduleMapper doctorScheduleMapper;

    public AppointmentScheduleRepairService(DoctorScheduleMapper doctorScheduleMapper) {
        this.doctorScheduleMapper = doctorScheduleMapper;
    }

    @Transactional
    public int repairIfNoFutureAvailableSchedules(LocalDate today) {
        if (doctorScheduleMapper.countFutureAvailableSchedules() > 0) {
            return 0;
        }

        List<DoctorSchedule> templates = doctorScheduleMapper.findScheduleRepairTemplates();
        Set<String> usedTemplates = new HashSet<>();
        int created = 0;

        for (DoctorSchedule template : templates) {
            String key = template.getDoctorId() + ":" + template.getTimePeriod();
            if (!usedTemplates.add(key)) {
                continue;
            }

            DoctorSchedule schedule = copyAsFutureSchedule(template, today.plusDays((created % 7) + 1));
            doctorScheduleMapper.insert(schedule);
            created++;
        }

        return created;
    }

    private DoctorSchedule copyAsFutureSchedule(DoctorSchedule template, LocalDate workDate) {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(template.getDoctorId());
        schedule.setDeptId(template.getDeptId());
        schedule.setWorkDate(workDate);
        schedule.setTimePeriod(toStandardTimePeriod(template.getTimePeriod()));
        schedule.setStartTime(template.getStartTime());
        schedule.setEndTime(template.getEndTime());
        schedule.setTotalQuota(defaultQuota(template.getTotalQuota()));
        schedule.setRemainQuota(defaultQuota(template.getTotalQuota()));
        schedule.setRegistrationFee(template.getRegistrationFee());
        schedule.setStatus(EnumValues.SCHEDULE_AVAILABLE);
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());
        return schedule;
    }

    private int defaultQuota(Integer totalQuota) {
        return totalQuota == null || totalQuota <= 0 ? 20 : totalQuota;
    }

    private String toStandardTimePeriod(String timePeriod) {
        if ("morning".equals(timePeriod)) {
            return EnumValues.SCHEDULE_MORNING;
        }
        if ("afternoon".equals(timePeriod)) {
            return EnumValues.SCHEDULE_AFTERNOON;
        }
        if ("night".equals(timePeriod)) {
            return EnumValues.SCHEDULE_NIGHT;
        }
        return timePeriod;
    }
}
