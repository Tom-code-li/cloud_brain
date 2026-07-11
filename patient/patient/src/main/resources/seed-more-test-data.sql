SET NAMES utf8mb4;
USE doctor_platform;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM prescription_item
WHERE prescription_id IN (
  SELECT prescription_id FROM prescription
  WHERE prescription_no IN ('PR20260623002', 'PR20260623003')
);
DELETE FROM prescription WHERE prescription_no IN ('PR20260623002', 'PR20260623003');
DELETE FROM drug WHERE drug_code IN ('DRUG003', 'DRUG004', 'DRUG005');

DELETE FROM exam_lab_report WHERE report_no IN ('RP20260623002', 'RP20260623003');
DELETE FROM exam_lab_order_item
WHERE order_id IN (
  SELECT order_id FROM exam_lab_order
  WHERE order_no IN ('EO20260623002', 'EO20260623003')
);
DELETE FROM exam_lab_order WHERE order_no IN ('EO20260623002', 'EO20260623003');
DELETE FROM medical_item WHERE item_code IN ('ITEM002', 'ITEM003');

DELETE FROM medical_record
WHERE visit_id IN (
  SELECT visit_id FROM outpatient_visit
  WHERE visit_no IN ('V20260623002', 'V20260623003')
);
DELETE FROM outpatient_visit WHERE visit_no IN ('V20260623002', 'V20260623003');

DELETE FROM fee_order WHERE order_no IN ('FO20260623003', 'FO20260623004', 'FO20260623005');
DELETE FROM registration WHERE registration_no IN ('RG20260623003', 'RG20260623004', 'RG20260623005');

DELETE FROM doctor_schedule
WHERE doctor_id IN (
  SELECT doctor_id FROM doctor
  WHERE doctor_no IN ('D2026004', 'D2026005', 'D2026006')
);
DELETE FROM doctor WHERE doctor_no IN ('D2026004', 'D2026005', 'D2026006');
DELETE FROM department WHERE dept_code IN ('DEPT005', 'DEPT006', 'DEPT007');
DELETE FROM sys_user WHERE username IN ('doctor_zhao', 'doctor_sun', 'doctor_luo');

SET FOREIGN_KEY_CHECKS = 1;

SET @doctor_role_id = (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR');
SET @patient_id = (SELECT patient_id FROM patient WHERE patient_no = 'P20260623001');

INSERT INTO sys_user (role_id, username, password, real_name, phone, email, status)
VALUES
  (@doctor_role_id, 'doctor_zhao', '123456', '赵强', '13800001004', 'zhao.qiang@example.com', 1),
  (@doctor_role_id, 'doctor_sun', '123456', '孙悦', '13800001005', 'sun.yue@example.com', 1),
  (@doctor_role_id, 'doctor_luo', '123456', '罗宁', '13800001006', 'luo.ning@example.com', 1)
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  email = VALUES(email),
  status = VALUES(status);

SET @doctor_zhao_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_zhao');
SET @doctor_sun_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_sun');
SET @doctor_luo_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_luo');

INSERT INTO department (dept_code, dept_name, dept_type, location, description, status)
VALUES
  ('DEPT005', '儿科', '临床', '门诊楼 3 层 B 区', '儿童发热、咳嗽、腹泻及生长发育咨询', 1),
  ('DEPT006', '皮肤科', '临床', '门诊楼 4 层 A 区', '皮炎、湿疹、痤疮、过敏性皮肤病诊疗', 1),
  ('DEPT007', '骨科', '临床', '门诊楼 4 层 B 区', '关节疼痛、扭伤、骨折复查及康复建议', 1)
ON DUPLICATE KEY UPDATE
  dept_name = VALUES(dept_name),
  dept_type = VALUES(dept_type),
  location = VALUES(location),
  description = VALUES(description),
  status = VALUES(status);

SET @pediatrics_dept_id = (SELECT dept_id FROM department WHERE dept_code = 'DEPT005');
SET @skin_dept_id = (SELECT dept_id FROM department WHERE dept_code = 'DEPT006');
SET @ortho_dept_id = (SELECT dept_id FROM department WHERE dept_code = 'DEPT007');

INSERT INTO doctor (
  user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status
)
VALUES
  (@doctor_zhao_user_id, @pediatrics_dept_id, 'D2026004', '专家门诊', '主任医师', '儿童呼吸道感染、儿童哮喘', '长期从事儿科常见病诊疗和儿童慢病管理。', 1),
  (@doctor_sun_user_id, @skin_dept_id, 'D2026005', '普通门诊', '主治医师', '湿疹、荨麻疹、痤疮', '擅长过敏性皮肤病和皮肤屏障修复指导。', 1),
  (@doctor_luo_user_id, @ortho_dept_id, 'D2026006', '专家门诊', '副主任医师', '运动损伤、关节疼痛、骨折复查', '关注骨科康复和运动损伤评估。', 1)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dept_id = VALUES(dept_id),
  doctor_type = VALUES(doctor_type),
  title = VALUES(title),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  status = VALUES(status);

SET @doctor_zhao_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026004');
SET @doctor_sun_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026005');
SET @doctor_luo_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026006');

INSERT INTO doctor_schedule (
  doctor_id, dept_id, work_date, time_period, start_time, end_time,
  total_quota, remain_quota, registration_fee, status
)
VALUES
  (@doctor_zhao_id, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 20, 7, 35.00, '可预约'),
  (@doctor_zhao_id, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 20, 14, 35.00, '可预约'),
  (@doctor_sun_id, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 30, 24, 15.00, '可预约'),
  (@doctor_sun_id, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 25, 20, 15.00, '可预约'),
  (@doctor_luo_id, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '08:00:00', '11:30:00', 18, 6, 30.00, '可预约'),
  (@doctor_luo_id, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '下午', '14:00:00', '17:00:00', 18, 11, 30.00, '可预约')
ON DUPLICATE KEY UPDATE
  total_quota = VALUES(total_quota),
  remain_quota = VALUES(remain_quota),
  registration_fee = VALUES(registration_fee),
  status = VALUES(status);

SET @schedule_sun_morning = (
  SELECT schedule_id FROM doctor_schedule
  WHERE doctor_id = @doctor_sun_id AND work_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY) AND time_period = '上午'
);
SET @schedule_luo_morning = (
  SELECT schedule_id FROM doctor_schedule
  WHERE doctor_id = @doctor_luo_id AND work_date = DATE_ADD(CURDATE(), INTERVAL 3 DAY) AND time_period = '上午'
);
SET @schedule_zhao_afternoon = (
  SELECT schedule_id FROM doctor_schedule
  WHERE doctor_id = @doctor_zhao_id AND work_date = DATE_ADD(CURDATE(), INTERVAL 4 DAY) AND time_period = '下午'
);

INSERT INTO registration (
  patient_id, dept_id, doctor_id, schedule_id, source, registration_no, queue_no,
  registration_fee, fee_status, status, registered_at
)
VALUES
  (@patient_id, @skin_dept_id, @doctor_sun_id, @schedule_sun_morning, '线上', 'RG20260623003', 12, 15.00, '待支付', '待支付', NOW()),
  (@patient_id, @ortho_dept_id, @doctor_luo_id, @schedule_luo_morning, '线上', 'RG20260623004', 5, 30.00, '已支付', '已完成', DATE_SUB(NOW(), INTERVAL 18 DAY)),
  (@patient_id, @pediatrics_dept_id, @doctor_zhao_id, @schedule_zhao_afternoon, '线上', 'RG20260623005', 10, 35.00, '待支付', '已取消', DATE_SUB(NOW(), INTERVAL 2 DAY))
ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  dept_id = VALUES(dept_id),
  doctor_id = VALUES(doctor_id),
  schedule_id = VALUES(schedule_id),
  queue_no = VALUES(queue_no),
  registration_fee = VALUES(registration_fee),
  fee_status = VALUES(fee_status),
  status = VALUES(status),
  registered_at = VALUES(registered_at);

SET @reg_skin_id = (SELECT registration_id FROM registration WHERE registration_no = 'RG20260623003');
SET @reg_ortho_id = (SELECT registration_id FROM registration WHERE registration_no = 'RG20260623004');
SET @reg_cancelled_id = (SELECT registration_id FROM registration WHERE registration_no = 'RG20260623005');

INSERT INTO fee_order (
  order_no, patient_id, registration_id, business_type, business_id,
  total_amount, paid_amount, refund_amount, status, created_by, paid_at
)
VALUES
  ('FO20260623003', @patient_id, @reg_skin_id, 'REGISTRATION', @reg_skin_id, 15.00, 0.00, 0.00, '待支付', @doctor_sun_user_id, NULL),
  ('FO20260623004', @patient_id, @reg_ortho_id, 'REGISTRATION', @reg_ortho_id, 30.00, 30.00, 0.00, '已支付', @doctor_luo_user_id, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  ('FO20260623005', @patient_id, @reg_ortho_id, 'EXAM_LAB_ORDER', @reg_ortho_id, 86.00, 86.00, 0.00, '已支付', @doctor_luo_user_id, DATE_SUB(NOW(), INTERVAL 18 DAY))
ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  registration_id = VALUES(registration_id),
  business_type = VALUES(business_type),
  business_id = VALUES(business_id),
  total_amount = VALUES(total_amount),
  paid_amount = VALUES(paid_amount),
  refund_amount = VALUES(refund_amount),
  status = VALUES(status),
  created_by = VALUES(created_by),
  paid_at = VALUES(paid_at);

INSERT INTO outpatient_visit (
  registration_id, patient_id, doctor_id, dept_id, visit_no, queue_no,
  status, started_at, finished_at
)
VALUES
  (@reg_ortho_id, @patient_id, @doctor_luo_id, @ortho_dept_id, 'V20260623002', 5, '已完成', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 35 MINUTE),
  (@reg_skin_id, @patient_id, @doctor_sun_id, @skin_dept_id, 'V20260623003', 12, 'waiting', NULL, NULL)
ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  dept_id = VALUES(dept_id),
  queue_no = VALUES(queue_no),
  status = VALUES(status),
  started_at = VALUES(started_at),
  finished_at = VALUES(finished_at);

SET @visit_ortho_id = (SELECT visit_id FROM outpatient_visit WHERE visit_no = 'V20260623002');
SET @visit_skin_id = (SELECT visit_id FROM outpatient_visit WHERE visit_no = 'V20260623003');

INSERT INTO medical_record (
  visit_id, patient_id, doctor_id, chief_complaint, present_illness,
  past_history, allergy_history, physical_exam, auxiliary_exam, diagnosis,
  treatment_advice, doctor_note, status, initial_saved_at, completed_at
)
VALUES
  (@visit_ortho_id, @patient_id, @doctor_luo_id, '右踝扭伤后疼痛 2 天', '运动后右踝外侧疼痛，活动时加重，局部轻度肿胀。', '无骨折史', '青霉素过敏', '右踝外侧压痛，活动受限，末梢血运可。', '踝关节 X 线未见明显骨折征象。', '右踝软组织扭伤', '减少负重，冰敷，抬高患肢，必要时佩戴护踝。', '两周后复查', '已完成', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 30 MINUTE),
  (@visit_skin_id, @patient_id, @doctor_sun_id, '面部皮疹伴瘙痒 1 周', '面部反复红斑、瘙痒，近期更换护肤品后加重。', '无特殊既往史', '青霉素过敏', '面颊散在红斑，轻度脱屑。', '暂未检查', '过敏性皮炎待查', '停用可疑护肤品，避免搔抓，按医嘱外用药。', '待复诊完善记录', '草稿', NOW(), NULL)
ON DUPLICATE KEY UPDATE
  chief_complaint = VALUES(chief_complaint),
  present_illness = VALUES(present_illness),
  diagnosis = VALUES(diagnosis),
  treatment_advice = VALUES(treatment_advice),
  doctor_note = VALUES(doctor_note),
  status = VALUES(status),
  completed_at = VALUES(completed_at);

SET @record_ortho_id = (
  SELECT record_id FROM medical_record WHERE visit_id = @visit_ortho_id ORDER BY record_id DESC LIMIT 1
);

INSERT INTO medical_item (item_code, item_name, item_type, dept_id, unit, price, sample_type, clinical_meaning, status)
VALUES
  ('ITEM002', '踝关节 X 线', '检查', @ortho_dept_id, '次', 86.00, '影像', '辅助判断骨折、脱位等情况', 1),
  ('ITEM003', '过敏原筛查', '检验', @skin_dept_id, '次', 168.00, '静脉血', '辅助判断常见过敏原', 1)
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  item_type = VALUES(item_type),
  dept_id = VALUES(dept_id),
  unit = VALUES(unit),
  price = VALUES(price),
  sample_type = VALUES(sample_type),
  clinical_meaning = VALUES(clinical_meaning),
  status = VALUES(status);

SET @item_xray_id = (SELECT item_id FROM medical_item WHERE item_code = 'ITEM002');
SET @item_allergy_id = (SELECT item_id FROM medical_item WHERE item_code = 'ITEM003');

INSERT INTO exam_lab_order (
  order_no, visit_id, record_id, patient_id, apply_doctor_id, execute_dept_id,
  order_type, clinical_diagnosis, purpose, total_amount, fee_status, status, applied_at, executed_at, completed_at
)
VALUES
  ('EO20260623002', @visit_ortho_id, @record_ortho_id, @patient_id, @doctor_luo_id, @ortho_dept_id, '检查', '右踝软组织扭伤', '排除骨折', 86.00, '已支付', '已完成', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 40 MINUTE, DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 100 MINUTE),
  ('EO20260623003', @visit_skin_id, NULL, @patient_id, @doctor_sun_id, @skin_dept_id, '检验', '过敏性皮炎待查', '筛查常见过敏原', 168.00, '待支付', '待缴费', NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE
  visit_id = VALUES(visit_id),
  record_id = VALUES(record_id),
  patient_id = VALUES(patient_id),
  apply_doctor_id = VALUES(apply_doctor_id),
  execute_dept_id = VALUES(execute_dept_id),
  total_amount = VALUES(total_amount),
  fee_status = VALUES(fee_status),
  status = VALUES(status),
  completed_at = VALUES(completed_at);

SET @exam_xray_order_id = (SELECT order_id FROM exam_lab_order WHERE order_no = 'EO20260623002');
SET @exam_allergy_order_id = (SELECT order_id FROM exam_lab_order WHERE order_no = 'EO20260623003');

INSERT INTO exam_lab_order_item (
  order_id, item_id, item_name, item_type, unit_price, quantity, amount, status, executed_at, result_summary
)
VALUES
  (@exam_xray_order_id, @item_xray_id, '踝关节 X 线', '检查', 86.00, 1.00, 86.00, '已完成', DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 55 MINUTE, '未见明显骨折征象'),
  (@exam_allergy_order_id, @item_allergy_id, '过敏原筛查', '检验', 168.00, 1.00, 168.00, '待缴费', NULL, '待执行')
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  item_type = VALUES(item_type),
  unit_price = VALUES(unit_price),
  quantity = VALUES(quantity),
  amount = VALUES(amount),
  status = VALUES(status),
  result_summary = VALUES(result_summary);

SET @exam_xray_item_id = (
  SELECT order_item_id FROM exam_lab_order_item
  WHERE order_id = @exam_xray_order_id AND item_id = @item_xray_id
  ORDER BY order_item_id DESC LIMIT 1
);
SET @exam_allergy_item_id = (
  SELECT order_item_id FROM exam_lab_order_item
  WHERE order_id = @exam_allergy_order_id AND item_id = @item_allergy_id
  ORDER BY order_item_id DESC LIMIT 1
);

INSERT INTO exam_lab_report (
  order_id, order_item_id, patient_id, report_doctor_id, report_no, report_type,
  findings, conclusion, ai_draft, doctor_review, status, published_at
)
VALUES
  (@exam_xray_order_id, @exam_xray_item_id, @patient_id, @doctor_luo_id, 'RP20260623002', '检查', '右踝关节对位关系可，未见明显骨折线。', '右踝未见明显骨折征象，建议结合临床随访。', 'AI 提示：未见明显骨折。', '审核通过。', '已发布', DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 120 MINUTE),
  (@exam_allergy_order_id, @exam_allergy_item_id, @patient_id, @doctor_sun_id, 'RP20260623003', '检验', '待检测。', '报告待发布。', NULL, NULL, '草稿', NULL)
ON DUPLICATE KEY UPDATE
  order_id = VALUES(order_id),
  order_item_id = VALUES(order_item_id),
  patient_id = VALUES(patient_id),
  report_doctor_id = VALUES(report_doctor_id),
  report_type = VALUES(report_type),
  findings = VALUES(findings),
  conclusion = VALUES(conclusion),
  ai_draft = VALUES(ai_draft),
  doctor_review = VALUES(doctor_review),
  status = VALUES(status),
  published_at = VALUES(published_at);

INSERT INTO drug (
  drug_code, drug_name, specification, dosage_form, manufacturer, unit,
  sale_price, stock_quantity, warning_quantity, contraindication, status
)
VALUES
  ('DRUG003', '双氯芬酸二乙胺乳胶剂', '20g', '外用乳胶剂', '示例制药有限公司', '支', 32.00, 60.00, 10.00, '皮肤破损处禁用', 1),
  ('DRUG004', '炉甘石洗剂', '100ml', '洗剂', '示例制药有限公司', '瓶', 12.00, 120.00, 20.00, '对本品过敏者禁用', 1),
  ('DRUG005', '维生素C片', '100mg*100片', '片剂', '示例制药有限公司', '瓶', 9.50, 200.00, 30.00, '按说明服用', 1)
ON DUPLICATE KEY UPDATE
  drug_name = VALUES(drug_name),
  specification = VALUES(specification),
  dosage_form = VALUES(dosage_form),
  manufacturer = VALUES(manufacturer),
  unit = VALUES(unit),
  sale_price = VALUES(sale_price),
  stock_quantity = VALUES(stock_quantity),
  warning_quantity = VALUES(warning_quantity),
  contraindication = VALUES(contraindication),
  status = VALUES(status);

SET @drug_pain_id = (SELECT drug_id FROM drug WHERE drug_code = 'DRUG003');
SET @drug_skin_id = (SELECT drug_id FROM drug WHERE drug_code = 'DRUG004');
SET @drug_vc_id = (SELECT drug_id FROM drug WHERE drug_code = 'DRUG005');

INSERT INTO prescription (
  prescription_no, visit_id, record_id, patient_id, doctor_id, total_amount,
  fee_status, audit_status, status, diagnosis, usage_note, audit_doctor_id, audit_note, audited_at
)
VALUES
  ('PR20260623002', @visit_ortho_id, @record_ortho_id, @patient_id, @doctor_luo_id, 32.00, '已支付', '审核通过', '已发药', '右踝软组织扭伤', '外用药请避开破损皮肤。', @doctor_luo_id, '审核通过', DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 45 MINUTE),
  ('PR20260623003', @visit_skin_id, NULL, @patient_id, @doctor_sun_id, 21.50, '待支付', '待审核', '待缴费', '过敏性皮炎待查', '按说明使用，若皮疹加重及时复诊。', NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
  visit_id = VALUES(visit_id),
  record_id = VALUES(record_id),
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  total_amount = VALUES(total_amount),
  fee_status = VALUES(fee_status),
  audit_status = VALUES(audit_status),
  status = VALUES(status),
  diagnosis = VALUES(diagnosis),
  usage_note = VALUES(usage_note),
  audit_doctor_id = VALUES(audit_doctor_id),
  audit_note = VALUES(audit_note),
  audited_at = VALUES(audited_at);

SET @prescription_ortho_id = (SELECT prescription_id FROM prescription WHERE prescription_no = 'PR20260623002');
SET @prescription_skin_id = (SELECT prescription_id FROM prescription WHERE prescription_no = 'PR20260623003');

INSERT INTO prescription_item (
  prescription_id, drug_id, drug_name, specification, unit_price, quantity,
  amount, dosage, frequency, usage_method, days, status
)
VALUES
  (@prescription_ortho_id, @drug_pain_id, '双氯芬酸二乙胺乳胶剂', '20g', 32.00, 1.00, 32.00, '适量', '每日3次', '外用', 5, '已发药'),
  (@prescription_skin_id, @drug_skin_id, '炉甘石洗剂', '100ml', 12.00, 1.00, 12.00, '适量', '每日2次', '外用', 5, '待发药'),
  (@prescription_skin_id, @drug_vc_id, '维生素C片', '100mg*100片', 9.50, 1.00, 9.50, '1片', '每日1次', '口服', 7, '待发药')
ON DUPLICATE KEY UPDATE
  drug_name = VALUES(drug_name),
  specification = VALUES(specification),
  unit_price = VALUES(unit_price),
  quantity = VALUES(quantity),
  amount = VALUES(amount),
  dosage = VALUES(dosage),
  frequency = VALUES(frequency),
  usage_method = VALUES(usage_method),
  days = VALUES(days),
  status = VALUES(status);

SELECT 'more seed completed' AS message;
SELECT COUNT(*) AS departments FROM department;
SELECT COUNT(*) AS doctors FROM doctor;
SELECT COUNT(*) AS schedules FROM doctor_schedule;
SELECT COUNT(*) AS registrations FROM registration WHERE patient_id = @patient_id;
SELECT COUNT(*) AS fee_orders FROM fee_order WHERE patient_id = @patient_id;
SELECT COUNT(*) AS medical_records FROM medical_record WHERE patient_id = @patient_id;
SELECT COUNT(*) AS reports FROM exam_lab_report WHERE patient_id = @patient_id;
SELECT COUNT(*) AS prescriptions FROM prescription WHERE patient_id = @patient_id;

DELETE FROM doctor_schedule
WHERE doctor_id IN (
  SELECT doctor_id FROM doctor
  WHERE doctor_no IN ('D2026015', 'D2026016', 'D2026017', 'D2026018', 'D2026019', 'D2026020')
);
DELETE FROM doctor WHERE doctor_no IN ('D2026015', 'D2026016', 'D2026017', 'D2026018', 'D2026019', 'D2026020');
DELETE FROM sys_user WHERE username IN ('doctor_feng', 'doctor_he', 'doctor_xu', 'doctor_jiang', 'doctor_su', 'doctor_peng');

SET @doctor_role_id_extra2 = (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR');

INSERT INTO sys_user (role_id, username, password, real_name, phone, email, status)
VALUES
  (@doctor_role_id_extra2, 'doctor_feng', '123456', '冯涛', '13800001015', 'feng.tao@example.com', 1),
  (@doctor_role_id_extra2, 'doctor_he', '123456', '何敏', '13800001016', 'he.min@example.com', 1),
  (@doctor_role_id_extra2, 'doctor_xu', '123456', '徐倩', '13800001017', 'xu.qian@example.com', 1),
  (@doctor_role_id_extra2, 'doctor_jiang', '123456', '蒋强', '13800001018', 'jiang.qiang@example.com', 1),
  (@doctor_role_id_extra2, 'doctor_su', '123456', '苏宁', '13800001019', 'su.ning@example.com', 1),
  (@doctor_role_id_extra2, 'doctor_peng', '123456', '彭静', '13800001020', 'peng.jing@example.com', 1)
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  password = VALUES(password),
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  email = VALUES(email),
  status = VALUES(status);

SET @doctor_feng_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_feng');
SET @doctor_he_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_he');
SET @doctor_xu_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_xu');
SET @doctor_jiang_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_jiang');
SET @doctor_su_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_su');
SET @doctor_peng_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_peng');

INSERT INTO doctor (
  user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status
)
VALUES
  (@doctor_feng_user_id, @pediatrics_dept_id, 'D2026015', '专家门诊', '主任医师', '儿科呼吸、发热、成长发育', '擅长儿童常见病和慢病随访', 1),
  (@doctor_he_user_id, @pediatrics_dept_id, 'D2026016', '普通门诊', '副主任医师', '儿童咳嗽、腹泻、营养', '擅长儿科综合诊疗', 1),
  (@doctor_xu_user_id, @skin_dept_id, 'D2026017', '专家门诊', '主任医师', '湿疹、荨麻疹、痤疮', '擅长皮肤科慢病和过敏管理', 1),
  (@doctor_jiang_user_id, @skin_dept_id, 'D2026018', '普通门诊', '副主任医师', '银屑病、皮炎、痤疮', '擅长常见皮肤病治疗', 1),
  (@doctor_su_user_id, @ortho_dept_id, 'D2026019', '专家门诊', '主任医师', '运动损伤、关节疼痛、骨折康复', '擅长骨科康复和术后管理', 1),
  (@doctor_peng_user_id, @ortho_dept_id, 'D2026020', '普通门诊', '副主任医师', '扭伤、骨关节病、腰腿痛', '擅长骨科常见病与康复指导', 1)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dept_id = VALUES(dept_id),
  doctor_type = VALUES(doctor_type),
  title = VALUES(title),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  status = VALUES(status);

SET @doctor_feng_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026015');
SET @doctor_he_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026016');
SET @doctor_xu_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026017');
SET @doctor_jiang_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026018');
SET @doctor_su_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026019');
SET @doctor_peng_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026020');

INSERT INTO doctor_schedule (
  doctor_id, dept_id, work_date, time_period, start_time, end_time,
  total_quota, remain_quota, registration_fee, status
)
VALUES
  (@doctor_feng_id, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 24, 12, 35.00, '可预约'),
  (@doctor_feng_id, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 24, 18, 35.00, '可预约'),
  (@doctor_he_id, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 20, 9, 28.00, '可预约'),
  (@doctor_he_id, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 20, 15, 28.00, '可预约'),
  (@doctor_xu_id, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 22, 11, 32.00, '可预约'),
  (@doctor_xu_id, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 22, 16, 32.00, '可预约'),
  (@doctor_jiang_id, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '08:00:00', '11:30:00', 18, 8, 26.00, '可预约'),
  (@doctor_jiang_id, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '下午', '14:00:00', '17:00:00', 18, 13, 26.00, '可预约'),
  (@doctor_su_id, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 26, 14, 30.00, '可预约'),
  (@doctor_su_id, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 26, 20, 30.00, '可预约'),
  (@doctor_peng_id, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 20, 10, 24.00, '可预约'),
  (@doctor_peng_id, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 20, 15, 24.00, '可预约')
ON DUPLICATE KEY UPDATE
  dept_id = VALUES(dept_id),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  total_quota = VALUES(total_quota),
  remain_quota = VALUES(remain_quota),
  registration_fee = VALUES(registration_fee),
  status = VALUES(status);
