SET NAMES utf8mb4;
USE doctor_platform;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM prescription_item
WHERE prescription_id IN (SELECT prescription_id FROM prescription WHERE prescription_no = 'PR20260623001');
DELETE FROM prescription WHERE prescription_no = 'PR20260623001';
DELETE FROM drug WHERE drug_code IN ('DRUG001', 'DRUG002');

DELETE FROM exam_lab_report WHERE report_no = 'RP20260623001';
DELETE FROM exam_lab_order_item
WHERE order_id IN (SELECT order_id FROM exam_lab_order WHERE order_no = 'EO20260623001');
DELETE FROM exam_lab_order WHERE order_no = 'EO20260623001';
DELETE FROM medical_item WHERE item_code = 'ITEM001';

DELETE FROM medical_record
WHERE visit_id IN (SELECT visit_id FROM outpatient_visit WHERE visit_no = 'V20260623001');
DELETE FROM outpatient_visit WHERE visit_no = 'V20260623001';

DELETE FROM fee_order WHERE order_no IN ('FO20260623001', 'FO20260623002');
DELETE FROM registration WHERE registration_no IN ('RG20260623001', 'RG20260623002');

DELETE FROM doctor_schedule
WHERE doctor_id IN (SELECT doctor_id FROM doctor WHERE doctor_no IN ('D2026001', 'D2026002', 'D2026003'));
DELETE FROM doctor WHERE doctor_no IN ('D2026001', 'D2026002', 'D2026003');
DELETE FROM department WHERE dept_code IN ('DEPT001', 'DEPT002', 'DEPT003', 'DEPT004');

DELETE FROM patient WHERE patient_no = 'P20260623001';
DELETE FROM sys_user WHERE username IN ('demo_patient', 'doctor_li', 'doctor_wang', 'doctor_chen');

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_role (role_code, role_name, description, status)
VALUES
  ('PATIENT', '患者', '患者端用户', 1),
  ('DOCTOR', '医生', '医生端用户', 1)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = VALUES(status);

SET @patient_role_id = (SELECT role_id FROM sys_role WHERE role_code = 'PATIENT');
SET @doctor_role_id = (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR');

INSERT INTO sys_user (role_id, username, password, real_name, phone, email, status)
VALUES
  (@patient_role_id, 'demo_patient', '123456', '章瑶', '18355117940', 'patient@example.com', 1),
  (@doctor_role_id, 'doctor_li', '123456', '李明', '13800001001', 'li.ming@example.com', 1),
  (@doctor_role_id, 'doctor_wang', '123456', '王敏', '13800001002', 'wang.min@example.com', 1),
  (@doctor_role_id, 'doctor_chen', '123456', '陈晨', '13800001003', 'chen.chen@example.com', 1)
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  password = VALUES(password),
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  email = VALUES(email),
  status = VALUES(status);

SET @demo_patient_user_id = (SELECT user_id FROM sys_user WHERE username = 'demo_patient');
SET @doctor_li_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_li');
SET @doctor_wang_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_wang');
SET @doctor_chen_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_chen');

INSERT INTO patient (
  user_id, patient_no, patient_name, gender, birthday, id_card, phone,
  emergency_contact, emergency_phone, address, allergy_history, past_history, status
)
VALUES (
  @demo_patient_user_id, 'P20260623001', '章瑶', '女', '2003-06-18',
  '341421200606080771', '18355117940',
  '张女士', '13900002001', '安徽省合肥市蜀山区',
  '青霉素过敏', '无重大既往病史', 1
)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  patient_name = VALUES(patient_name),
  gender = VALUES(gender),
  birthday = VALUES(birthday),
  phone = VALUES(phone),
  emergency_contact = VALUES(emergency_contact),
  emergency_phone = VALUES(emergency_phone),
  address = VALUES(address),
  allergy_history = VALUES(allergy_history),
  past_history = VALUES(past_history),
  status = VALUES(status);

SET @patient_id = (SELECT patient_id FROM patient WHERE patient_no = 'P20260623001');

INSERT INTO department (dept_code, dept_name, dept_type, location, description, status)
VALUES
  ('DEPT001', '心血管内科', '临床', '门诊楼 2 层 A 区', '胸闷、心悸、高血压等常见心血管疾病诊疗', 1),
  ('DEPT002', '呼吸内科', '临床', '门诊楼 2 层 B 区', '咳嗽、发热、哮喘、肺部感染诊疗', 1),
  ('DEPT003', '消化内科', '临床', '门诊楼 3 层 A 区', '胃痛、腹胀、胃肠道疾病诊疗', 1),
  ('DEPT004', '检验科', '医技', '医技楼 1 层', '血常规、生化、免疫等检验项目', 1)
ON DUPLICATE KEY UPDATE
  dept_name = VALUES(dept_name),
  dept_type = VALUES(dept_type),
  location = VALUES(location),
  description = VALUES(description),
  status = VALUES(status);

SET @cardio_dept_id = (SELECT dept_id FROM department WHERE dept_code = 'DEPT001');
SET @resp_dept_id = (SELECT dept_id FROM department WHERE dept_code = 'DEPT002');
SET @digest_dept_id = (SELECT dept_id FROM department WHERE dept_code = 'DEPT003');
SET @lab_dept_id = (SELECT dept_id FROM department WHERE dept_code = 'DEPT004');

INSERT INTO doctor (
  user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status
)
VALUES
  (@doctor_li_user_id, @cardio_dept_id, 'D2026001', '普通门诊', '副主任医师', '高血压、冠心病、心律失常', '从事心血管内科临床工作 12 年。', 1),
  (@doctor_wang_user_id, @resp_dept_id, 'D2026002', '专家门诊', '主任医师', '慢性咳嗽、哮喘、肺部感染', '擅长呼吸系统常见病和多发病诊疗。', 1),
  (@doctor_chen_user_id, @digest_dept_id, 'D2026003', '普通门诊', '主治医师', '胃炎、消化不良、腹痛', '关注消化系统慢病管理。', 1)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dept_id = VALUES(dept_id),
  doctor_type = VALUES(doctor_type),
  title = VALUES(title),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  status = VALUES(status);

SET @doctor_li_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026001');
SET @doctor_wang_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026002');
SET @doctor_chen_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026003');

INSERT INTO doctor_schedule (
  doctor_id, dept_id, work_date, time_period, start_time, end_time,
  total_quota, remain_quota, registration_fee, status
)
VALUES
  (@doctor_li_id, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 30, 18, 20.00, '可预约'),
  (@doctor_li_id, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '下午', '14:00:00', '17:00:00', 25, 12, 20.00, '可预约'),
  (@doctor_wang_id, @resp_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '下午', '14:00:00', '17:00:00', 20, 9, 35.00, '可预约'),
  (@doctor_chen_id, @digest_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '08:00:00', '11:30:00', 28, 21, 18.00, '可预约')
ON DUPLICATE KEY UPDATE
  dept_id = VALUES(dept_id),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  total_quota = VALUES(total_quota),
  remain_quota = VALUES(remain_quota),
  registration_fee = VALUES(registration_fee),
  status = VALUES(status);

SET @schedule_li_morning = (
  SELECT schedule_id FROM doctor_schedule
  WHERE doctor_id = @doctor_li_id AND work_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND time_period = '上午'
);
SET @schedule_wang_afternoon = (
  SELECT schedule_id FROM doctor_schedule
  WHERE doctor_id = @doctor_wang_id AND work_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND time_period = '下午'
);

INSERT INTO registration (
  patient_id, dept_id, doctor_id, schedule_id, source, registration_no, queue_no,
  registration_fee, fee_status, status, registered_at
)
VALUES
  (@patient_id, @cardio_dept_id, @doctor_li_id, @schedule_li_morning, '线上', 'RG20260623001', 8, 20.00, '待支付', '待支付', NOW()),
  (@patient_id, @resp_dept_id, @doctor_wang_id, @schedule_wang_afternoon, '线上', 'RG20260623002', 3, 35.00, '已支付', '已完成', DATE_SUB(NOW(), INTERVAL 7 DAY))
ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  dept_id = VALUES(dept_id),
  doctor_id = VALUES(doctor_id),
  schedule_id = VALUES(schedule_id),
  source = VALUES(source),
  queue_no = VALUES(queue_no),
  registration_fee = VALUES(registration_fee),
  fee_status = VALUES(fee_status),
  status = VALUES(status),
  registered_at = VALUES(registered_at);

SET @reg_unpaid_id = (SELECT registration_id FROM registration WHERE registration_no = 'RG20260623001');
SET @reg_completed_id = (SELECT registration_id FROM registration WHERE registration_no = 'RG20260623002');

INSERT INTO fee_order (
  order_no, patient_id, registration_id, business_type, business_id,
  total_amount, paid_amount, refund_amount, status, created_by, paid_at
)
VALUES
  ('FO20260623001', @patient_id, @reg_unpaid_id, 'REGISTRATION', @reg_unpaid_id, 20.00, 0.00, 0.00, '待支付', @doctor_li_user_id, NULL),
  ('FO20260623002', @patient_id, @reg_completed_id, 'REGISTRATION', @reg_completed_id, 35.00, 35.00, 0.00, '已支付', @doctor_wang_user_id, DATE_SUB(NOW(), INTERVAL 7 DAY))
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
VALUES (
  @reg_completed_id, @patient_id, @doctor_wang_id, @resp_dept_id,
  'V20260623001', 3, '已完成', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 30 MINUTE
)
ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  dept_id = VALUES(dept_id),
  queue_no = VALUES(queue_no),
  status = VALUES(status),
  started_at = VALUES(started_at),
  finished_at = VALUES(finished_at);

SET @visit_id = (SELECT visit_id FROM outpatient_visit WHERE visit_no = 'V20260623001');

INSERT INTO medical_record (
  visit_id, patient_id, doctor_id, chief_complaint, present_illness,
  past_history, allergy_history, physical_exam, auxiliary_exam, diagnosis,
  treatment_advice, doctor_note, status, initial_saved_at, completed_at
)
VALUES (
  @visit_id, @patient_id, @doctor_wang_id, '咳嗽伴咽痛 3 天',
  '患者 3 天前受凉后出现咳嗽、咽痛，无明显胸闷气促。',
  '无特殊既往病史', '青霉素过敏', '体温 37.2℃，咽部轻度充血，双肺呼吸音清。',
  '血常规白细胞轻度升高', '急性上呼吸道感染',
  '多饮水，注意休息，按医嘱服药，症状加重及时复诊。',
  '门诊随访', '已完成', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 20 MINUTE
)
ON DUPLICATE KEY UPDATE
  chief_complaint = VALUES(chief_complaint),
  present_illness = VALUES(present_illness),
  past_history = VALUES(past_history),
  allergy_history = VALUES(allergy_history),
  physical_exam = VALUES(physical_exam),
  auxiliary_exam = VALUES(auxiliary_exam),
  diagnosis = VALUES(diagnosis),
  treatment_advice = VALUES(treatment_advice),
  doctor_note = VALUES(doctor_note),
  status = VALUES(status),
  completed_at = VALUES(completed_at);

SET @record_id = (
  SELECT record_id FROM medical_record
  WHERE visit_id = @visit_id AND patient_id = @patient_id
  ORDER BY record_id DESC LIMIT 1
);

INSERT INTO medical_item (item_code, item_name, item_type, dept_id, unit, price, sample_type, clinical_meaning, status)
VALUES
  ('ITEM001', '血常规', '检验', @lab_dept_id, '次', 25.00, '静脉血', '辅助判断感染、贫血等情况', 1)
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  item_type = VALUES(item_type),
  dept_id = VALUES(dept_id),
  unit = VALUES(unit),
  price = VALUES(price),
  sample_type = VALUES(sample_type),
  clinical_meaning = VALUES(clinical_meaning),
  status = VALUES(status);

SET @item_blood_id = (SELECT item_id FROM medical_item WHERE item_code = 'ITEM001');

INSERT INTO exam_lab_order (
  order_no, visit_id, record_id, patient_id, apply_doctor_id, execute_dept_id,
  order_type, clinical_diagnosis, purpose, total_amount, fee_status, status, applied_at, executed_at, completed_at
)
VALUES (
  'EO20260623001', @visit_id, @record_id, @patient_id, @doctor_wang_id, @lab_dept_id,
  '检验', '急性上呼吸道感染', '判断感染指标', 25.00, '已支付', '已完成',
  DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 40 MINUTE, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 90 MINUTE
)
ON DUPLICATE KEY UPDATE
  visit_id = VALUES(visit_id),
  record_id = VALUES(record_id),
  patient_id = VALUES(patient_id),
  apply_doctor_id = VALUES(apply_doctor_id),
  execute_dept_id = VALUES(execute_dept_id),
  clinical_diagnosis = VALUES(clinical_diagnosis),
  purpose = VALUES(purpose),
  total_amount = VALUES(total_amount),
  fee_status = VALUES(fee_status),
  status = VALUES(status),
  completed_at = VALUES(completed_at);

SET @exam_order_id = (SELECT order_id FROM exam_lab_order WHERE order_no = 'EO20260623001');

INSERT INTO exam_lab_order_item (
  order_id, item_id, item_name, item_type, unit_price, quantity, amount, status, executed_at, result_summary
)
VALUES (
  @exam_order_id, @item_blood_id, '血常规', '检验', 25.00, 1.00, 25.00,
  '已完成', DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 50 MINUTE, '白细胞轻度升高'
)
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  item_type = VALUES(item_type),
  unit_price = VALUES(unit_price),
  quantity = VALUES(quantity),
  amount = VALUES(amount),
  status = VALUES(status),
  result_summary = VALUES(result_summary);

SET @exam_order_item_id = (
  SELECT order_item_id FROM exam_lab_order_item
  WHERE order_id = @exam_order_id AND item_id = @item_blood_id
  ORDER BY order_item_id DESC LIMIT 1
);

INSERT INTO exam_lab_report (
  order_id, order_item_id, patient_id, report_doctor_id, report_no, report_type,
  findings, conclusion, ai_draft, doctor_review, status, published_at
)
VALUES (
  @exam_order_id, @exam_order_item_id, @patient_id, @doctor_wang_id,
  'RP20260623001', '检验', '白细胞计数 10.8×10^9/L，中性粒细胞比例轻度升高。',
  '提示轻度感染，建议结合临床症状判断。', 'AI 提示：感染指标轻度升高。',
  '已审核，结果可供临床参考。', '已发布', DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 100 MINUTE
)
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
  ('DRUG001', '蒲地蓝消炎片', '0.3g*48片', '片剂', '示例制药有限公司', '盒', 28.00, 100.00, 20.00, '孕妇慎用', 1),
  ('DRUG002', '氯雷他定片', '10mg*12片', '片剂', '示例制药有限公司', '盒', 18.00, 80.00, 20.00, '对本品过敏者禁用', 1)
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

SET @drug_1_id = (SELECT drug_id FROM drug WHERE drug_code = 'DRUG001');
SET @drug_2_id = (SELECT drug_id FROM drug WHERE drug_code = 'DRUG002');

INSERT INTO prescription (
  prescription_no, visit_id, record_id, patient_id, doctor_id, total_amount,
  fee_status, audit_status, status, diagnosis, usage_note, audit_doctor_id, audit_note, audited_at
)
VALUES (
  'PR20260623001', @visit_id, @record_id, @patient_id, @doctor_wang_id, 46.00,
  '已支付', '审核通过', '待发药', '急性上呼吸道感染',
  '按说明服用，如出现不适及时停药并咨询医生。',
  @doctor_wang_id, '审核通过', DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 35 MINUTE
)
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

SET @prescription_id = (SELECT prescription_id FROM prescription WHERE prescription_no = 'PR20260623001');

INSERT INTO prescription_item (
  prescription_id, drug_id, drug_name, specification, unit_price, quantity,
  amount, dosage, frequency, usage_method, days, status
)
VALUES
  (@prescription_id, @drug_1_id, '蒲地蓝消炎片', '0.3g*48片', 28.00, 1.00, 28.00, '4片', '每日3次', '口服', 3, '待发药'),
  (@prescription_id, @drug_2_id, '氯雷他定片', '10mg*12片', 18.00, 1.00, 18.00, '1片', '每日1次', '口服', 3, '待发药')
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

SELECT 'seed completed' AS message;
SELECT username, password, real_name FROM sys_user WHERE username = 'demo_patient';
SELECT patient_id, patient_no, patient_name FROM patient WHERE patient_no = 'P20260623001';

DELETE FROM doctor_schedule
WHERE doctor_id IN (
  SELECT doctor_id FROM doctor
  WHERE doctor_no IN ('D2026010', 'D2026011', 'D2026012', 'D2026013', 'D2026014')
);
DELETE FROM doctor WHERE doctor_no IN ('D2026010', 'D2026011', 'D2026012', 'D2026013', 'D2026014');
DELETE FROM sys_user WHERE username IN ('doctor_hu', 'doctor_yu', 'doctor_shen', 'doctor_ma', 'doctor_liu');

SET @doctor_role_id_extra = (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR');

INSERT INTO sys_user (role_id, username, password, real_name, phone, email, status)
VALUES
  (@doctor_role_id_extra, 'doctor_hu', '123456', '胡明', '13800001010', 'hu.ming@example.com', 1),
  (@doctor_role_id_extra, 'doctor_yu', '123456', '余涛', '13800001011', 'yu.tao@example.com', 1),
  (@doctor_role_id_extra, 'doctor_shen', '123456', '沈洁', '13800001012', 'shen.jie@example.com', 1),
  (@doctor_role_id_extra, 'doctor_ma', '123456', '马强', '13800001013', 'ma.qiang@example.com', 1),
  (@doctor_role_id_extra, 'doctor_liu', '123456', '刘宁', '13800001014', 'liu.ning@example.com', 1)
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  password = VALUES(password),
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  email = VALUES(email),
  status = VALUES(status);

SET @doctor_hu_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_hu');
SET @doctor_yu_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_yu');
SET @doctor_shen_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_shen');
SET @doctor_ma_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_ma');
SET @doctor_liu_user_id = (SELECT user_id FROM sys_user WHERE username = 'doctor_liu');

INSERT INTO doctor (
  user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status
)
VALUES
  (@doctor_hu_user_id, @cardio_dept_id, 'D2026010', '专家门诊', '主任医师', '高血压、冠心病、心律失常', '擅长心血管常见病与慢病管理', 1),
  (@doctor_yu_user_id, @resp_dept_id, 'D2026011', '普通门诊', '副主任医师', '慢阻肺、哮喘、呼吸道感染', '擅长呼吸系统疾病诊治', 1),
  (@doctor_shen_user_id, @digest_dept_id, 'D2026012', '专家门诊', '主任医师', '胃炎、消化不良、腹痛', '擅长消化系统疾病与胃肠镜相关咨询', 1),
  (@doctor_ma_user_id, @lab_dept_id, 'D2026013', '检验门诊', '主管技师', '血常规、生化、免疫', '负责检验项目咨询与报告解读', 1),
  (@doctor_liu_user_id, @lab_dept_id, 'D2026014', '检验门诊', '副主管技师', '采血、标本处理、检验咨询', '负责检验前置服务和结果解释', 1)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dept_id = VALUES(dept_id),
  doctor_type = VALUES(doctor_type),
  title = VALUES(title),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  status = VALUES(status);

SET @doctor_hu_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026010');
SET @doctor_yu_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026011');
SET @doctor_shen_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026012');
SET @doctor_ma_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026013');
SET @doctor_liu_id = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026014');

INSERT INTO doctor_schedule (
  doctor_id, dept_id, work_date, time_period, start_time, end_time,
  total_quota, remain_quota, registration_fee, status
)
VALUES
  (@doctor_hu_id, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '08:00:00', '11:30:00', 20, 12, 30.00, '可预约'),
  (@doctor_hu_id, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '下午', '14:00:00', '17:00:00', 20, 15, 30.00, '可预约'),
  (@doctor_yu_id, @resp_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 18, 9, 25.00, '可预约'),
  (@doctor_yu_id, @resp_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 18, 11, 25.00, '可预约'),
  (@doctor_shen_id, @digest_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 22, 14, 22.00, '可预约'),
  (@doctor_shen_id, @digest_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 22, 16, 22.00, '可预约'),
  (@doctor_ma_id, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 30, 18, 18.00, '可预约'),
  (@doctor_ma_id, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 30, 24, 18.00, '可预约'),
  (@doctor_liu_id, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '08:00:00', '11:30:00', 28, 20, 18.00, '可预约'),
  (@doctor_liu_id, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '下午', '14:00:00', '17:00:00', 28, 25, 18.00, '可预约')
ON DUPLICATE KEY UPDATE
  dept_id = VALUES(dept_id),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  total_quota = VALUES(total_quota),
  remain_quota = VALUES(remain_quota),
  registration_fee = VALUES(registration_fee),
  status = VALUES(status);
