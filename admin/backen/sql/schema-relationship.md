# 数据库表结构关系说明

数据库名称：`doctor_platform`

> **说明**：本文档描述的数据库结构基于 `schema.sql`（基础建表）+ `change.sql`（增量变更）的合并结果。
> 后端代码（JPA 实体）与数据库结构已核对一致，未发现多余字段。

## 一、核心表关系概览

以下是各表的主外键关系图（箭头表示外键依赖方向）：

```text
sys_role
   ↑
   │
sys_user ─────────────────────────────┐
   ↑                                  │
   │ (user_id)                        │ (operator_user_id)
   │                                  │
doctor ────────────────┐              │
   │                   │              │
   │ (doctor_id)       │              │
   │                   │              │
   ├──────── doctor_schedule          │
   │                   │              │
   │ (doctor_id)       │              │
   │                   │              │
   ├──────── ai_schedule_suggestion   │
   │              │                   │
   │              │ (suggestion_id)   │
   │              ↓                   │
   │         ai_schedule_suggestion_detail
   │                                  │
   ├──────── registration ────────────┘
   │           │
   │           │ (registration_id)
   │           │
   │           ├─ outpatient_visit
   │           │        │
   │           │        │ (visit_id)
   │           │        │
   │           │        ├─ medical_record
   │           │        │
   │           │        ├─ exam_lab_order
   │           │        │        │
   │           │        │        ├─ exam_lab_order_item
   │           │        │        │
   │           │        │        └─ exam_lab_report
   │           │        │
   │           │        └─ prescription
   │           │                 │
   │           │                 ├─ prescription_item
   │           │                 │
   │           │                 └─ pharmacy_dispense
   │           │
   │           ├─ fee_order
   │           │      │
   │           │      ├─ payment_record
   │           │      │
   │           │      └─ refund_record
   │           │
   │           └─ triage_record
   │
   ├──────── ai_consultation
   │
   ├──────── drug
   │
   └──────── drug_stock_record

department（支持树形结构，parent_id 自引用）
   │ (dept_id)
   │
   ├── self (parent_id → dept_id)     ── 上级科室
   ├── doctor
   ├── doctor_schedule
   ├── ai_schedule_suggestion
   ├── registration
   ├── ai_consultation (recommended)
   ├── triage_record (recommended)
   ├── medical_item
   └── exam_lab_order

patient ──────────────────────────┐
   │ (patient_id)                  │
   │                               │
   ├─ registration                │
   ├─ outpatient_visit            │
   ├─ medical_record              │
   ├─ exam_lab_order              │
   ├─ fee_order                   │
   └─ ai_consultation
```

---

## 二、各表详细说明

### 1. 系统权限表

#### 1.1 sys_role（角色表）

**表说明**：定义系统中的用户角色，每个用户只能拥有一个角色。

> **change.sql 变更**：原角色 `DOCTOR` 已拆分为 `DOCTOR_CLINIC`（门诊医生，role_id=2）、`DOCTOR_INPATIENT`（住院医生，role_id=4）、`DOCTOR_CHIEF`（主任医师，role_id=5），以支持不同权限的医生登录后进入不同页面。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| role_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 角色ID（主键） |
| role_code | VARCHAR(50) | NOT NULL, UNIQUE | 角色编码（如 `ADMIN`、`DOCTOR_CLINIC`、`DOCTOR_INPATIENT`、`DOCTOR_CHIEF`） |
| role_name | VARCHAR(50) | NOT NULL | 角色名称（如"系统管理员"、"门诊医生"） |
| description | VARCHAR(255) | 可选 | 角色描述说明 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=启用，0=停用 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

---

#### 1.2 sys_user（用户表）

**表说明**：系统的核心用户表，存储所有登录系统的账号信息（管理员、医生、其他工作人员）。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| user_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户ID（主键） |
| role_id | BIGINT | NOT NULL, FK→sys_role.role_id | 角色ID（外键关联角色表） |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 登录用户名（唯一） |
| password | VARCHAR(255) | NOT NULL | 密码（加密存储） |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| phone | VARCHAR(20) | 可选 | 手机号码 |
| email | VARCHAR(100) | 可选 | 电子邮箱 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=启用，0=停用 |
| last_login_at | DATETIME | 可选 | 上次登录时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_sys_user_role`: role_id → sys_role.role_id

---

### 2. 组织架构表

#### 2.1 department（科室/部门表）

**表说明**：医院的科室或部门信息，支持多级树形结构（通过 `parent_id` 自引用）。医生、排班、挂号等都需要关联科室。

> **change.sql 变更**：新增 `parent_id`（上级科室ID，支持树形结构）、`floor`（楼层）、`phone`（科室电话）、`sort_order`（排序序号）四个字段，以及自引用外键 `fk_department_parent`。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| dept_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 科室ID（主键） |
| parent_id | BIGINT | 可选, FK→department.dept_id | 上级科室ID（NULL 表示顶级科室） |
| dept_code | VARCHAR(50) | NOT NULL, UNIQUE | 科室编码（如 `INTERNAL`、`SURGERY`） |
| dept_name | VARCHAR(100) | NOT NULL | 科室名称（如"内科"、"外科"） |
| dept_type | VARCHAR(30) | NOT NULL | 科室类型（区分诊疗科室、行政科室等） |
| floor | VARCHAR(50) | 可选 | 楼层（如"3楼"） |
| phone | VARCHAR(30) | 可选 | 科室电话 |
| location | VARCHAR(100) | 可选 | 位置描述（如"门诊楼3楼"） |
| description | VARCHAR(255) | 可选 | 科室简介 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=启用，0=停用 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序序号（数值越小越靠前） |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_department_parent`: parent_id → department.dept_id（自引用）

---

### 3. 人员信息表

#### 3.1 patient（患者表）

**表说明**：患者信息表，记录到医院就诊的病人基本信息。一个患者可以有多个挂号记录。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| patient_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 患者ID（主键） |
| user_id | BIGINT | 可选, FK→sys_user.user_id | 关联的系统用户ID（如果患者有登录账号） |
| patient_no | VARCHAR(50) | NOT NULL, UNIQUE | 患者编号（医院内部编号） |
| patient_name | VARCHAR(50) | NOT NULL | 患者姓名 |
| gender | VARCHAR(10) | NOT NULL | 性别 |
| birthday | DATE | 可选 | 出生日期 |
| id_card | VARCHAR(30) | UNIQUE | 身份证号（可选，唯一） |
| phone | VARCHAR(20) | 可选 | 联系电话 |
| emergency_contact | VARCHAR(50) | 可选 | 紧急联系人 |
| emergency_phone | VARCHAR(20) | 可选 | 紧急联系电话 |
| address | VARCHAR(255) | 可选 | 住址 |
| allergy_history | TEXT | 可选 | 过敏史 |
| past_history | TEXT | 可选 | 既往病史 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=正常，0=停用 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_patient_user`: user_id → sys_user.user_id

---

#### 3.2 doctor（医生表）

**表说明**：医生信息表，包含医生的专业信息、所属科室等。每个医生必须关联一个系统用户账号。

> **change.sql 变更**：已移除冗余的 `doctor_name` 字段（医生姓名由 `sys_user.real_name` 提供）。

> **设计说明**：本表通过 `user_id` 与 `sys_user` 关联，实现医生信息与登录账号的分离管理。登录认证使用 `sys_user` 的账号密码，医生的专业信息（职称、专长等）存储在此表中。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| doctor_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 医生ID（主键） |
| user_id | BIGINT | NOT NULL, UNIQUE, FK→sys_user.user_id | 关联的系统用户ID（每个医生唯一对应一个账号） |
| dept_id | BIGINT | NOT NULL, FK→department.dept_id | 所属科室ID |
| doctor_no | VARCHAR(50) | NOT NULL, UNIQUE | 医生工号（医院内部编号） |
| doctor_type | VARCHAR(50) | NOT NULL | 医生类型（如"门诊医生"、"住院医生"） |
| title | VARCHAR(50) | 可选 | 职称（如"主任医师"、"副主任医师"） |
| specialty | VARCHAR(255) | 可选 | 专长（擅长的诊疗方向） |
| introduction | TEXT | 可选 | 医生简介 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=启用，0=停用 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_doctor_user`: user_id → sys_user.user_id
- `fk_doctor_dept`: dept_id → department.dept_id

---

### 4. 挂号与排班表

#### 4.1 doctor_schedule（医生排班表）

**表说明**：医生的出诊排班信息，记录每位医生在特定日期和时段的出诊安排和号源数量。

> **change.sql 变更**：
> 1. `status` 默认值从 `'可预约'` 改为 `'AVAILABLE'`（英文枚举），现有中文数据已迁移为对应英文值（可预约→AVAILABLE，已取消/停诊→CANCELLED）。
> 2. 新增 `source` 字段，用于区分排班是人工创建（MANUAL）还是 AI 推荐（AI_SUGGESTED）。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| schedule_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 排班ID（主键） |
| doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 医生ID |
| dept_id | BIGINT | NOT NULL, FK→department.dept_id | 科室ID（冗余字段，方便查询） |
| work_date | DATE | NOT NULL | 出诊日期 |
| time_period | VARCHAR(20) | NOT NULL | 时段（如"上午"、"下午"） |
| start_time | TIME | 可选 | 开始时间（精确时间） |
| end_time | TIME | 可选 | 结束时间（精确时间） |
| total_quota | INT | NOT NULL, DEFAULT 0 | 总号源数 |
| remain_quota | INT | NOT NULL, DEFAULT 0 | 剩余号源数 |
| registration_fee | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 挂号费 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'AVAILABLE' | 状态（`AVAILABLE`/`FULL`/`CANCELLED`） |
| source | VARCHAR(30) | NOT NULL, DEFAULT 'MANUAL' | 排班来源（`MANUAL`=人工创建，`AI_SUGGESTED`=AI推荐） |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一约束**：
- `uk_doctor_schedule`: (doctor_id, work_date, time_period) 同一医生在同一天同一时段只能有一条排班记录

**外键关系**：
- `fk_schedule_doctor`: doctor_id → doctor.doctor_id
- `fk_schedule_dept`: dept_id → department.dept_id

---

#### 4.2 ai_schedule_suggestion（AI排班建议表）

**表说明**：系统基于AI分析生成的排班建议，供管理员参考调整排班计划。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| suggestion_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 建议ID（主键） |
| doctor_id | BIGINT | 可选, FK→doctor.doctor_id | 目标医生ID |
| dept_id | BIGINT | 可选, FK→department.dept_id | 目标科室ID |
| work_date | DATE | 可选 | 建议的出诊日期 |
| time_period | VARCHAR(20) | 可选 | 建议的时段 |
| suggested_quota | INT | 可选 | 建议的号源数量 |
| suggestion_reason | TEXT | 可选 | 建议理由/分析说明 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '待确认' | 状态（如"待确认"、"已采纳"、"已忽略"） |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| confirmed_at | DATETIME | 可选 | 确认时间 |

**外键关系**：
- `fk_ai_schedule_doctor`: doctor_id → doctor.doctor_id
- `fk_ai_schedule_dept`: dept_id → department.dept_id

---

#### 4.3 ai_schedule_suggestion_detail（AI排班建议明细表）

**表说明**：`change.sql` 新增表。一条 AI 排班建议可能包含多条建议明细（不同医生/不同时段），供管理员逐条确认或忽略。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| detail_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 明细ID（主键） |
| suggestion_id | BIGINT | NOT NULL, FK→ai_schedule_suggestion.suggestion_id | 关联的建议ID |
| doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 医生ID |
| doctor_name | VARCHAR(50) | NOT NULL | 医生姓名（冗余字段） |
| schedule_date | DATE | NOT NULL | 排班日期 |
| time_slot | VARCHAR(20) | NOT NULL | 时段 |
| max_appointments | INT | NOT NULL | 建议最大预约数 |
| reason | TEXT | 可选 | 建议理由 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 状态（`PENDING`=待确认，`ACCEPTED`=已采纳，`REJECTED`=已忽略） |

**外键关系**：
- `fk_ai_schedule_detail_suggestion`: suggestion_id → ai_schedule_suggestion.suggestion_id
- `fk_ai_schedule_detail_doctor`: doctor_id → doctor.doctor_id

---

### 5. 就诊流程表

#### 5.1 registration（挂号记录表）

**表说明**：患者的挂号记录，是整个就诊流程的起点。每次挂号对应一次就诊。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| registration_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 挂号ID（主键） |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| consultation_id | BIGINT | 可选, FK→ai_consultation.consultation_id | 关联的AI咨询记录ID |
| dept_id | BIGINT | NOT NULL, FK→department.dept_id | 挂号科室ID |
| doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 挂号医生ID |
| schedule_id | BIGINT | 可选, FK→doctor_schedule.schedule_id | 关联的排班ID |
| operator_user_id | BIGINT | 可选, FK→sys_user.user_id | 操作人（挂号登记员）ID |
| source | VARCHAR(20) | NOT NULL, DEFAULT '线下' | 挂号来源（如"线上"、"线下"、"手机APP"） |
| registration_no | VARCHAR(50) | NOT NULL, UNIQUE | 挂号编号 |
| queue_no | INT | 可选 | 排队序号 |
| registration_fee | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 挂号费 |
| fee_status | VARCHAR(20) | NOT NULL, DEFAULT '待支付' | 费用状态（如"待支付"、"已支付"） |
| status | VARCHAR(30) | NOT NULL, DEFAULT '待支付' | 挂号状态（如"待支付"、"已挂号"、"已就诊"、"已退号"） |
| registered_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 挂号时间 |
| called_at | DATETIME | 可选 | 叫号时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_registration_patient`: patient_id → patient.patient_id
- `fk_registration_consultation`: consultation_id → ai_consultation.consultation_id
- `fk_registration_dept`: dept_id → department.dept_id
- `fk_registration_doctor`: doctor_id → doctor.doctor_id
- `fk_registration_schedule`: schedule_id → doctor_schedule.schedule_id
- `fk_registration_operator`: operator_user_id → sys_user.user_id

---

#### 5.2 triage_record（分诊记录表）

**表说明**：患者的分诊信息，在正式挂号前由分诊医生根据病情判断推荐合适的科室和医生。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| triage_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 分诊ID（主键） |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| consultation_id | BIGINT | 可选, FK→ai_consultation.consultation_id | 关联的AI咨询ID |
| registration_id | BIGINT | 可选, FK→registration.registration_id | 关联的挂号ID |
| triage_doctor_id | BIGINT | 可选, FK→doctor.doctor_id | 分诊医生ID |
| recommended_dept_id | BIGINT | 可选, FK→department.dept_id | 推荐科室ID |
| chief_complaint | TEXT | 可选 | 主诉（患者描述的症状） |
| risk_level | VARCHAR(20) | NOT NULL, DEFAULT '普通' | 风险等级（如"普通"、"紧急"、"危重"） |
| triage_result | TEXT | 可选 | 分诊结果/建议 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '已分诊' | 状态 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_triage_patient`: patient_id → patient.patient_id
- `fk_triage_consultation`: consultation_id → ai_consultation.consultation_id
- `fk_triage_registration`: registration_id → registration.registration_id
- `fk_triage_doctor`: triage_doctor_id → doctor.doctor_id
- `fk_triage_dept`: recommended_dept_id → department.dept_id

---

### 6. AI辅助诊断表

#### 6.1 ai_consultation（AI咨询表）

**表说明**：记录患者通过AI系统进行的在线问诊信息，包括症状描述、AI分析结果和推荐科室。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| consultation_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 咨询ID（主键） |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| chief_complaint | TEXT | 可选 | 主诉 |
| symptom_detail | TEXT | 可选 | 症状详情 |
| ai_summary | TEXT | 可选 | AI生成的症状摘要 |
| recommended_dept_id | BIGINT | 可选, FK→department.dept_id | AI推荐的科室ID |
| risk_level | VARCHAR(20) | NOT NULL, DEFAULT '普通' | AI判断的风险等级 |
| ai_result | TEXT | 可选 | AI完整分析结果 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '已生成' | 状态 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_ai_consult_patient`: patient_id → patient.patient_id
- `fk_ai_consult_dept`: recommended_dept_id → department.dept_id

---

### 7. 门诊就诊表

#### 7.1 outpatient_visit（门诊就诊表）

**表说明**：患者实际就诊的记录，每次挂号对应一次就诊，包含接诊医生、接诊时间等核心信息。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| visit_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 就诊ID（主键） |
| registration_id | BIGINT | NOT NULL, UNIQUE, FK→registration.registration_id | 挂号ID（一个挂号对应一次就诊） |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 接诊医生ID |
| dept_id | BIGINT | NOT NULL, FK→department.dept_id | 就诊科室ID |
| visit_no | VARCHAR(50) | NOT NULL, UNIQUE | 就诊编号 |
| queue_no | INT | 可选 | 接诊序号 |
| status | VARCHAR(30) | NOT NULL, DEFAULT '待接诊' | 就诊状态（如"待接诊"、"接诊中"、"已结束"） |
| started_at | DATETIME | 可选 | 接诊开始时间 |
| finished_at | DATETIME | 可选 | 接诊结束时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_visit_registration`: registration_id → registration.registration_id
- `fk_visit_patient`: patient_id → patient.patient_id
- `fk_visit_doctor`: doctor_id → doctor.doctor_id
- `fk_visit_dept`: dept_id → department.dept_id

---

#### 7.2 medical_record（病历表）

**表说明**：医生书写的患者病历记录，包含诊断结果、医嘱等核心医疗信息。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| record_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 病历ID（主键） |
| visit_id | BIGINT | NOT NULL, FK→outpatient_visit.visit_id | 关联的就诊ID |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 书写病历的医生ID |
| chief_complaint | TEXT | 可选 | 主诉 |
| present_illness | TEXT | 可选 | 现病史 |
| past_history | TEXT | 可选 | 既往史 |
| allergy_history | TEXT | 可选 | 过敏史 |
| physical_exam | TEXT | 可选 | 体格检查 |
| auxiliary_exam | TEXT | 可选 | 辅助检查 |
| diagnosis | TEXT | 可选 | 诊断结果 |
| treatment_advice | TEXT | 可选 | 治疗建议 |
| doctor_note | TEXT | 可选 | 医生备注 |
| status | VARCHAR(30) | NOT NULL, DEFAULT '初诊暂存' | 状态（如"初诊暂存"、"已完成"） |
| initial_saved_at | DATETIME | 可选 | 初次保存时间 |
| completed_at | DATETIME | 可选 | 完成时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_record_visit`: visit_id → outpatient_visit.visit_id
- `fk_record_patient`: patient_id → patient.patient_id
- `fk_record_doctor`: doctor_id → doctor.doctor_id

---

### 8. 检查检验表

#### 8.1 medical_item（医疗项目表）

**表说明**：医院提供的检查/检验项目目录，如血常规、X光检查等，作为检查检验订单的商品基础数据。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| item_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 项目ID（主键） |
| item_code | VARCHAR(50) | NOT NULL, UNIQUE | 项目编码 |
| item_name | VARCHAR(100) | NOT NULL | 项目名称 |
| item_type | VARCHAR(30) | NOT NULL | 项目类型（如"检查"、"检验"） |
| dept_id | BIGINT | 可选, FK→department.dept_id | 所属科室ID（执行科室） |
| unit | VARCHAR(20) | 可选 | 计量单位 |
| price | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 单价 |
| sample_type | VARCHAR(50) | 可选 | 样本类型（如"血液"、"尿液"） |
| clinical_meaning | TEXT | 可选 | 临床意义说明 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=启用，0=停用 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_medical_item_dept`: dept_id → department.dept_id

---

#### 8.2 exam_lab_order（检查检验订单表）

**表说明**：医生开具的检查/检验申请单，一次申请可以包含多个检查项目。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| order_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 订单ID（主键） |
| order_no | VARCHAR(50) | NOT NULL, UNIQUE | 订单编号 |
| visit_id | BIGINT | NOT NULL, FK→outpatient_visit.visit_id | 关联的就诊ID |
| record_id | BIGINT | 可选, FK→medical_record.record_id | 关联的病历ID |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| apply_doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 申请医生ID |
| execute_dept_id | BIGINT | NOT NULL, FK→department.dept_id | 执行科室ID |
| order_type | VARCHAR(20) | NOT NULL | 订单类型（如"检查"、"检验"） |
| clinical_diagnosis | TEXT | 可选 | 临床诊断 |
| purpose | TEXT | 可选 | 检查目的 |
| total_amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 总金额 |
| fee_status | VARCHAR(20) | NOT NULL, DEFAULT '待支付' | 费用状态（如"待支付"、"已支付"） |
| status | VARCHAR(30) | NOT NULL, DEFAULT '待缴费' | 状态（如"待缴费"、"待检查"、"已完成"） |
| applied_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 申请时间 |
| executed_at | DATETIME | 可选 | 执行时间 |
| completed_at | DATETIME | 可选 | 完成时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_exam_order_visit`: visit_id → outpatient_visit.visit_id
- `fk_exam_order_record`: record_id → medical_record.record_id
- `fk_exam_order_patient`: patient_id → patient.patient_id
- `fk_exam_order_doctor`: apply_doctor_id → doctor.doctor_id
- `fk_exam_order_dept`: execute_dept_id → department.dept_id

---

#### 8.3 exam_lab_order_item（检查检验明细表）

**表说明**：检查/检验订单的明细条目，一次订单可以包含多个具体的检查项目。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| order_item_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 明细ID（主键） |
| order_id | BIGINT | NOT NULL, FK→exam_lab_order.order_id | 关联的订单ID |
| item_id | BIGINT | NOT NULL, FK→medical_item.item_id | 关联的检查项目ID |
| item_name | VARCHAR(100) | NOT NULL | 项目名称（冗余字段，便于查询） |
| item_type | VARCHAR(30) | NOT NULL | 项目类型 |
| unit_price | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 单价 |
| quantity | DECIMAL(10,2) | NOT NULL, DEFAULT 1.00 | 数量 |
| amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 金额 |
| status | VARCHAR(30) | NOT NULL, DEFAULT '待缴费' | 状态 |
| executed_at | DATETIME | 可选 | 执行时间 |
| result_summary | TEXT | 可选 | 结果摘要 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_exam_item_order`: order_id → exam_lab_order.order_id
- `fk_exam_item_item`: item_id → medical_item.item_id

---

#### 8.4 exam_lab_report（检查检验报告表）

**表说明**：检查/检验的结果报告，记录报告内容和审核医生信息。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| report_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 报告ID（主键） |
| order_id | BIGINT | NOT NULL, FK→exam_lab_order.order_id | 关联的订单ID |
| order_item_id | BIGINT | 可选, FK→exam_lab_order_item.order_item_id | 关联的订单明细ID |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| report_doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 报告医生/审核医生ID |
| report_no | VARCHAR(50) | NOT NULL, UNIQUE | 报告编号 |
| report_type | VARCHAR(20) | NOT NULL | 报告类型 |
| findings | TEXT | 可选 | 检查所见 |
| conclusion | TEXT | 可选 | 诊断结论 |
| ai_draft | TEXT | 可选 | AI辅助生成的草稿 |
| doctor_review | TEXT | 可选 | 医生审核意见 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '草稿' | 状态（如"草稿"、"已发布"） |
| published_at | DATETIME | 可选 | 发布时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_report_order`: order_id → exam_lab_order.order_id
- `fk_report_order_item`: order_item_id → exam_lab_order_item.order_item_id
- `fk_report_patient`: patient_id → patient.patient_id
- `fk_report_doctor`: report_doctor_id → doctor.doctor_id

---

### 9. 药品表

#### 9.1 drug（药品表）

**表说明**：医院药品库存目录，记录药品基本信息和库存数量。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| drug_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 药品ID（主键） |
| drug_code | VARCHAR(50) | NOT NULL, UNIQUE | 药品编码 |
| drug_name | VARCHAR(100) | NOT NULL | 药品名称 |
| specification | VARCHAR(100) | 可选 | 规格（如"0.5g*24片"） |
| dosage_form | VARCHAR(50) | 可选 | 剂型（如"片剂"、"注射剂"） |
| manufacturer | VARCHAR(100) | 可选 | 生产厂家 |
| unit | VARCHAR(20) | NOT NULL | 单位（如"盒"、"瓶"） |
| sale_price | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 售价 |
| stock_quantity | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 库存数量 |
| warning_quantity | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 库存预警数量（低于此值需要补货） |
| contraindication | TEXT | 可选 | 禁忌症说明 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=上架，0=下架 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

---

### 10. 处方表

#### 10.1 prescription（处方表）

**表说明**：医生开具的处方信息，一张处方包含多种药品。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| prescription_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 处方ID（主键） |
| prescription_no | VARCHAR(50) | NOT NULL, UNIQUE | 处方编号 |
| visit_id | BIGINT | NOT NULL, FK→outpatient_visit.visit_id | 关联的就诊ID |
| record_id | BIGINT | 可选, FK→medical_record.record_id | 关联的病历ID |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 开方医生ID |
| total_amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 总金额 |
| fee_status | VARCHAR(20) | NOT NULL, DEFAULT '待支付' | 费用状态 |
| audit_status | VARCHAR(20) | NOT NULL, DEFAULT '待审核' | 审核状态（如"待审核"、"已审核"） |
| status | VARCHAR(30) | NOT NULL, DEFAULT '待缴费' | 状态（如"待缴费"、"待发药"、"已发药"） |
| diagnosis | TEXT | 可选 | 诊断 |
| usage_note | TEXT | 可选 | 使用说明 |
| audit_doctor_id | BIGINT | 可选, FK→doctor.doctor_id | 审核药师ID |
| audit_note | VARCHAR(255) | 可选 | 审核备注 |
| audited_at | DATETIME | 可选 | 审核时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_prescription_visit`: visit_id → outpatient_visit.visit_id
- `fk_prescription_record`: record_id → medical_record.record_id
- `fk_prescription_patient`: patient_id → patient.patient_id
- `fk_prescription_doctor`: doctor_id → doctor.doctor_id
- `fk_prescription_audit_doctor`: audit_doctor_id → doctor.doctor_id

---

#### 10.2 prescription_item（处方明细表）

**表说明**：处方中的具体药品条目明细。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| prescription_item_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 明细ID（主键） |
| prescription_id | BIGINT | NOT NULL, FK→prescription.prescription_id | 关联的处方ID |
| drug_id | BIGINT | NOT NULL, FK→drug.drug_id | 关联的药品ID |
| drug_name | VARCHAR(100) | NOT NULL | 药品名称（冗余字段） |
| specification | VARCHAR(100) | 可选 | 药品规格 |
| unit_price | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 单价 |
| quantity | DECIMAL(10,2) | NOT NULL, DEFAULT 1.00 | 数量 |
| amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 金额 |
| dosage | VARCHAR(100) | 可选 | 用法用量 |
| frequency | VARCHAR(100) | 可选 | 服用频率（如"每日3次"） |
| usage_method | VARCHAR(100) | 可选 | 使用方法（如"口服"、"外用"） |
| days | INT | 可选 | 使用天数 |
| status | VARCHAR(30) | NOT NULL, DEFAULT '待发药' | 状态 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_prescription_item_prescription`: prescription_id → prescription.prescription_id
- `fk_prescription_item_drug`: drug_id → drug.drug_id

---

#### 10.3 pharmacy_dispense（药房发药表）

**表说明**：患者到药房取药的记录，记录处方发药信息。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| dispense_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 发药ID（主键） |
| prescription_id | BIGINT | NOT NULL, FK→prescription.prescription_id | 关联的处方ID |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| pharmacy_doctor_id | BIGINT | NOT NULL, FK→doctor.doctor_id | 发药药师ID |
| dispense_no | VARCHAR(50) | NOT NULL, UNIQUE | 发药编号 |
| total_amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 发药总金额 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '待发药' | 状态（如"待发药"、"已发药"） |
| audit_note | VARCHAR(255) | 可选 | 发药备注 |
| dispensed_at | DATETIME | 可选 | 发药时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_dispense_prescription`: prescription_id → prescription.prescription_id
- `fk_dispense_patient`: patient_id → patient.patient_id
- `fk_dispense_doctor`: pharmacy_doctor_id → doctor.doctor_id

---

#### 10.4 pharmacy_return（药房退药表）

**表说明**：患者退药记录，记录退药的药品和数量。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| return_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 退药ID（主键） |
| dispense_id | BIGINT | NOT NULL, FK→pharmacy_dispense.dispense_id | 关联的发药ID |
| prescription_id | BIGINT | NOT NULL, FK→prescription.prescription_id | 关联的处方ID |
| drug_id | BIGINT | NOT NULL, FK→drug.drug_id | 关联的药品ID |
| return_no | VARCHAR(50) | NOT NULL, UNIQUE | 退药编号 |
| return_quantity | DECIMAL(10,2) | NOT NULL | 退药数量 |
| return_amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 退药金额 |
| reason | VARCHAR(255) | 可选 | 退药原因 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '申请中' | 状态（如"申请中"、"已完成"） |
| operator_user_id | BIGINT | 可选, FK→sys_user.user_id | 操作人ID |
| returned_at | DATETIME | 可选 | 退药完成时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_return_dispense`: dispense_id → pharmacy_dispense.dispense_id
- `fk_return_prescription`: prescription_id → prescription.prescription_id
- `fk_return_drug`: drug_id → drug.drug_id
- `fk_return_operator`: operator_user_id → sys_user.user_id

---

### 11. 费用订单表

#### 11.1 fee_order（费用订单表）

**表说明**：统一的费用订单，记录患者产生的各种费用（挂号费、检查费、检验费、药品费等）。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| fee_order_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 订单ID（主键） |
| order_no | VARCHAR(50) | NOT NULL, UNIQUE | 订单编号 |
| patient_id | BIGINT | NOT NULL, FK→patient.patient_id | 患者ID |
| registration_id | BIGINT | 可选, FK→registration.registration_id | 关联的挂号ID |
| business_type | VARCHAR(30) | NOT NULL | 业务类型（如"挂号"、"检查"、"检验"、"药品"） |
| business_id | BIGINT | 可选 | 关联业务记录ID（具体业务表的主键） |
| total_amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 订单总金额 |
| paid_amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 已支付金额 |
| refund_amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 已退款金额 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '待支付' | 订单状态（如"待支付"、"已支付"、"已退款"） |
| created_by | BIGINT | 可选, FK→sys_user.user_id | 创建人ID |
| paid_at | DATETIME | 可选 | 支付时间 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键关系**：
- `fk_fee_order_patient`: patient_id → patient.patient_id
- `fk_fee_order_registration`: registration_id → registration.registration_id
- `fk_fee_order_creator`: created_by → sys_user.user_id

---

#### 11.2 fee_order_item（费用明细表）

**表说明**：费用订单的明细条目，用于拆分不同费用项目。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| fee_order_item_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 明细ID（主键） |
| fee_order_id | BIGINT | NOT NULL, FK→fee_order.fee_order_id | 关联的费用订单ID |
| item_type | VARCHAR(30) | NOT NULL | 费用项目类型 |
| item_id | BIGINT | 可选 | 具体项目ID |
| item_name | VARCHAR(100) | NOT NULL | 项目名称 |
| unit_price | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 单价 |
| quantity | DECIMAL(10,2) | NOT NULL, DEFAULT 1.00 | 数量 |
| amount | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 金额 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '待支付' | 状态 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_fee_item_order`: fee_order_id → fee_order.fee_order_id

---

#### 11.3 payment_record（支付记录表）

**表说明**：患者的支付记录，记录每次支付的金额、方式和状态。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| payment_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 支付ID（主键） |
| fee_order_id | BIGINT | NOT NULL, FK→fee_order.fee_order_id | 关联的费用订单ID |
| payment_no | VARCHAR(50) | NOT NULL, UNIQUE | 支付编号 |
| payment_method | VARCHAR(30) | NOT NULL | 支付方式（如"现金"、"微信"、"支付宝"、"医保卡"） |
| payment_amount | DECIMAL(10,2) | NOT NULL | 支付金额 |
| payer_name | VARCHAR(50) | 可选 | 付款人姓名 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '成功' | 支付状态（如"成功"、"失败"、"处理中"） |
| paid_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 支付时间 |
| operator_user_id | BIGINT | 可选, FK→sys_user.user_id | 操作人ID（如收银员） |
| remark | VARCHAR(255) | 可选 | 备注 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_payment_order`: fee_order_id → fee_order.fee_order_id
- `fk_payment_operator`: operator_user_id → sys_user.user_id

---

#### 11.4 refund_record（退款记录表）

**表说明**：退款记录，记录患者退款的金额和原因。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| refund_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 退款ID（主键） |
| fee_order_id | BIGINT | NOT NULL, FK→fee_order.fee_order_id | 关联的费用订单ID |
| payment_id | BIGINT | 可选, FK→payment_record.payment_id | 关联的支付记录ID（退哪笔钱） |
| refund_no | VARCHAR(50) | NOT NULL, UNIQUE | 退款编号 |
| refund_type | VARCHAR(30) | NOT NULL | 退款类型（如"全额退款"、"部分退款"） |
| refund_amount | DECIMAL(10,2) | NOT NULL | 退款金额 |
| reason | VARCHAR(255) | 可选 | 退款原因 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '申请中' | 状态（如"申请中"、"已退款"、"已拒绝"） |
| requested_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 申请时间 |
| completed_at | DATETIME | 可选 | 退款完成时间 |
| operator_user_id | BIGINT | 可选, FK→sys_user.user_id | 操作人ID |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_refund_order`: fee_order_id → fee_order.fee_order_id
- `fk_refund_payment`: payment_id → payment_record.payment_id
- `fk_refund_operator`: operator_user_id → sys_user.user_id

---

### 12. 库存记录表

#### 12.1 drug_stock_record（药品库存记录表）

**表说明**：药品库存变动记录，用于追踪库存增减变化（入库、出库、发药、退药等）。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| stock_record_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 记录ID（主键） |
| drug_id | BIGINT | NOT NULL, FK→drug.drug_id | 关联的药品ID |
| business_type | VARCHAR(30) | NOT NULL | 业务类型（如"入库"、"出库"、"发药"、"退药"、"盘点"） |
| business_id | BIGINT | 可选 | 关联业务记录ID |
| change_quantity | DECIMAL(10,2) | NOT NULL | 变动数量（正数增加，负数减少） |
| before_quantity | DECIMAL(10,2) | NOT NULL | 变动前库存数量 |
| after_quantity | DECIMAL(10,2) | NOT NULL | 变动后库存数量 |
| operator_user_id | BIGINT | 可选, FK→sys_user.user_id | 操作人ID |
| remark | VARCHAR(255) | 可选 | 备注 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**外键关系**：
- `fk_stock_record_drug`: drug_id → drug.drug_id
- `fk_stock_record_operator`: operator_user_id → sys_user.user_id

---

### 13. AI调用日志表

#### 13.1 ai_call_log（AI调用日志表）

**表说明**：记录所有调用AI服务的操作日志，用于审计和调试。

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| call_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 调用ID（主键） |
| user_id | BIGINT | 可选, FK→sys_user.user_id | 调用用户ID |
| doctor_id | BIGINT | 可选, FK→doctor.doctor_id | 关联医生ID |
| patient_id | BIGINT | 可选, FK→patient.patient_id | 关联患者ID |
| business_type | VARCHAR(50) | NOT NULL | 业务类型（如"AI咨询"、"AI诊断"、"AI报告"） |
| business_id | BIGINT | 可选 | 关联业务记录ID |
| prompt | TEXT | 可选 | 发送给AI的提示词/输入 |
| response | TEXT | 可选 | AI返回的响应内容 |
| model_name | VARCHAR(100) | NOT NULL, DEFAULT 'simulated-ai' | 使用的AI模型名称 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '成功' | 调用状态（如"成功"、"失败"） |
| error_message | TEXT | 可选 | 错误信息（失败时记录） |
| started_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 调用开始时间 |
| completed_at | DATETIME | 可选 | 调用完成时间 |

**外键关系**：
- `fk_ai_call_user`: user_id → sys_user.user_id
- `fk_ai_call_doctor`: doctor_id → doctor.doctor_id
- `fk_ai_call_patient`: patient_id → patient.patient_id

---

## 三、核心业务流程

### 流程1：完整就诊流程

```text
患者 → 分诊(triage_record) / AI咨询(ai_consultation)
         ↓
     挂号(registration)
         ↓
     就诊(outpatient_visit)
         ↓
   ┌────┼────┐
   │    │    │
   ↓    ↓    ↓
病历  检查  处方
(medical_record) │ (exam_lab_order) │ (prescription)
                                        ↓
                                    发药(pharmacy_dispense)
                                        ↓
                                   费用结算(fee_order)
                                        ↓
                                   支付(payment_record)
```

### 流程2：AI排班建议流程

```text
AI 分析生成排班建议(ai_schedule_suggestion)
         ↓
建议明细(ai_schedule_suggestion_detail)
         ↓
管理员逐条确认/忽略（status: PENDING → ACCEPTED/REJECTED）
         ↓
采纳后生成正式排班(doctor_schedule, source='AI_SUGGESTED')
```

### 流程3：科室-医生-排班关系

```text
department(科室, 支持树形结构)
     ↓ 1:N
doctor(医生)
     ↓ 1:N
doctor_schedule(排班, 支持AI推荐来源)
     ↓ 1:N
registration(挂号)
```

### 流程4：库存变动流程

```text
drug(药品)
  ↓
drug_stock_record(库存变动记录)
  ↑ 记录每次库存变化
入库 ←─┐ ┌─→ 发药
        │ │
       盘点
        │ │
退药 →─┘ └─← 出库
```

## 四、关键设计说明

### 4.1 用户与角色分离

- **sys_user** 存储通用账号信息（登录凭据、联系信息）
- **doctor** 存储医生专业信息（职称、专长、所属科室）
- 通过 `user_id` 关联，一个账号对应一个医生身份

### 4.2 角色体系

系统角色通过 `change.sql` 演进为更细粒度的权限划分：

| role_id | role_code | role_name | 说明 |
|---------|-----------|-----------|------|
| 1 | ADMIN | 系统管理员 | 最高权限 |
| 2 | DOCTOR_CLINIC | 门诊医生 | 基础门诊医生，处理门诊排班 |
| 3 | PHARMACY | 药房人员 | 药房发药、退药操作 |
| 4 | EXAM_DOCTOR | 检查医生 | 负责检查操作（放射科、超声等） |
| 5 | DOCTOR_CHIEF | 主任医师 | 主任医师，拥有更高权限 |
| 6 | PATIENT | 患者 | 患者端用户，可挂号、查看报告、缴费 |
| 7 | LAB_DOCTOR | 检验医生 | 负责检验操作（血液、尿液等） |
| 8 | REGISTRATION_DOCTOR | 挂号医生 | 负责挂号登记和分诊 |

### 4.3 状态字段设计

大部分业务表都包含 `status` 字段，用于追踪业务状态流转：
- 挂号：待支付 → 已挂号 → 已就诊 / 已退号
- 处方：待审核 → 待缴费 → 待发药 → 已发药
- 订单：待支付 → 已支付 → 已退款
- 排班状态使用英文枚举：`AVAILABLE` / `FULL` / `CANCELLED`

### 4.4 冗余字段设计

部分表中存在冗余字段（如挂号表中同时有 doctor_id 和 dept_id），目的是：
- 提高查询效率，减少多表 JOIN
- 保留数据快照（如处方明细中冗余药品名称，防止药品信息修改后历史数据不一致）

### 4.5 时间戳字段

大部分表都有 `created_at` 和 `updated_at` 时间戳，用于：
- 数据审计追踪
- 按时间范围查询统计
- 记录业务发生时间

### 4.6 数据库变更记录（change.sql）

`change.sql` 是增量变更脚本，执行顺序为 `schema.sql → change.sql → seed-data.sql`，包含以下变更：

| 变更编号 | 目标表 | 变更内容 | 原因 |
|----------|--------|----------|------|
| 1 | doctor | 移除 `doctor_name` 字段 | 冗余字段，医生姓名由 sys_user.real_name 提供 |
| 2 | doctor_schedule | `status` 默认值从中文改为英文枚举 | 前后端代码使用英文值，数据库默认值需匹配 |
| 3 | doctor_schedule | 新增 `source` 字段 | 区分人工排班和 AI 推荐排班 |
| 4 | ai_schedule_suggestion_detail | 新建表 | AI 排班建议需要独立的明细表 |
| 5 | ai_schedule_suggestion_detail | 新增 `status` 字段 | 支持逐条确认/忽略建议明细 |
| 6 | department | 新增 `parent_id`、`floor`、`phone`、`sort_order` | 支持树形科室结构和展示信息 |
| 7 | sys_role | 拆分 DOCTOR 角色为 CLINIC/INPATIENT/CHIEF | 不同权限的医生进入不同页面 |
| 7b | sys_user | 迁移已有医生用户的角色 | 根据 doctor_type/title 自动分配新角色 |