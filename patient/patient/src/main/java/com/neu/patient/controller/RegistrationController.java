package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.entity.*;
import com.neu.patient.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {
    @Autowired private RegistrationService registrationService;

    @GetMapping("/departments")
    public Result<List<Department>> getDepartments() {
        return Result.ok(registrationService.getAllDepartments());
    }

    @GetMapping("/doctors/{deptId}")
    public Result<List<Doctor>> getDoctors(@PathVariable Long deptId) {
        return Result.ok(registrationService.getDoctorsByDept(deptId));
    }

    @GetMapping("/schedules/{doctorId}")
    public Result<List<DoctorSchedule>> getSchedules(@PathVariable Long doctorId) {
        return Result.ok(registrationService.getDoctorSchedules(doctorId));
    }

    @PostMapping("/book")
    public Result<Registration> book(@RequestBody Registration reg) {
        try {
            Registration result = registrationService.registerAppointment(reg);
            return Result.ok("挂号成功", result);
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/my/{patientId}")
    public Result<List<Registration>> myRegistrations(@PathVariable Long patientId) {
        return Result.ok(registrationService.getMyRegistrations(patientId));
    }

    @PostMapping("/cancel/{registrationId}")
    public Result<?> cancel(@PathVariable Long registrationId) {
        return registrationService.cancelRegistration(registrationId)
                ? Result.ok("取消成功") : Result.fail("取消失败");
    }
}
