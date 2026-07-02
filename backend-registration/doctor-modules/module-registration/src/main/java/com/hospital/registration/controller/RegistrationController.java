package com.hospital.registration.controller;

import com.hospital.common.core.R;
import com.hospital.registration.domain.DepartmentView;
import com.hospital.registration.domain.DoctorView;
import com.hospital.registration.domain.DoctorScheduleView;
import com.hospital.registration.domain.FeeChargeRequest;
import com.hospital.registration.domain.FeeHistoryView;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.OfflineRegisterRequest;
import com.hospital.registration.domain.OnlineRegisterRequest;
import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.PaymentSubmitRequest;
import com.hospital.registration.domain.RefundCheckView;
import com.hospital.registration.domain.RefundRequest;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.service.RegistrationWorkflowService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/registration")
public class RegistrationController {
    private final RegistrationWorkflowService registrationWorkflowService;

    public RegistrationController(RegistrationWorkflowService registrationWorkflowService) {
        this.registrationWorkflowService = registrationWorkflowService;
    }

    @PostMapping("/patient/sync")
    public R<PatientView> syncPatient(@RequestBody PatientSyncRequest request) {
        return R.ok(registrationWorkflowService.syncPatient(request));
    }

    @GetMapping("/departments")
    public R<List<DepartmentView>> departments() {
        return R.ok(registrationWorkflowService.listDepartments());
    }

    @GetMapping("/doctors")
    public R<List<DoctorView>> doctors(@RequestParam(value = "deptId", required = false) Long deptId) {
        return R.ok(registrationWorkflowService.listDoctors(deptId));
    }

    @GetMapping("/schedules")
    public R<List<DoctorScheduleView>> schedules(
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "workDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate
    ) {
        return R.ok(registrationWorkflowService.listSchedules(deptId, doctorId, workDate));
    }

    @PostMapping("/offline/submit")
    public R<RegistrationView> submitOffline(@RequestBody OfflineRegisterRequest request) {
        return R.ok(registrationWorkflowService.submitOfflineRegistration(
                request.patientId(),
                request.doctorId(),
                request.scheduleId()
        ));
    }

    @PostMapping("/fee/charge")
    public R<RegistrationView> charge(@RequestBody PaymentSubmitRequest request) {
        return R.ok(registrationWorkflowService.chargeRegistration(request.registrationId(), request.payMethod()));
    }

    @PostMapping("/fee/order/charge")
    public R<FeeOrderView> chargeFeeOrder(@RequestBody FeeChargeRequest request) {
        return R.ok(registrationWorkflowService.chargeFeeOrder(request.feeOrderId(), request.payMethod()));
    }

    @PostMapping("/queue/call")
    public R<RegistrationView> call(
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "scheduleId", required = false) Long scheduleId,
            @RequestParam(value = "workDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate
    ) {
        return R.ok(registrationWorkflowService.callNext(doctorId, scheduleId, workDate));
    }

    @GetMapping("/queue")
    public R<List<RegistrationView>> queue(
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "scheduleId", required = false) Long scheduleId,
            @RequestParam(value = "workDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate
    ) {
        return R.ok(registrationWorkflowService.listQueue(doctorId, scheduleId, workDate));
    }

    @GetMapping("/online/pending")
    public R<List<RegistrationView>> onlinePending() {
        return R.ok(registrationWorkflowService.listOnlinePending());
    }

    @PostMapping("/online/submit")
    public R<RegistrationView> submitOnline(@RequestBody OnlineRegisterRequest request) {
        return R.ok(registrationWorkflowService.submitOnlineRegistration(
                request.patientId(),
                request.doctorId(),
                request.scheduleId(),
                request.paid()
        ));
    }

    @PutMapping("/online/confirm")
    public R<RegistrationView> confirmOnline(@RequestParam("registrationId") Long registrationId) {
        return R.ok(registrationWorkflowService.confirmOnlineRegistration(registrationId));
    }

    @GetMapping("/fee/pending")
    public R<List<FeeOrderView>> pendingFees(
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam(value = "registrationId", required = false) Long registrationId
    ) {
        return R.ok(registrationWorkflowService.listPendingFees(patientId, registrationId));
    }

    @GetMapping("/fee/history")
    public R<FeeHistoryView> feeHistory(
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam(value = "registrationId", required = false) Long registrationId
    ) {
        return R.ok(registrationWorkflowService.feeHistory(patientId, registrationId));
    }

    @GetMapping("/fee/refund/check")
    public R<RefundCheckView> refundCheck(@RequestParam("feeOrderId") Long feeOrderId) {
        return R.ok(registrationWorkflowService.checkRefund(feeOrderId));
    }

    @PostMapping("/fee/refund")
    public R<FeeOrderView> refund(@RequestBody RefundRequest request) {
        return R.ok(registrationWorkflowService.refund(request.feeOrderId(), request.reason()));
    }
}
