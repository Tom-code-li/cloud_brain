SET NAMES utf8mb4;
USE doctor_platform;

SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

DELETE rr
FROM refund_record rr
JOIN fee_order fo ON fo.fee_order_id = rr.fee_order_id
JOIN registration r ON r.registration_id = fo.registration_id
WHERE r.patient_id = 41
  AND fo.business_type = 'REGISTRATION';

DELETE pr
FROM payment_record pr
JOIN fee_order fo ON fo.fee_order_id = pr.fee_order_id
JOIN registration r ON r.registration_id = fo.registration_id
WHERE r.patient_id = 41
  AND fo.business_type = 'REGISTRATION';

DELETE foi
FROM fee_order_item foi
JOIN fee_order fo ON fo.fee_order_id = foi.fee_order_id
JOIN registration r ON r.registration_id = fo.registration_id
WHERE r.patient_id = 41
  AND fo.business_type = 'REGISTRATION';

DELETE fo
FROM fee_order fo
JOIN registration r ON r.registration_id = fo.registration_id
WHERE r.patient_id = 41
  AND fo.business_type = 'REGISTRATION';

DELETE prh
FROM pharmacy_return prh
JOIN prescription p ON p.prescription_id = prh.prescription_id
JOIN outpatient_visit ov ON ov.visit_id = p.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE pd
FROM pharmacy_dispense pd
JOIN prescription p ON p.prescription_id = pd.prescription_id
JOIN outpatient_visit ov ON ov.visit_id = p.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE pi
FROM prescription_item pi
JOIN prescription p ON p.prescription_id = pi.prescription_id
JOIN outpatient_visit ov ON ov.visit_id = p.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE p
FROM prescription p
JOIN outpatient_visit ov ON ov.visit_id = p.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE lri
FROM lab_result_item lri
JOIN exam_lab_report er ON er.report_id = lri.report_id
JOIN exam_lab_order eo ON eo.order_id = er.order_id
JOIN outpatient_visit ov ON ov.visit_id = eo.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE erf
FROM exam_result_feature erf
JOIN exam_lab_report er ON er.report_id = erf.report_id
JOIN exam_lab_order eo ON eo.order_id = er.order_id
JOIN outpatient_visit ov ON ov.visit_id = eo.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE er
FROM exam_lab_report er
JOIN exam_lab_order eo ON eo.order_id = er.order_id
JOIN outpatient_visit ov ON ov.visit_id = eo.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE eoi
FROM exam_lab_order_item eoi
JOIN exam_lab_order eo ON eo.order_id = eoi.order_id
JOIN outpatient_visit ov ON ov.visit_id = eo.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE eo
FROM exam_lab_order eo
JOIN outpatient_visit ov ON ov.visit_id = eo.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE mr
FROM medical_record mr
JOIN outpatient_visit ov ON ov.visit_id = mr.visit_id
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE tr
FROM triage_record tr
JOIN registration r ON r.registration_id = tr.registration_id
WHERE r.patient_id = 41;

DELETE ov
FROM outpatient_visit ov
JOIN registration r ON r.registration_id = ov.registration_id
WHERE r.patient_id = 41;

DELETE r
FROM registration r
WHERE r.patient_id = 41;

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
