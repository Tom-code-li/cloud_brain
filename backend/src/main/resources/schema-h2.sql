CREATE TABLE sys_role (
  role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE patient (
  patient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  allergy_history CLOB,
  past_history CLOB,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE department (
  dept_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dept_code VARCHAR(50) NOT NULL UNIQUE,
  dept_name VARCHAR(100) NOT NULL,
  dept_type VARCHAR(30) NOT NULL,
  location VARCHAR(100),
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctor (
  doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  dept_id BIGINT NOT NULL,
  doctor_no VARCHAR(50) NOT NULL UNIQUE,
  doctor_type VARCHAR(50) NOT NULL,
  title VARCHAR(50),
  specialty VARCHAR(255),
  introduction CLOB,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctor_schedule (
  schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_schedule_suggestion (
  suggestion_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT,
  dept_id BIGINT,
  work_date DATE,
  time_period VARCHAR(20),
  suggested_quota INT,
  suggestion_reason CLOB,
  status VARCHAR(20) NOT NULL DEFAULT '待确认',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmed_at DATETIME
);

CREATE TABLE ai_consultation (
  consultation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  chief_complaint CLOB,
  symptom_detail CLOB,
  ai_summary CLOB,
  recommended_dept_id BIGINT,
  risk_level VARCHAR(20) NOT NULL DEFAULT '普通',
  ai_result CLOB,
  status VARCHAR(20) NOT NULL DEFAULT '已生成',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE registration (
  registration_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE triage_record (
  triage_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  consultation_id BIGINT,
  registration_id BIGINT,
  triage_doctor_id BIGINT,
  recommended_dept_id BIGINT,
  chief_complaint CLOB,
  risk_level VARCHAR(20) NOT NULL DEFAULT '普通',
  triage_result CLOB,
  status VARCHAR(20) NOT NULL DEFAULT '已分诊',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fee_order (
  fee_order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fee_order_item (
  fee_order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_record (
  payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fee_order_id BIGINT NOT NULL,
  payment_no VARCHAR(50) NOT NULL UNIQUE,
  payment_method VARCHAR(30) NOT NULL,
  payment_amount DECIMAL(10,2) NOT NULL,
  payer_name VARCHAR(50),
  status VARCHAR(20) NOT NULL DEFAULT '成功',
  paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  operator_user_id BIGINT,
  remark VARCHAR(255)
);

CREATE TABLE refund_record (
  refund_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fee_order_id BIGINT NOT NULL,
  payment_id BIGINT,
  refund_no VARCHAR(50) NOT NULL UNIQUE,
  refund_type VARCHAR(30) NOT NULL,
  refund_amount DECIMAL(10,2) NOT NULL,
  reason VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT '申请中',
  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  operator_user_id BIGINT
);

CREATE TABLE outpatient_visit (
  visit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medical_record (
  record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  visit_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  chief_complaint CLOB,
  present_illness CLOB,
  current_treatment CLOB,
  past_history CLOB,
  allergy_history CLOB,
  physical_exam CLOB,
  auxiliary_exam CLOB,
  diagnosis CLOB,
  treatment_advice CLOB,
  doctor_note CLOB,
  final_diagnosis CLOB,
  final_opinion CLOB,
  confirmed_doctor_id BIGINT,
  confirmed_at TIMESTAMP,
  status VARCHAR(30) NOT NULL DEFAULT '初诊暂存',
  initial_saved_at DATETIME,
  completed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medical_item (
  item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_code VARCHAR(50) NOT NULL UNIQUE,
  item_name VARCHAR(100) NOT NULL,
  item_type VARCHAR(30) NOT NULL,
  dept_id BIGINT,
  unit VARCHAR(20),
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  sample_type VARCHAR(50),
  clinical_meaning CLOB,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_lab_order (
  order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(50) NOT NULL UNIQUE,
  visit_id BIGINT NOT NULL,
  record_id BIGINT,
  patient_id BIGINT NOT NULL,
  apply_doctor_id BIGINT NOT NULL,
  execute_dept_id BIGINT NOT NULL,
  order_type VARCHAR(20) NOT NULL,
  clinical_diagnosis CLOB,
  purpose CLOB,
  exam_site VARCHAR(255),
  specimen_type VARCHAR(50),
  remark CLOB,
  priority VARCHAR(20),
  collection_way VARCHAR(50),
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fee_status VARCHAR(20) NOT NULL DEFAULT '待支付',
  status VARCHAR(30) NOT NULL DEFAULT '待缴费',
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  executed_at DATETIME,
  completed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_lab_order_item (
  order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  item_name VARCHAR(100) NOT NULL,
  item_type VARCHAR(30) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(30) NOT NULL DEFAULT '待缴费',
  executed_at DATETIME,
  result_summary CLOB,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_lab_report (
  report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  order_item_id BIGINT,
  patient_id BIGINT NOT NULL,
  report_doctor_id BIGINT NOT NULL,
  report_no VARCHAR(50) NOT NULL UNIQUE,
  report_type VARCHAR(20) NOT NULL,
  findings CLOB,
  conclusion CLOB,
  ai_draft CLOB,
  doctor_review CLOB,
  status VARCHAR(20) NOT NULL DEFAULT '草稿',
  published_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_result_feature (
  feature_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT,
  order_item_id BIGINT NOT NULL,
  feature_name VARCHAR(100) NOT NULL,
  feature_value VARCHAR(500) NOT NULL,
  unit VARCHAR(50),
  abnormal_flag VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lab_result_item (
  result_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE drug (
  drug_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  drug_code VARCHAR(50) NOT NULL UNIQUE,
  drug_name VARCHAR(100) NOT NULL,
  specification VARCHAR(100),
  dosage_form VARCHAR(50),
  manufacturer VARCHAR(100),
  unit VARCHAR(20) NOT NULL,
  sale_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  warning_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  contraindication CLOB,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prescription (
  prescription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  prescription_no VARCHAR(50) NOT NULL UNIQUE,
  visit_id BIGINT NOT NULL,
  record_id BIGINT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fee_status VARCHAR(20) NOT NULL DEFAULT '待支付',
  audit_status VARCHAR(20) NOT NULL DEFAULT '待审核',
  status VARCHAR(30) NOT NULL DEFAULT '待缴费',
  diagnosis CLOB,
  usage_note CLOB,
  audit_doctor_id BIGINT,
  audit_note VARCHAR(255),
  audited_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prescription_item (
  prescription_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pharmacy_dispense (
  dispense_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  prescription_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  pharmacy_doctor_id BIGINT NOT NULL,
  dispense_no VARCHAR(50) NOT NULL UNIQUE,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL DEFAULT '待发药',
  audit_note VARCHAR(255),
  dispensed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pharmacy_return (
  return_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE drug_stock_record (
  stock_record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  drug_id BIGINT NOT NULL,
  business_type VARCHAR(30) NOT NULL,
  business_id BIGINT,
  change_quantity DECIMAL(10,2) NOT NULL,
  before_quantity DECIMAL(10,2) NOT NULL,
  after_quantity DECIMAL(10,2) NOT NULL,
  operator_user_id BIGINT,
  remark VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_role_config (
  config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  provider VARCHAR(50) NOT NULL DEFAULT 'simulated',
  model_name VARCHAR(100) NOT NULL DEFAULT 'simulated-ai',
  api_key_ref VARCHAR(100) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_prompt_template (
  template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(50) NOT NULL,
  scene_code VARCHAR(50) NOT NULL,
  scene_name VARCHAR(80) NOT NULL,
  system_prompt CLOB NOT NULL,
  user_prompt_template CLOB NOT NULL,
  version VARCHAR(30) NOT NULL DEFAULT 'v1',
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_call_log (
  call_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  doctor_id BIGINT,
  patient_id BIGINT,
  role_code VARCHAR(50),
  scene_code VARCHAR(50),
  business_type VARCHAR(50) NOT NULL,
  business_id BIGINT,
  prompt CLOB,
  response CLOB,
  model_name VARCHAR(100) NOT NULL DEFAULT 'simulated-ai',
  api_key_ref VARCHAR(100),
  status VARCHAR(20) NOT NULL DEFAULT '成功',
  changed_business_status TINYINT NOT NULL DEFAULT 0,
  error_message CLOB,
  started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME
);
