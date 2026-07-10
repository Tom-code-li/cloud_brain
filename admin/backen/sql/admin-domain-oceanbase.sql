CREATE TABLE IF NOT EXISTS sys_role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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
);

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
);

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
);

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
);

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
);

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
);
