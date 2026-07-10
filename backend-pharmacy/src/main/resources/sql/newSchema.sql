CREATE DATABASE IF NOT EXISTS doctor_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE doctor_platform;

CREATE TABLE IF NOT EXISTS sys_role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sys_user_role FOREIGN KEY (role_id) REFERENCES sys_role(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS patient (
  patient_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  patient_no VARCHAR(50) NOT NULL UNIQUE,
  patient_name VARCHAR(50) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  birthday DATE,
  id_card VARCHAR(30) UNIQUE,
  phone VARCHAR(20),
  emergency_contact VARCHAR(50),
  emergency_phone VARCHAR(20),
  address VARCHAR(255),
  allergy_history TEXT,
  past_history TEXT,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS department (
  dept_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT,
  dept_code VARCHAR(50) NOT NULL UNIQUE,
  dept_name VARCHAR(100) NOT NULL,
  dept_type VARCHAR(30) NOT NULL,
  floor VARCHAR(50),
  phone VARCHAR(30),
  location VARCHAR(100),
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctor (
  doctor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  dept_id BIGINT NOT NULL,
  doctor_no VARCHAR(50) NOT NULL UNIQUE,
  doctor_type VARCHAR(50) NOT NULL,
  title VARCHAR(50),
  specialty VARCHAR(255),
  introduction TEXT,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
  CONSTRAINT fk_doctor_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctor_schedule (
  schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  time_period VARCHAR(20) NOT NULL,
  start_time TIME,
  end_time TIME,
  total_quota INT NOT NULL DEFAULT 0,
  remain_quota INT NOT NULL DEFAULT 0,
  registration_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL DEFAULT '可预约',
  source VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_doctor_schedule (doctor_id, work_date, time_period),
  CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_schedule_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_schedule_suggestion (
  suggestion_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT,
  dept_id BIGINT,
  work_date DATE,
  time_period VARCHAR(20),
  suggested_quota INT,
  suggestion_reason TEXT,
  status VARCHAR(20) NOT NULL DEFAULT '待确认',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmed_at DATETIME,
  CONSTRAINT fk_ai_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_ai_schedule_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_schedule_suggestion_detail (
  detail_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  suggestion_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  doctor_name VARCHAR(50) NOT NULL,
  schedule_date DATE NOT NULL,
  time_slot VARCHAR(20) NOT NULL,
  max_appointments INT NOT NULL,
  reason TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  CONSTRAINT fk_ai_schedule_detail_suggestion FOREIGN KEY (suggestion_id) REFERENCES ai_schedule_suggestion(suggestion_id),
  CONSTRAINT fk_ai_schedule_detail_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_consultation (
  consultation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  chief_complaint TEXT,
  symptom_detail TEXT,
  ai_summary TEXT,
  recommended_dept_id BIGINT,
  risk_level VARCHAR(20) NOT NULL DEFAULT '普通',
  ai_result TEXT,
  status VARCHAR(20) NOT NULL DEFAULT '已生成',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ai_consult_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_ai_consult_dept FOREIGN KEY (recommended_dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS registration (
  registration_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  consultation_id BIGINT,
  dept_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  schedule_id BIGINT,
  operator_user_id BIGINT,
  source VARCHAR(20) NOT NULL DEFAULT '线下',
  registration_no VARCHAR(50) NOT NULL UNIQUE,
  queue_no INT,
  registration_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fee_status VARCHAR(20) NOT NULL DEFAULT '待支付',
  status VARCHAR(30) NOT NULL DEFAULT '待支付',
  registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  called_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_registration_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_registration_consultation FOREIGN KEY (consultation_id) REFERENCES ai_consultation(consultation_id),
  CONSTRAINT fk_registration_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id),
  CONSTRAINT fk_registration_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_registration_schedule FOREIGN KEY (schedule_id) REFERENCES doctor_schedule(schedule_id),
  CONSTRAINT fk_registration_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS triage_record (
  triage_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  consultation_id BIGINT,
  registration_id BIGINT,
  triage_doctor_id BIGINT,
  recommended_dept_id BIGINT,
  chief_complaint TEXT,
  risk_level VARCHAR(20) NOT NULL DEFAULT '普通',
  triage_result TEXT,
  status VARCHAR(20) NOT NULL DEFAULT '已分诊',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_triage_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_triage_consultation FOREIGN KEY (consultation_id) REFERENCES ai_consultation(consultation_id),
  CONSTRAINT fk_triage_registration FOREIGN KEY (registration_id) REFERENCES registration(registration_id),
  CONSTRAINT fk_triage_doctor FOREIGN KEY (triage_doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_triage_dept FOREIGN KEY (recommended_dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS outpatient_visit (
  visit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  registration_id BIGINT NOT NULL UNIQUE,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  visit_no VARCHAR(50) NOT NULL UNIQUE,
  queue_no INT,
  status VARCHAR(30) NOT NULL DEFAULT '待接诊',
  started_at DATETIME,
  finished_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_visit_registration FOREIGN KEY (registration_id) REFERENCES registration(registration_id),
  CONSTRAINT fk_visit_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_visit_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_visit_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fee_order (
  fee_order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) NOT NULL UNIQUE,
  patient_id BIGINT NOT NULL,
  registration_id BIGINT,
  visit_id BIGINT,
  business_type VARCHAR(30) NOT NULL,
  business_id BIGINT,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL DEFAULT '待支付',
  created_by BIGINT,
  paid_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fee_order_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_fee_order_registration FOREIGN KEY (registration_id) REFERENCES registration(registration_id),
  CONSTRAINT fk_fee_order_visit FOREIGN KEY (visit_id) REFERENCES outpatient_visit(visit_id),
  CONSTRAINT fk_fee_order_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fee_order_item (
  fee_order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fee_order_id BIGINT NOT NULL,
  item_type VARCHAR(30) NOT NULL,
  item_id BIGINT,
  item_code VARCHAR(50),
  item_name VARCHAR(100) NOT NULL,
  item_spec VARCHAR(100),
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL DEFAULT '待支付',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_fee_item_order FOREIGN KEY (fee_order_id) REFERENCES fee_order(fee_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_record (
  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fee_order_id BIGINT NOT NULL,
  payment_no VARCHAR(50) NOT NULL UNIQUE,
  payment_method VARCHAR(30) NOT NULL,
  payment_amount DECIMAL(10,2) NOT NULL,
  payer_name VARCHAR(50),
  status VARCHAR(20) NOT NULL DEFAULT '成功',
  paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  operator_user_id BIGINT,
  remark VARCHAR(255),
  CONSTRAINT fk_payment_order FOREIGN KEY (fee_order_id) REFERENCES fee_order(fee_order_id),
  CONSTRAINT fk_payment_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refund_record (
  refund_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fee_order_id BIGINT NOT NULL,
  payment_id BIGINT,
  refund_no VARCHAR(50) NOT NULL UNIQUE,
  refund_type VARCHAR(30) NOT NULL,
  refund_amount DECIMAL(10,2) NOT NULL,
  reason VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT '申请中',
  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  operator_user_id BIGINT,
  CONSTRAINT fk_refund_order FOREIGN KEY (fee_order_id) REFERENCES fee_order(fee_order_id),
  CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payment_record(payment_id),
  CONSTRAINT fk_refund_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS medical_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  visit_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  chief_complaint TEXT,
  present_illness TEXT,
  current_treatment TEXT,
  past_history TEXT,
  allergy_history TEXT,
  physical_exam TEXT,
  auxiliary_exam TEXT,
  diagnosis TEXT,
  treatment_advice TEXT,
  doctor_note TEXT,
  final_diagnosis TEXT,
  final_opinion TEXT,
  confirmed_doctor_id BIGINT,
  confirmed_at DATETIME,
  status VARCHAR(30) NOT NULL DEFAULT '初诊暂存',
  initial_saved_at DATETIME,
  completed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_record_visit FOREIGN KEY (visit_id) REFERENCES outpatient_visit(visit_id),
  CONSTRAINT fk_record_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_record_confirm_doctor FOREIGN KEY (confirmed_doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS medical_item (
  item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_code VARCHAR(50) NOT NULL UNIQUE,
  item_name VARCHAR(100) NOT NULL,
  item_type VARCHAR(30) NOT NULL,
  dept_id BIGINT,
  unit VARCHAR(20),
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  sample_type VARCHAR(50),
  clinical_meaning TEXT,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_medical_item_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_lab_order (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) NOT NULL UNIQUE,
  visit_id BIGINT NOT NULL,
  record_id BIGINT,
  patient_id BIGINT NOT NULL,
  apply_doctor_id BIGINT NOT NULL,
  execute_dept_id BIGINT NOT NULL,
  order_type VARCHAR(20) NOT NULL,
  clinical_diagnosis TEXT,
  purpose TEXT,
  exam_site VARCHAR(255),
  specimen_type VARCHAR(50),
  remark TEXT,
  priority VARCHAR(20),
  collection_way VARCHAR(50),
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fee_status VARCHAR(20) NOT NULL DEFAULT '待支付',
  status VARCHAR(30) NOT NULL DEFAULT '待缴费',
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  executed_at DATETIME,
  completed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_order_visit FOREIGN KEY (visit_id) REFERENCES outpatient_visit(visit_id),
  CONSTRAINT fk_exam_order_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id),
  CONSTRAINT fk_exam_order_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_exam_order_doctor FOREIGN KEY (apply_doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_exam_order_dept FOREIGN KEY (execute_dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_lab_order_item (
  order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  item_name VARCHAR(100) NOT NULL,
  item_type VARCHAR(30) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(30) NOT NULL DEFAULT '待缴费',
  executed_at DATETIME,
  result_summary TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_item_order FOREIGN KEY (order_id) REFERENCES exam_lab_order(order_id),
  CONSTRAINT fk_exam_item_item FOREIGN KEY (item_id) REFERENCES medical_item(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_lab_report (
  report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_item_id BIGINT,
  patient_id BIGINT NOT NULL,
  report_doctor_id BIGINT NOT NULL,
  report_no VARCHAR(50) NOT NULL UNIQUE,
  report_type VARCHAR(20) NOT NULL,
  findings TEXT,
  conclusion TEXT,
  ai_draft TEXT,
  doctor_review TEXT,
  status VARCHAR(20) NOT NULL DEFAULT '草稿',
  published_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_report_order FOREIGN KEY (order_id) REFERENCES exam_lab_order(order_id),
  CONSTRAINT fk_report_order_item FOREIGN KEY (order_item_id) REFERENCES exam_lab_order_item(order_item_id),
  CONSTRAINT fk_report_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_report_doctor FOREIGN KEY (report_doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_result_feature (
  feature_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT,
  order_item_id BIGINT NOT NULL,
  feature_name VARCHAR(100) NOT NULL,
  feature_value VARCHAR(500) NOT NULL,
  unit VARCHAR(50),
  abnormal_flag VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_feature_report FOREIGN KEY (report_id) REFERENCES exam_lab_report(report_id),
  CONSTRAINT fk_exam_feature_order_item FOREIGN KEY (order_item_id) REFERENCES exam_lab_order_item(order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lab_result_item (
  result_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT,
  order_item_id BIGINT NOT NULL,
  item_code VARCHAR(50) NOT NULL,
  indicator_code VARCHAR(50) NOT NULL,
  indicator_name VARCHAR(100) NOT NULL,
  result_value VARCHAR(100) NOT NULL,
  unit VARCHAR(50),
  reference_range VARCHAR(100),
  abnormal_flag VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_lab_result_report FOREIGN KEY (report_id) REFERENCES exam_lab_report(report_id),
  CONSTRAINT fk_lab_result_order_item FOREIGN KEY (order_item_id) REFERENCES exam_lab_order_item(order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS drug (
  drug_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  drug_code VARCHAR(50) NOT NULL UNIQUE,
  drug_name VARCHAR(100) NOT NULL,
  specification VARCHAR(100),
  dosage_form VARCHAR(50),
  manufacturer VARCHAR(100),
  unit VARCHAR(20) NOT NULL,
  sale_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  warning_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  contraindication TEXT,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prescription (
  prescription_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_no VARCHAR(50) NOT NULL UNIQUE,
  visit_id BIGINT NOT NULL,
  record_id BIGINT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fee_status VARCHAR(20) NOT NULL DEFAULT '待支付',
  audit_status VARCHAR(20) NOT NULL DEFAULT '待审核',
  status VARCHAR(30) NOT NULL DEFAULT '待缴费',
  diagnosis TEXT,
  usage_note TEXT,
  audit_doctor_id BIGINT,
  audit_note VARCHAR(255),
  audited_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_prescription_visit FOREIGN KEY (visit_id) REFERENCES outpatient_visit(visit_id),
  CONSTRAINT fk_prescription_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id),
  CONSTRAINT fk_prescription_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_prescription_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_prescription_audit_doctor FOREIGN KEY (audit_doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prescription_item (
  prescription_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_id BIGINT NOT NULL,
  drug_id BIGINT NOT NULL,
  drug_name VARCHAR(100) NOT NULL,
  specification VARCHAR(100),
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  dosage VARCHAR(100),
  frequency VARCHAR(100),
  usage_method VARCHAR(100),
  days INT,
  status VARCHAR(30) NOT NULL DEFAULT '待发药',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_prescription_item_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id),
  CONSTRAINT fk_prescription_item_drug FOREIGN KEY (drug_id) REFERENCES drug(drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pharmacy_dispense (
  dispense_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  prescription_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  pharmacy_doctor_id BIGINT NOT NULL,
  dispense_no VARCHAR(50) NOT NULL UNIQUE,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL DEFAULT '待发药',
  audit_note VARCHAR(255),
  dispensed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_dispense_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id),
  CONSTRAINT fk_dispense_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_dispense_doctor FOREIGN KEY (pharmacy_doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pharmacy_return (
  return_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dispense_id BIGINT NOT NULL,
  prescription_id BIGINT NOT NULL,
  drug_id BIGINT NOT NULL,
  return_no VARCHAR(50) NOT NULL UNIQUE,
  return_quantity DECIMAL(10,2) NOT NULL,
  return_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  reason VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT '申请中',
  operator_user_id BIGINT,
  returned_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_return_dispense FOREIGN KEY (dispense_id) REFERENCES pharmacy_dispense(dispense_id),
  CONSTRAINT fk_return_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id),
  CONSTRAINT fk_return_drug FOREIGN KEY (drug_id) REFERENCES drug(drug_id),
  CONSTRAINT fk_return_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS drug_stock_record (
  stock_record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  drug_id BIGINT NOT NULL,
  business_type VARCHAR(30) NOT NULL,
  business_id BIGINT,
  change_quantity DECIMAL(10,2) NOT NULL,
  before_quantity DECIMAL(10,2) NOT NULL,
  after_quantity DECIMAL(10,2) NOT NULL,
  operator_user_id BIGINT,
  remark VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_stock_record_drug FOREIGN KEY (drug_id) REFERENCES drug(drug_id),
  CONSTRAINT fk_stock_record_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_role_config (
  config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  provider VARCHAR(50) NOT NULL DEFAULT 'simulated',
  model_name VARCHAR(100) NOT NULL DEFAULT 'simulated-ai',
  api_key_ref VARCHAR(100) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  remark VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_prompt_template (
  template_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL,
  scene_code VARCHAR(50) NOT NULL,
  scene_name VARCHAR(80) NOT NULL,
  system_prompt TEXT NOT NULL,
  user_prompt_template TEXT NOT NULL,
  version VARCHAR(30) NOT NULL DEFAULT 'v1',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ai_prompt_role_scene_version (role_code, scene_code, version),
  CONSTRAINT fk_ai_prompt_role FOREIGN KEY (role_code) REFERENCES ai_role_config(role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_call_log (
  call_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  doctor_id BIGINT,
  patient_id BIGINT,
  role_code VARCHAR(50),
  scene_code VARCHAR(50),
  business_type VARCHAR(50) NOT NULL,
  business_id BIGINT,
  prompt TEXT,
  response TEXT,
  model_name VARCHAR(100) NOT NULL DEFAULT 'simulated-ai',
  api_key_ref VARCHAR(100),
  status VARCHAR(20) NOT NULL DEFAULT '成功',
  changed_business_status TINYINT(1) NOT NULL DEFAULT 0,
  error_message TEXT,
  started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  CONSTRAINT fk_ai_call_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
  CONSTRAINT fk_ai_call_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_ai_call_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  CONSTRAINT fk_ai_call_role FOREIGN KEY (role_code) REFERENCES ai_role_config(role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
