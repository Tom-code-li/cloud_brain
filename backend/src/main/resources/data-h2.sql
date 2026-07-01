INSERT INTO sys_role(role_id, role_code, role_name, status)
VALUES (1, 'outpatient', '门诊医生', 1);

INSERT INTO sys_user(user_id, role_id, username, password, real_name, status)
VALUES (1, 1, 'DOC2025001', '{noop}123456', '张仲景', 1);

INSERT INTO department(dept_id, dept_code, dept_name, dept_type, status)
VALUES (1, 'RESP', '呼吸内科', '门诊', 1);

INSERT INTO doctor(doctor_id, user_id, dept_id, doctor_no, doctor_type, title, status)
VALUES (1, 1, 1, 'D2025001', '门诊医生', '主任医师', 1);

INSERT INTO patient(patient_id, patient_no, patient_name, gender, birthday, id_card, phone, allergy_history, past_history, status)
VALUES
  (1, 'P20260622001', '张晓雨', '女', DATE '1992-03-12', '110101199203120021', '13800000001', '青霉素过敏', '既往体健', 1),
  (2, 'P20260622002', '刘建国', '男', DATE '1979-08-20', '110101197908200038', '13800000002', '无', '高血压病史', 1);

INSERT INTO registration(registration_id, patient_id, dept_id, doctor_id, registration_no, queue_no, fee_status, status)
VALUES
  (1, 1, 1, 1, 'REG20260622001', 1, '已支付', '接诊中'),
  (2, 2, 1, 1, 'REG20260622002', 2, '已支付', '待接诊');

INSERT INTO outpatient_visit(visit_id, registration_id, patient_id, doctor_id, dept_id, visit_no, queue_no, status, started_at)
VALUES
  (1, 1, 1, 1, 1, 'VIS20260622001', 1, '接诊中', CURRENT_TIMESTAMP),
  (2, 2, 2, 1, 1, 'VIS20260622002', 2, '待接诊', NULL);

INSERT INTO medical_record(record_id, visit_id, patient_id, doctor_id, chief_complaint, present_illness, current_treatment, past_history, allergy_history, physical_exam, diagnosis, treatment_advice, doctor_note, final_diagnosis, final_opinion, confirmed_doctor_id, confirmed_at, status)
VALUES
  (1, 1, 1, 1, '咳嗽、发热 2 天', '发热伴咳嗽，最高体温 38.5℃。', '已口服退热药', '既往体健', '青霉素过敏', '双肺呼吸音粗', '社区获得性肺炎待排', '完善血常规、CRP、胸片', '注意休息，多饮水', '社区获得性肺炎', '建议抗感染治疗并门诊随访', 1, CURRENT_TIMESTAMP, '初诊暂存');

INSERT INTO medical_item(item_id, item_code, item_name, item_type, dept_id, unit, price, sample_type, clinical_meaning, status)
VALUES
  (1, 'EXAM-CXR', '胸部DR正位片', '检查', 1, '次', 28.00, NULL, '胸部影像学筛查', 1),
  (2, 'LAB-CBC', '血常规', '检验', 1, '次', 18.50, '静脉血', '感染、贫血等筛查', 1),
  (3, 'LAB-CRP', 'C反应蛋白', '检验', 1, '次', 26.00, '静脉血', '炎症指标评估', 1);

INSERT INTO exam_lab_order(order_id, order_no, visit_id, record_id, patient_id, apply_doctor_id, execute_dept_id, order_type, clinical_diagnosis, purpose, exam_site, specimen_type, remark, priority, collection_way, total_amount, fee_status, status, applied_at, completed_at)
VALUES
  (1, 'EXL20260622001', 1, 1, 1, 1, 1, '检查', '社区获得性肺炎待排', '排查肺部感染', '胸部', NULL, '门诊检查', '普通', '门诊采样', 28.00, '已支付', '已完成', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fee_order(fee_order_id, order_no, patient_id, registration_id, visit_id, business_type, business_id, total_amount, paid_amount, refund_amount, status, created_by)
VALUES
  (1, 'FEE20260622001', 1, 1, 1, 'EXAM_LAB_ORDER', 1, 28.00, 28.00, 0.00, '已支付', 1);

INSERT INTO fee_order_item(fee_order_item_id, fee_order_id, item_type, item_id, item_code, item_name, item_spec, unit_price, quantity, amount, status)
VALUES
  (1, 1, '检查', 1, 'EXAM-CXR', '胸部DR正位片', '次', 28.00, 1.00, 28.00, '已支付');

INSERT INTO exam_lab_order_item(order_item_id, order_id, item_id, item_name, item_type, unit_price, quantity, amount, status, executed_at, result_summary)
VALUES
  (1, 1, 1, '胸部DR正位片', '检查', 28.00, 1.00, 28.00, '已完成', CURRENT_TIMESTAMP, '右肺中叶炎性改变');

INSERT INTO exam_lab_report(report_id, order_id, order_item_id, patient_id, report_doctor_id, report_no, report_type, findings, conclusion, doctor_review, status, published_at)
VALUES
  (1, 1, 1, 1, 1, 'REP20260622001', '检查', '胸片显示右肺中叶斑片状高密度影；检验回报白细胞计数 12.5，中性粒细胞 85%。', '结合影像及炎症指标，社区获得性肺炎可能。', '建议结合症状、体征和药物过敏史综合判断。', '已发布', CURRENT_TIMESTAMP);

INSERT INTO exam_result_feature(feature_id, report_id, order_item_id, feature_name, feature_value, unit, abnormal_flag, sort_order)
VALUES
  (1, 1, 1, '影像学表现', '右肺中叶斑片状高密度影', NULL, 'ABNORMAL', 1),
  (2, 1, 1, '胸腔积液', '未见明显胸腔积液', NULL, 'NORMAL', 2);

INSERT INTO lab_result_item(result_item_id, report_id, order_item_id, item_code, indicator_code, indicator_name, result_value, unit, reference_range, abnormal_flag, sort_order)
VALUES
  (1, 1, 1, 'LAB-CBC', 'WBC', '白细胞计数', '12.5', '10^9/L', '3.5-9.5', 'HIGH', 1),
  (2, 1, 1, 'LAB-CBC', 'NEUT%', '中性粒细胞百分比', '85', '%', '40-75', 'HIGH', 2),
  (3, 1, 1, 'LAB-CRP', 'CRP', 'C反应蛋白', '26', 'mg/L', '0-10', 'HIGH', 3);

INSERT INTO drug(drug_id, drug_code, drug_name, specification, dosage_form, manufacturer, unit, sale_price, stock_quantity, warning_quantity, status)
VALUES
  (1, 'DRUG-AMOX', '阿莫西林胶囊', '0.25g*24粒/盒', '胶囊剂', '示例药厂', '盒', 12.80, 100.00, 10.00, 1),
  (2, 'DRUG-APAP', '对乙酰氨基酚片', '0.5g*20片/盒', '片剂', '示例药厂', '盒', 8.60, 120.00, 10.00, 1);
