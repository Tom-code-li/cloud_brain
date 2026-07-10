SET NAMES utf8mb4;
USE doctor_platform;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM doctor_schedule
WHERE doctor_id IN (
  SELECT doctor_id FROM doctor
  WHERE doctor_no IN ('D2026001','D2026002','D2026003','D2026004','D2026005','D2026006','D2026007','D2026008','D2026009','D2026010','D2026011','D2026012','D2026013','D2026014','D2026015','D2026016','D2026017','D2026018','D2026019','D2026020')
);

DELETE FROM doctor
WHERE doctor_no IN (
  'D2026001','D2026002','D2026003','D2026004','D2026005','D2026006','D2026007','D2026008','D2026009',
  'D2026010','D2026011','D2026012','D2026013','D2026014','D2026015','D2026016','D2026017','D2026018','D2026019','D2026020'
);

DELETE FROM sys_user
WHERE username IN (
  'doctor_li','doctor_wang','doctor_chen','doctor_zhao','doctor_sun','doctor_luo',
  'doctor_hu','doctor_yu','doctor_shen','doctor_ma','doctor_liu',
  'doctor_feng','doctor_he','doctor_xu','doctor_jiang','doctor_su','doctor_peng'
);

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_user (role_id, username, password, real_name, phone, email, status)
SELECT role_id, username, '123456', real_name, phone, email, 1
FROM (
  SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR') AS role_id, 'doctor_li' AS username, '李明' AS real_name, '13800001001' AS phone, 'li.ming@example.com' AS email
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_hu', '胡强', '13800001002', 'hu.qiang@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_wang', '王敏', '13800001003', 'wang.min@example.com', 1
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_yu', '于涛', '13800001004', 'yu.tao@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_chen', '陈晨', '13800001005', 'chen.chen@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_shen', '沈洁', '13800001006', 'shen.jie@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_ma', '马强', '13800001007', 'ma.qiang@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_liu', '刘宁', '13800001008', 'liu.ning@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_feng', '冯涛', '13800001009', 'feng.tao@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_he', '何敏', '13800001010', 'he.min@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_xu', '徐倩', '13800001011', 'xu.qian@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_jiang', '蒋强', '13800001012', 'jiang.qiang@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_su', '苏宁', '13800001013', 'su.ning@example.com'
  UNION ALL SELECT (SELECT role_id FROM sys_role WHERE role_code = 'DOCTOR'), 'doctor_peng', '彭静', '13800001014', 'peng.jing@example.com'
) t;

SET @cardio_dept_id = (SELECT dept_id FROM department WHERE dept_name = '心血管内科' LIMIT 1);
SET @resp_dept_id = (SELECT dept_id FROM department WHERE dept_name = '呼吸内科' LIMIT 1);
SET @digest_dept_id = (SELECT dept_id FROM department WHERE dept_name = '消化内科' LIMIT 1);
SET @lab_dept_id = (SELECT dept_id FROM department WHERE dept_name = '检验科' LIMIT 1);
SET @pediatrics_dept_id = (SELECT dept_id FROM department WHERE dept_name = '儿科' LIMIT 1);
SET @skin_dept_id = (SELECT dept_id FROM department WHERE dept_name = '皮肤科' LIMIT 1);
SET @ortho_dept_id = (SELECT dept_id FROM department WHERE dept_name = '骨科' LIMIT 1);

INSERT INTO doctor (user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status)
SELECT user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status
FROM (
  SELECT (SELECT user_id FROM sys_user WHERE username='doctor_li') AS user_id, @cardio_dept_id AS dept_id, 'D2026001' AS doctor_no, '普通门诊' AS doctor_type, '副主任医师' AS title, '高血压、冠心病、心律失常' AS specialty, '擅长心血管常见病门诊' AS introduction, 1 AS status
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_hu'), @cardio_dept_id, 'D2026007', '专家门诊', '主任医师', '复杂病例门诊', '擅长复杂心血管病例', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_wang'), @resp_dept_id, 'D2026002', '普通门诊', '副主任医师', '慢性咳嗽、哮喘、肺部感染', '擅长呼吸系统常见病', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_liu'), @resp_dept_id, 'D2026008', '专家门诊', '主任医师', '慢阻肺、哮喘、呼吸道感染', '擅长呼吸系统复杂病例', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_chen'), @digest_dept_id, 'D2026003', '普通门诊', '主治医师', '胃炎、消化不良、腹痛', '擅长消化内科常见病', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_shen'), @digest_dept_id, 'D2026009', '专家门诊', '主任医师', '胃肠镜、慢性胃炎、肠易激综合征', '擅长消化系统慢病管理', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_zhao'), @pediatrics_dept_id, 'D2026010', '普通门诊', '副主任医师', '儿童呼吸、发热、成长发育', '擅长儿科常见病', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_yu'), @pediatrics_dept_id, 'D2026011', '专家门诊', '主任医师', '儿童慢病、发育咨询', '擅长儿科复杂病例', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_xu'), @skin_dept_id, 'D2026012', '普通门诊', '副主任医师', '湿疹、荨麻疹、痤疮', '擅长皮肤科常见病', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_jiang'), @skin_dept_id, 'D2026013', '专家门诊', '主任医师', '银屑病、皮炎、痤疮', '擅长皮肤科复杂病例', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_su'), @ortho_dept_id, 'D2026014', '普通门诊', '副主任医师', '扭伤、骨关节病、腰腿痛', '擅长骨科常见病', 1
  UNION ALL SELECT (SELECT user_id FROM sys_user WHERE username='doctor_luo'), @ortho_dept_id, 'D2026020', '专家门诊', '主任医师', '运动损伤、关节疼痛、骨折康复', '擅长骨科复杂病例', 1
) t
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dept_id = VALUES(dept_id),
  doctor_type = VALUES(doctor_type),
  title = VALUES(title),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  status = VALUES(status);

SET @d1 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026001' LIMIT 1);
SET @d2 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026007' LIMIT 1);
SET @d3 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026002' LIMIT 1);
SET @d4 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026008' LIMIT 1);
SET @d5 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026003' LIMIT 1);
SET @d6 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026009' LIMIT 1);
SET @d7 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026010' LIMIT 1);
SET @d8 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026011' LIMIT 1);
SET @d9 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026012' LIMIT 1);
SET @d10 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026013' LIMIT 1);
SET @d11 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026014' LIMIT 1);
SET @d12 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026020' LIMIT 1);
SET @lab1 = (SELECT user_id FROM sys_user WHERE username = 'doctor_ma' LIMIT 1);
SET @lab2 = (SELECT user_id FROM sys_user WHERE username = 'doctor_liu' LIMIT 1);

INSERT INTO doctor (user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status)
SELECT user_id, dept_id, doctor_no, doctor_type, title, specialty, introduction, status
FROM (
  SELECT @lab1 AS user_id, @lab_dept_id AS dept_id, 'D2026015' AS doctor_no, '检验门诊' AS doctor_type, '主管技师' AS title, '血常规、生化、免疫' AS specialty, '负责检验咨询与报告解读' AS introduction, 1 AS status
  UNION ALL SELECT @lab2, @lab_dept_id, 'D2026016', '检验门诊', '副主管技师', '采血、标本处理、检验咨询', '负责检验前置服务和结果解释', 1
) t
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dept_id = VALUES(dept_id),
  doctor_type = VALUES(doctor_type),
  title = VALUES(title),
  specialty = VALUES(specialty),
  introduction = VALUES(introduction),
  status = VALUES(status);

SET @d13 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026015' LIMIT 1);
SET @d14 = (SELECT doctor_id FROM doctor WHERE doctor_no = 'D2026016' LIMIT 1);

INSERT INTO doctor_schedule (
  doctor_id, dept_id, work_date, time_period, start_time, end_time,
  total_quota, remain_quota, registration_fee, status
)
VALUES
  (@d1, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 20, 20, 20.00, '可预约'),
  (@d1, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '下午', '14:00:00', '17:00:00', 20, 20, 20.00, '可预约'),
  (@d2, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 20, 20, 30.00, '可预约'),
  (@d2, @cardio_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 20, 20, 30.00, '可预约'),
  (@d3, @resp_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 18, 18, 18.00, '可预约'),
  (@d3, @resp_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 18, 18, 18.00, '可预约'),
  (@d4, @resp_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 18, 18, 28.00, '可预约'),
  (@d4, @resp_dept_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '下午', '14:00:00', '17:00:00', 18, 18, 28.00, '可预约'),
  (@d5, @digest_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 20, 20, 22.00, '可预约'),
  (@d5, @digest_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '下午', '14:00:00', '17:00:00', 20, 20, 22.00, '可预约'),
  (@d6, @digest_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 20, 20, 32.00, '可预约'),
  (@d6, @digest_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 20, 20, 32.00, '可预约'),
  (@d7, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 22, 22, 35.00, '可预约'),
  (@d7, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 22, 22, 35.00, '可预约'),
  (@d8, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 22, 22, 40.00, '可预约'),
  (@d8, @pediatrics_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 22, 22, 40.00, '可预约'),
  (@d9, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 18, 18, 26.00, '可预约'),
  (@d9, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 18, 18, 26.00, '可预约'),
  (@d10, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 18, 18, 32.00, '可预约'),
  (@d10, @skin_dept_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '下午', '14:00:00', '17:00:00', 18, 18, 32.00, '可预约'),
  (@d11, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 20, 20, 24.00, '可预约'),
  (@d11, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '下午', '14:00:00', '17:00:00', 20, 20, 24.00, '可预约'),
  (@d12, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 20, 20, 30.00, '可预约'),
  (@d12, @ortho_dept_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '14:00:00', '17:00:00', 20, 20, 30.00, '可预约'),
  (@d13, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '08:00:00', '11:30:00', 24, 24, 18.00, '可预约'),
  (@d13, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '下午', '14:00:00', '17:00:00', 24, 24, 18.00, '可预约'),
  (@d14, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '08:00:00', '11:30:00', 24, 24, 18.00, '可预约'),
  (@d14, @lab_dept_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '下午', '14:00:00', '17:00:00', 24, 24, 18.00, '可预约')
ON DUPLICATE KEY UPDATE
  dept_id = VALUES(dept_id),
  work_date = VALUES(work_date),
  time_period = VALUES(time_period),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  total_quota = VALUES(total_quota),
  remain_quota = VALUES(remain_quota),
  registration_fee = VALUES(registration_fee),
  status = VALUES(status);
