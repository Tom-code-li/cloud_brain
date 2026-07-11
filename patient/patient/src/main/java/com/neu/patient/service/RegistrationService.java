package com.neu.patient.service;

import com.neu.patient.entity.*;
import java.util.List;

public interface RegistrationService {
    List<Department> getAllDepartments();
    List<Doctor> getDoctorsByDept(Long deptId);
    List<DoctorSchedule> getDoctorSchedules(Long doctorId);
    Registration registerAppointment(Registration reg);
    List<Registration> getMyRegistrations(Long patientId);
    boolean cancelRegistration(Long registrationId);
}
