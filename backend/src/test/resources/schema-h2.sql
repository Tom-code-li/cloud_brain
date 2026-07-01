DROP TABLE IF EXISTS lab_result_item;
DROP TABLE IF EXISTS exam_result_feature;
DROP TABLE IF EXISTS exam_lab_report;
DROP TABLE IF EXISTS exam_lab_order_item;
DROP TABLE IF EXISTS exam_lab_order;
DROP TABLE IF EXISTS outpatient_visit;
DROP TABLE IF EXISTS medical_item;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS department;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  user_id BIGINT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  status TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE patient (
  patient_id BIGINT PRIMARY KEY,
  patient_no VARCHAR(50) NOT NULL,
  patient_name VARCHAR(50) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  birthday DATE,
  allergy_history TEXT,
  past_history TEXT,
  status TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE department (
  dept_id BIGINT PRIMARY KEY,
  dept_code VARCHAR(50) NOT NULL,
  dept_name VARCHAR(100) NOT NULL,
  dept_type VARCHAR(30) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE doctor (
  doctor_id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  doctor_no VARCHAR(50) NOT NULL,
  doctor_type VARCHAR(50) NOT NULL,
  title VARCHAR(50),
  specialty VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE medical_item (
  item_id BIGINT PRIMARY KEY,
  item_code VARCHAR(50) NOT NULL,
  item_name VARCHAR(100) NOT NULL,
  item_type VARCHAR(30) NOT NULL,
  dept_id BIGINT,
  unit VARCHAR(20),
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  sample_type VARCHAR(50),
  clinical_meaning TEXT,
  status TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE outpatient_visit (
  visit_id BIGINT PRIMARY KEY,
  registration_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  visit_no VARCHAR(50) NOT NULL,
  queue_no INT,
  status VARCHAR(30) NOT NULL DEFAULT '待接诊',
  started_at DATETIME,
  finished_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_lab_order (
  order_id BIGINT PRIMARY KEY,
  order_no VARCHAR(50) NOT NULL,
  visit_id BIGINT NOT NULL,
  record_id BIGINT,
  patient_id BIGINT NOT NULL,
  apply_doctor_id BIGINT NOT NULL,
  execute_dept_id BIGINT NOT NULL,
  order_type VARCHAR(20) NOT NULL,
  clinical_diagnosis TEXT,
  purpose TEXT,
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
  order_item_id BIGINT PRIMARY KEY,
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
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
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
  item_code VARCHAR(50),
  indicator_code VARCHAR(50) NOT NULL,
  indicator_name VARCHAR(100) NOT NULL,
  result_value VARCHAR(100) NOT NULL,
  unit VARCHAR(50),
  reference_range VARCHAR(100),
  abnormal_flag VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_lab_report (
  report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  order_item_id BIGINT,
  patient_id BIGINT NOT NULL,
  report_doctor_id BIGINT NOT NULL,
  report_no VARCHAR(50) NOT NULL,
  report_type VARCHAR(20) NOT NULL,
  findings TEXT,
  conclusion TEXT,
  ai_draft TEXT,
  doctor_review TEXT,
  status VARCHAR(20) NOT NULL DEFAULT '草稿',
  published_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
