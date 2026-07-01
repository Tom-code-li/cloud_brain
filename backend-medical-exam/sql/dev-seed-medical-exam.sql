USE doctor_platform;

INSERT INTO sys_role (role_id, role_code, role_name, description, status) VALUES
  (2, 'OUTPATIENT_DOCTOR', '门诊医生', '负责接诊、病历、检查检验申请和处方', 1),
  (3, 'EXAM_DOCTOR', '检查医生', '负责检查执行和报告发布', 1),
  (4, 'LAB_DOCTOR', '检验医生', '负责检验执行和报告发布', 1),
  (6, 'PATIENT', '患者', '患者端登录和个人就诊信息查看', 1)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = VALUES(status);

INSERT INTO sys_user (user_id, role_id, username, password, real_name, phone, status) VALUES
  (2, 2, 'out001', '{noop}123456', '王门诊', '13800000002', 1),
  (3, 3, 'exam001', '{noop}123456', '钱检查', '13800000003', 1),
  (4, 4, 'lab001', '{noop}123456', '孙检验', '13800000004', 1),
  (6, 6, 'patient001', '{noop}123456', '张晓雨', '13900001001', 1)
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  password = VALUES(password),
  real_name = VALUES(real_name),
  phone = VALUES(phone),
  status = VALUES(status);

INSERT INTO department (dept_id, dept_code, dept_name, dept_type, location, description, status) VALUES
  (2, 'GM', '全科门诊', 'OUTPATIENT', '门诊二楼A区', '常见病和初诊接诊', 1),
  (3, 'IMG', '医学影像科', 'EXAM', '医技楼一楼', '胸部DR、心电图等检查', 1),
  (4, 'LAB', '检验科', 'LAB', '医技楼二楼', '血液和体液检验', 1)
ON DUPLICATE KEY UPDATE
  dept_name = VALUES(dept_name),
  dept_type = VALUES(dept_type),
  location = VALUES(location),
  description = VALUES(description),
  status = VALUES(status);

INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status) VALUES
  (2, 2, 2, 'D-OUT-001', 'OUTPATIENT', '主治医师', '呼吸道感染、慢病随访', '负责全科门诊接诊。', 1),
  (3, 3, 3, 'D-EXAM-001', 'EXAM', '主治医师', '胸部DR、心电图', '负责检查执行与报告。', 1),
  (4, 4, 4, 'D-LAB-001', 'LAB', '主管检验师', '血常规、生化检验', '负责检验执行与报告。', 1)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dept_id = VALUES(dept_id),
  doctor_type = VALUES(doctor_type),
  title = VALUES(title),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  status = VALUES(status);

INSERT INTO patient (patient_id, user_id, patient_no, patient_name, gender, birthday, id_card, phone, emergency_contact, emergency_phone, address, allergy_history, past_history, status) VALUES
  (1, 6, 'P20260624001', '张晓雨', '女', '1992-04-18', '110101199204180021', '13900001001', '张明', '13900002001', '北京市朝阳区', '青霉素过敏', '无特殊既往史', 1)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  patient_name = VALUES(patient_name),
  gender = VALUES(gender),
  birthday = VALUES(birthday),
  id_card = VALUES(id_card),
  phone = VALUES(phone),
  emergency_contact = VALUES(emergency_contact),
  emergency_phone = VALUES(emergency_phone),
  address = VALUES(address),
  allergy_history = VALUES(allergy_history),
  past_history = VALUES(past_history),
  status = VALUES(status);

INSERT INTO registration (registration_id, patient_id, consultation_id, dept_id, doctor_id, schedule_id, operator_user_id, source, registration_no, queue_no, registration_fee, fee_status, status) VALUES
  (9001, 1, NULL, 2, 2, NULL, NULL, '线下', 'REG-ME-202606240001', 1, 15.00, '已支付', '接诊中')
ON DUPLICATE KEY UPDATE
  patient_id = VALUES(patient_id),
  dept_id = VALUES(dept_id),
  doctor_id = VALUES(doctor_id),
  source = VALUES(source),
  queue_no = VALUES(queue_no),
  registration_fee = VALUES(registration_fee),
  fee_status = VALUES(fee_status),
  status = VALUES(status);

INSERT INTO outpatient_visit (visit_id, registration_id, patient_id, doctor_id, dept_id, visit_no, queue_no, status, started_at) VALUES
  (9001, 9001, 1, 2, 2, 'VIS-ME-202606240001', 1, '接诊中', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  registration_id = VALUES(registration_id),
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
  dept_id = VALUES(dept_id),
  queue_no = VALUES(queue_no),
  status = VALUES(status),
  started_at = VALUES(started_at);

INSERT INTO medical_record (record_id, visit_id, patient_id, doctor_id, chief_complaint, present_illness, past_history, allergy_history, physical_exam, auxiliary_exam, diagnosis, treatment_advice, doctor_note, status, initial_saved_at, completed_at) VALUES
  (9001, 9001, 1, 2, '咳嗽发热2天', '体温最高38.3摄氏度，伴咽痛，无胸痛、气促。', '无特殊既往史', '青霉素过敏', '咽部充血，双肺呼吸音清。', '建议完善胸部DR、心电图和相关检验。', '急性上呼吸道感染待查', '完善检查检验后结合结果处理。', '检查检验模块联调病历。', '已完成', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  visit_id = VALUES(visit_id),
  patient_id = VALUES(patient_id),
  doctor_id = VALUES(doctor_id),
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
  initial_saved_at = VALUES(initial_saved_at),
  completed_at = VALUES(completed_at);

INSERT INTO medical_item (item_id, item_code, item_name, item_type, dept_id, unit, price, sample_type, clinical_meaning, status) VALUES
  (9101, 'EXAM-CXR', '胸部DR正位片', '检查', 3, '次', 80.00, NULL, '用于评估肺部感染、占位等胸部影像改变。', 1),
  (9102, 'EXAM-ECG', '心电图', '检查', 3, '次', 35.00, NULL, '用于评估心律、传导和缺血相关改变。', 1),
  (9201, 'LAB-CBC', '血常规', '检验', 4, '次', 35.00, '静脉血', '用于评估感染、贫血和血小板异常。', 1),
  (9202, 'LAB-CRP', 'C反应蛋白', '检验', 4, '次', 45.00, '静脉血', '用于辅助判断炎症程度。', 1),
  (9203, 'LAB-URINE', '尿常规', '检验', 4, '次', 25.00, '尿液', '用于评估泌尿系统感染、蛋白尿等情况。', 1),
  (9204, 'LAB-LIVER', '肝功能', '检验', 4, '次', 60.00, '静脉血', '用于评估肝细胞损伤和胆红素代谢。', 1),
  (9205, 'LAB-RENAL', '肾功能', '检验', 4, '次', 50.00, '静脉血', '用于评估肌酐、尿素氮等肾功能指标。', 1),
  (9206, 'LAB-GLU', '血糖', '检验', 4, '次', 15.00, '静脉血', '用于评估血糖水平。', 1)
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  item_type = VALUES(item_type),
  dept_id = VALUES(dept_id),
  unit = VALUES(unit),
  price = VALUES(price),
  sample_type = VALUES(sample_type),
  clinical_meaning = VALUES(clinical_meaning),
  status = VALUES(status);

INSERT INTO exam_lab_order (order_id, order_no, visit_id, record_id, patient_id, apply_doctor_id, execute_dept_id, order_type, clinical_diagnosis, purpose, total_amount, fee_status, status, applied_at, executed_at, completed_at) VALUES
  (9101, 'EXAM-ME-202606240001', 9001, 9001, 1, 2, 3, '检查', '急性上呼吸道感染待查', '评估肺部感染及心电情况。', 115.00, '已支付', '待执行', CURRENT_TIMESTAMP, NULL, NULL),
  (9201, 'LAB-ME-202606240001', 9001, 9001, 1, 2, 4, '检验', '急性上呼吸道感染待查', '评估炎症、尿常规、肝肾功能和血糖。', 230.00, '已支付', '待执行', CURRENT_TIMESTAMP, NULL, NULL)
ON DUPLICATE KEY UPDATE
  visit_id = VALUES(visit_id),
  record_id = VALUES(record_id),
  patient_id = VALUES(patient_id),
  apply_doctor_id = VALUES(apply_doctor_id),
  execute_dept_id = VALUES(execute_dept_id),
  order_type = VALUES(order_type),
  clinical_diagnosis = VALUES(clinical_diagnosis),
  purpose = VALUES(purpose),
  total_amount = VALUES(total_amount),
  fee_status = VALUES(fee_status),
  status = VALUES(status),
  applied_at = VALUES(applied_at),
  executed_at = VALUES(executed_at),
  completed_at = VALUES(completed_at);

INSERT INTO exam_lab_order_item (order_item_id, order_id, item_id, item_name, item_type, unit_price, quantity, amount, status, executed_at, result_summary) VALUES
  (910101, 9101, 9101, '胸部DR正位片', '检查', 80.00, 1.00, 80.00, '待执行', NULL, NULL),
  (910102, 9101, 9102, '心电图', '检查', 35.00, 1.00, 35.00, '待执行', NULL, NULL),
  (920101, 9201, 9201, '血常规', '检验', 35.00, 1.00, 35.00, '待执行', NULL, NULL),
  (920102, 9201, 9202, 'C反应蛋白', '检验', 45.00, 1.00, 45.00, '待执行', NULL, NULL),
  (920103, 9201, 9203, '尿常规', '检验', 25.00, 1.00, 25.00, '待执行', NULL, NULL),
  (920104, 9201, 9204, '肝功能', '检验', 60.00, 1.00, 60.00, '待执行', NULL, NULL),
  (920105, 9201, 9205, '肾功能', '检验', 50.00, 1.00, 50.00, '待执行', NULL, NULL),
  (920106, 9201, 9206, '血糖', '检验', 15.00, 1.00, 15.00, '待执行', NULL, NULL)
ON DUPLICATE KEY UPDATE
  order_id = VALUES(order_id),
  item_id = VALUES(item_id),
  item_name = VALUES(item_name),
  item_type = VALUES(item_type),
  unit_price = VALUES(unit_price),
  quantity = VALUES(quantity),
  amount = VALUES(amount),
  status = VALUES(status),
  executed_at = VALUES(executed_at),
  result_summary = VALUES(result_summary);
