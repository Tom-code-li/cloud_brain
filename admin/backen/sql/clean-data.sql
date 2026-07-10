-- ============================================================
-- clean-data.sql
-- 清空数据库中所有表的数据（保留表结构）
-- 执行顺序：从依赖最深的子表开始，逐层到父表，避免外键约束错误
-- 幂等性：所有 TRUNCATE 都可重复执行
-- ============================================================

USE doctor_platform;

-- 临时关闭外键检查，避免手动排列顺序的繁琐
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第一阶段：费用与支付相关（依赖最顶层，最末端业务）
-- ============================================================
TRUNCATE TABLE fee_order_item;           -- 费用订单明细
TRUNCATE TABLE payment_record;            -- 支付记录
TRUNCATE TABLE refund_record;             -- 退款记录
TRUNCATE TABLE fee_order;                 -- 费用订单

-- ============================================================
-- 第二阶段：药房药品相关
-- ============================================================
TRUNCATE TABLE drug_stock_record;         -- 药品库存变动记录
TRUNCATE TABLE pharmacy_return;           -- 退药记录
TRUNCATE TABLE pharmacy_dispense;         -- 发药记录
TRUNCATE TABLE prescription_item;         -- 处方明细
TRUNCATE TABLE prescription;              -- 处方

-- ============================================================
-- 第三阶段：检验检查报告相关
-- ============================================================
TRUNCATE TABLE exam_lab_report;           -- 检查/检验报告
TRUNCATE TABLE exam_lab_order_item;       -- 检查/检验申请明细
TRUNCATE TABLE exam_lab_order;            -- 检查/检验申请单

-- ============================================================
-- 第四阶段：门诊病历相关
-- ============================================================
TRUNCATE TABLE medical_record;            -- 病历
TRUNCATE TABLE outpatient_visit;          -- 门诊就诊记录

-- ============================================================
-- 第五阶段：挂号分诊与 AI 问诊
-- ============================================================
TRUNCATE TABLE triage_record;             -- 分诊记录
TRUNCATE TABLE registration;              -- 挂号记录
TRUNCATE TABLE ai_consultation;           -- AI 问诊

-- ============================================================
-- 第六阶段：排班与 AI 排班建议
-- ============================================================
TRUNCATE TABLE ai_schedule_suggestion_detail;  -- AI 排班建议详情
TRUNCATE TABLE ai_schedule_suggestion;         -- AI 排班建议
TRUNCATE TABLE doctor_schedule;                -- 医生排班

-- ============================================================
-- 第七阶段：医生、患者、用户等基础数据
-- ============================================================
TRUNCATE TABLE doctor;                    -- 医生
TRUNCATE TABLE patient;                   -- 患者
TRUNCATE TABLE medical_item;              -- 检查/检验项目
TRUNCATE TABLE drug;                      -- 药品

-- ============================================================
-- 第八阶段：AI 调用日志（无外键强依赖，但放在最后便于定位）
-- ============================================================
TRUNCATE TABLE ai_call_log;               -- AI 调用日志

-- ============================================================
-- 第九阶段：基础数据（用户、科室、角色）
-- ============================================================
TRUNCATE TABLE sys_user;                  -- 用户（含自增重置）
TRUNCATE TABLE department;                -- 科室
TRUNCATE TABLE sys_role;                  -- 角色

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 重置所有表的 AUTO_INCREMENT（MySQL 8.0+ 支持）
ALTER TABLE sys_role                       AUTO_INCREMENT = 1;
ALTER TABLE department                     AUTO_INCREMENT = 1;
ALTER TABLE sys_user                       AUTO_INCREMENT = 1;
ALTER TABLE patient                        AUTO_INCREMENT = 1;
ALTER TABLE doctor                         AUTO_INCREMENT = 1;
ALTER TABLE medical_item                   AUTO_INCREMENT = 1;
ALTER TABLE drug                           AUTO_INCREMENT = 1;
ALTER TABLE doctor_schedule                AUTO_INCREMENT = 1;
ALTER TABLE ai_schedule_suggestion         AUTO_INCREMENT = 1;
ALTER TABLE ai_schedule_suggestion_detail  AUTO_INCREMENT = 1;
ALTER TABLE ai_consultation                AUTO_INCREMENT = 1;
ALTER TABLE registration                   AUTO_INCREMENT = 1;
ALTER TABLE triage_record                  AUTO_INCREMENT = 1;
ALTER TABLE fee_order                      AUTO_INCREMENT = 1;
ALTER TABLE fee_order_item                 AUTO_INCREMENT = 1;
ALTER TABLE payment_record                 AUTO_INCREMENT = 1;
ALTER TABLE refund_record                  AUTO_INCREMENT = 1;
ALTER TABLE outpatient_visit               AUTO_INCREMENT = 1;
ALTER TABLE medical_record                 AUTO_INCREMENT = 1;
ALTER TABLE exam_lab_order                 AUTO_INCREMENT = 1;
ALTER TABLE exam_lab_order_item            AUTO_INCREMENT = 1;
ALTER TABLE exam_lab_report                AUTO_INCREMENT = 1;
ALTER TABLE prescription                   AUTO_INCREMENT = 1;
ALTER TABLE prescription_item              AUTO_INCREMENT = 1;
ALTER TABLE pharmacy_dispense              AUTO_INCREMENT = 1;
ALTER TABLE pharmacy_return                AUTO_INCREMENT = 1;
ALTER TABLE drug_stock_record              AUTO_INCREMENT = 1;
ALTER TABLE ai_call_log                    AUTO_INCREMENT = 1;

-- ============================================================
-- 结束
-- 执行后：所有表数据被清空，AUTO_INCREMENT 重置为 1
-- 如需重新填充测试数据，可继续执行 seed-data.sql
-- ============================================================