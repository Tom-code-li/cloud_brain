package com.neu.patient.config;

import com.neu.patient.service.impl.AppointmentScheduleRepairService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AppointmentScheduleRepairRunner implements ApplicationRunner {
    private final AppointmentScheduleRepairService appointmentScheduleRepairService;

    public AppointmentScheduleRepairRunner(AppointmentScheduleRepairService appointmentScheduleRepairService) {
        this.appointmentScheduleRepairService = appointmentScheduleRepairService;
    }

    @Override
    public void run(ApplicationArguments args) {
        appointmentScheduleRepairService.repairIfNoFutureAvailableSchedules(LocalDate.now());
    }
}
