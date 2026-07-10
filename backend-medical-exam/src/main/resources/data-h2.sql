INSERT INTO sys_user (user_id, role_id, username, password, real_name, phone, status) VALUES
  (2, 2, 'out001', '{noop}123456', '王门诊', '13800000002', 1),
  (3, 3, 'exam001', '{noop}123456', '钱检查', '13800000003', 1),
  (4, 4, 'lab001', '{noop}123456', '孙检验', '13800000004', 1);

INSERT INTO patient (patient_id, patient_no, patient_name, gender, birthday, allergy_history, past_history, status) VALUES
  (1, 'P001', '张晓雨', '女', '1992-04-18', '青霉素过敏', '无特殊既往史', 1);

INSERT INTO department (dept_id, dept_code, dept_name, dept_type, status) VALUES
  (3, 'IMG', '医学影像科', 'EXAM', 1),
  (4, 'LAB', '检验科', 'LAB', 1);

INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_no, doctor_type, title, specialty, status) VALUES
  (2, 2, 3, 'D-OUT-001', 'OUTPATIENT', '主治医师', '门诊开单', 1),
  (3, 3, 3, 'D-EXAM-001', 'EXAM', '主治医师', '胸部影像', 1),
  (4, 4, 4, 'D-LAB-001', 'LAB', '主管检验师', '血液检验', 1);

INSERT INTO medical_item (item_id, item_code, item_name, item_type, dept_id, unit, price, sample_type, clinical_meaning, status) VALUES
  (1, 'EXAM-CXR', '胸部DR正位片', '检查', 3, '次', 80.00, NULL, '胸部影像检查', 1),
  (2, 'LAB-CBC', '血常规', '检验', 4, '次', 35.00, '静脉血', '血液检验', 1),
  (3, 'LAB-CRP', 'C反应蛋白', '检验', 4, '次', 45.00, '静脉血', '炎症指标', 1);

INSERT INTO outpatient_visit (visit_id, registration_id, patient_id, doctor_id, dept_id, visit_no, queue_no, status, started_at) VALUES
  (1, 1, 1, 2, 3, 'VIS001', 1, '接诊中', CURRENT_TIMESTAMP);

INSERT INTO exam_lab_order (order_id, order_no, visit_id, record_id, patient_id, apply_doctor_id, execute_dept_id, order_type, clinical_diagnosis, purpose, total_amount, fee_status, status, applied_at) VALUES
  (1, 'EXLAB001', 1, 1, 1, 2, 3, '检查', '急性上呼吸道感染待查', '评估肺部感染', 80.00, '已支付', '待执行', CURRENT_TIMESTAMP),
  (2, 'EXLAB002', 1, 1, 1, 2, 4, '检验', '急性上呼吸道感染待查', '评估炎症指标', 35.00, '已支付', '待执行', CURRENT_TIMESTAMP),
  (3, 'EXLAB003', 1, 1, 1, 2, 3, '检查', '急性上呼吸道感染待查', '未支付检查', 80.00, '待支付', '待缴费', CURRENT_TIMESTAMP);

INSERT INTO exam_lab_order_item (order_item_id, order_id, item_id, item_name, item_type, unit_price, quantity, amount, status, result_summary) VALUES
  (101, 1, 1, '胸部DR正位片', '检查', 80.00, 1.00, 80.00, '待执行', NULL),
  (102, 2, 2, '血常规', '检验', 35.00, 1.00, 35.00, '待执行', NULL),
  (103, 3, 1, '胸部DR正位片', '检查', 80.00, 1.00, 80.00, '待缴费', NULL);
