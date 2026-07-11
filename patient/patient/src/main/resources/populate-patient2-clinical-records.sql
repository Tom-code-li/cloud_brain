SET NAMES utf8mb4;
USE doctor_platform;

START TRANSACTION;

UPDATE registration
SET status = '已完成',
    updated_at = NOW()
WHERE registration_id = 2069399195437027344;

INSERT INTO outpatient_visit (
  registration_id, patient_id, doctor_id, dept_id, visit_no, queue_no,
  status, started_at, finished_at
)
VALUES (
  2069399195437027344, 2, 1, 1, 'V20260624004', 2,
  '已完成', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE)
);

SET @visit_id = (SELECT visit_id FROM outpatient_visit WHERE visit_no = 'V20260624004');

INSERT INTO medical_record (
  visit_id, patient_id, doctor_id, chief_complaint, present_illness,
  past_history, allergy_history, physical_exam, auxiliary_exam, diagnosis,
  treatment_advice, doctor_note, status, initial_saved_at, completed_at
)
VALUES (
  @visit_id, 2, 1, '咳嗽咽痛 2 天',
  '患者 2 天前受凉后出现咳嗽、咽痛，伴轻度乏力，无明显发热，精神可。',
  '既往体健，无特殊既往史。',
  '青霉素过敏',
  '体温 36.8℃，咽部轻度充血，双肺呼吸音清。',
  '血常规：白细胞轻度升高。',
  '急性上呼吸道感染',
  '多饮水，注意休息，清淡饮食，按医嘱用药，症状加重及时复诊。',
  '门诊随诊',
  '已完成', DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE)
);

SET @record_id = (
  SELECT record_id FROM medical_record
  WHERE visit_id = @visit_id AND patient_id = 2
  ORDER BY record_id DESC LIMIT 1
);

INSERT INTO exam_lab_order (
  order_no, visit_id, record_id, patient_id, apply_doctor_id, execute_dept_id,
  order_type, clinical_diagnosis, purpose, total_amount, fee_status, status, applied_at, executed_at, completed_at
)
VALUES (
  'EO20260624004', @visit_id, @record_id, 2, 1, 4,
  '检验', '急性上呼吸道感染', '判断感染及炎症程度', 25.00, '已支付', '已完成',
  DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE)
);

SET @exam_order_id = (SELECT order_id FROM exam_lab_order WHERE order_no = 'EO20260624004');

INSERT INTO exam_lab_order_item (
  order_id, item_id, item_name, item_type, unit_price, quantity, amount, status, executed_at, result_summary
)
VALUES (
  @exam_order_id, 1, '血常规', '检验', 25.00, 1, 25.00,
  '已完成', DATE_SUB(NOW(), INTERVAL 15 MINUTE), '白细胞 10.2×10^9/L，中性粒细胞比例轻度升高。'
);

SET @exam_order_item_id = (
  SELECT order_item_id FROM exam_lab_order_item
  WHERE order_id = @exam_order_id AND item_id = 1
  ORDER BY order_item_id DESC LIMIT 1
);

INSERT INTO exam_lab_report (
  order_id, order_item_id, patient_id, report_doctor_id, report_no, report_type,
  findings, conclusion, ai_draft, doctor_review, status, published_at
)
VALUES (
  @exam_order_id, @exam_order_item_id, 2, 1,
  'RP20260624004', '检验',
  '白细胞 10.2×10^9/L，中性粒细胞比例轻度升高。',
  '考虑轻度感染，建议对症治疗并注意休息。',
  'AI 提示：感染指标轻度升高。',
  '审核通过，结合临床处理。', '已发布', DATE_SUB(NOW(), INTERVAL 2 MINUTE)
);

INSERT INTO prescription (
  prescription_no, visit_id, record_id, patient_id, doctor_id, total_amount,
  fee_status, audit_status, status, diagnosis, usage_note, audit_doctor_id, audit_note, audited_at
)
VALUES (
  'PR20260624004', @visit_id, @record_id, 2, 1, 46.00,
  '已支付', '审核通过', '已发药', '急性上呼吸道感染',
  '按医嘱服用，若出现高热、气促或症状加重请及时复诊。',
  1, '审核通过', DATE_SUB(NOW(), INTERVAL 15 MINUTE)
);

SET @prescription_id = (SELECT prescription_id FROM prescription WHERE prescription_no = 'PR20260624004');

INSERT INTO prescription_item (
  prescription_id, drug_id, drug_name, specification, unit_price, quantity,
  amount, dosage, frequency, usage_method, days, status
)
VALUES
  (@prescription_id, 1, '复方氨酚烷胺片', '0.3g*48片', 28.00, 1, 28.00, '1片', '每日3次', '口服', 3, '已发药'),
  (@prescription_id, 2, '维生素C片', '10mg*12片', 18.00, 1, 18.00, '1片', '每日1次', '口服', 5, '已发药');

COMMIT;
