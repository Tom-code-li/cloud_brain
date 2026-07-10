# change.sql 变更说明文档

> **执行顺序**：`schema.sql` → `change.sql` → `seed-data.sql`
>
> 本文档记录 `change.sql` 对 `schema.sql` 基础结构所做的所有增量变更。

---

## 变更总览

| 编号 | 变更类型 | 目标表 | 变更说明 |
|------|----------|--------|----------|
| 1 | 删除字段 | doctor | 移除冗余字段 `doctor_name` |
| 2 | 修改字段 | doctor_schedule | `status` 默认值从中文改为英文枚举 |
| 3 | 新增字段 | doctor_schedule | 新增 `source`（排班来源） |
| 4 | 新建表 | ai_schedule_suggestion_detail | AI 排班建议明细表 |
| 5 | 新增字段 | ai_schedule_suggestion_detail | 新增 `status`（建议明细状态） |
| 6 | 新增字段 | department | 新增 `parent_id`、`floor`、`phone`、`sort_order` |
| — | 由 seed-data.sql 管理 | sys_role | 角色数据（ADMIN/门诊医生/药房/检查医生/主任/患者/检验/挂号） |

---

## 详细变更说明

### 变更 1：doctor 表 — 删除 `doctor_name` 字段

| 项目 | 说明 |
|------|------|
| 变更类型 | **删除字段** |
| 目标表 | `doctor` |
| 删除字段 | `doctor_name VARCHAR(50)` |
| 删除原因 | doctor 表通过 `user_id` 关联 `sys_user`，医生姓名应由 `sys_user.real_name` 提供。冗余字段会导致数据不一致，违反数据库范式。 |
| 影响范围 | 后端 Doctor 实体已不包含 doctorName 字段，无代码影响。 |

---

### 变更 2：doctor_schedule 表 — 修改 `status` 默认值

| 项目 | 说明 |
|------|------|
| 变更类型 | **修改字段** |
| 目标表 | `doctor_schedule` |
| 修改字段 | `status VARCHAR(20)` |
| 修改内容 | 默认值从 `'可预约'` 改为 `'AVAILABLE'`；现有中文数据迁移为英文枚举 |
| 修改原因 | 后端 Java 代码使用 `"AVAILABLE"` / `"CANCELLED"`，前端同样使用英文枚举值，数据库默认值需匹配。 |
| 数据迁移 | `可预约` → `AVAILABLE`，`已取消`/`停诊` → `CANCELLED` |

**迁移前后对比：**

| 修改前 | 修改后 |
|--------|--------|
| `status VARCHAR(20) NOT NULL DEFAULT '可预约'` | `status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'` |

---

### 变更 3：doctor_schedule 表 — 新增 `source` 字段

| 项目 | 说明 |
|------|------|
| 变更类型 | **新增字段** |
| 目标表 | `doctor_schedule` |
| 新增字段 | `source VARCHAR(30) NOT NULL DEFAULT 'MANUAL'` |
| 字段位置 | `status` 字段之后 |
| 枚举值 | `MANUAL`（人工创建）、`AI_SUGGESTED`（AI 推荐） |
| 新增原因 | AI 排班建议功能需要区分排班是人工创建还是 AI 推荐，用于前端展示不同的标识图标和操作入口。 |

**新增后 doctor_schedule 表结构（部分）：**

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| ... | ... | ... | ... |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'AVAILABLE' | 排班状态 |
| **source** | **VARCHAR(30)** | **NOT NULL, DEFAULT 'MANUAL'** | **排班来源** |
| created_at | DATETIME | NOT NULL | 创建时间 |

---

### 变更 4：新建 ai_schedule_suggestion_detail 表

| 项目 | 说明 |
|------|------|
| 变更类型 | **新建表** |
| 表名 | `ai_schedule_suggestion_detail` |
| 新增原因 | 一条 AI 排班建议可能包含多条建议明细（不同医生/不同时段），需要独立表来承载，供管理员逐条确认或忽略。 |

**完整表结构：**

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| detail_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 明细ID（主键） |
| suggestion_id | BIGINT | NOT NULL, FK→ai_schedule_suggestion | 关联的建议ID |
| doctor_id | BIGINT | NOT NULL, FK→doctor | 医生ID |
| doctor_name | VARCHAR(50) | NOT NULL | 医生姓名（冗余字段） |
| schedule_date | DATE | NOT NULL | 排班日期 |
| time_slot | VARCHAR(20) | NOT NULL | 时段 |
| max_appointments | INT | NOT NULL | 建议最大预约数 |
| reason | TEXT | 可选 | 建议理由 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 状态 |

**外键关系：**

| 约束名 | 字段 | 引用表 | 引用字段 |
|--------|------|--------|----------|
| fk_ai_schedule_detail_suggestion | suggestion_id | ai_schedule_suggestion | suggestion_id |
| fk_ai_schedule_detail_doctor | doctor_id | doctor | doctor_id |

---

### 变更 5：ai_schedule_suggestion_detail 表 — 新增 `status` 字段

| 项目 | 说明 |
|------|------|
| 变更类型 | **新增字段** |
| 目标表 | `ai_schedule_suggestion_detail` |
| 新增字段 | `status VARCHAR(20) NOT NULL DEFAULT 'PENDING'` |
| 字段位置 | `reason` 字段之后 |
| 枚举值 | `PENDING`（待确认）、`ACCEPTED`（已采纳）、`REJECTED`（已忽略） |
| 新增原因 | 每一条 AI 建议明细需要有独立的处理状态，否则管理员无法逐条操作。 |

---

### 变更 6：department 表 — 新增 4 个字段

| 项目 | 说明 |
|------|------|
| 变更类型 | **新增字段（4个）** |
| 目标表 | `department` |
| 新增原因 | 科室管理模块需要支持多级科室、楼层与电话展示、以及显式排序。 |

**新增字段详情：**

| 字段 | 类型 | 约束 | 位置 | 含义 |
|------|------|------|------|------|
| parent_id | BIGINT | 可选, FK→department.dept_id | dept_id 之后 | 上级科室ID（NULL 表示顶级科室，自引用支持树形结构） |
| floor | VARCHAR(50) | 可选 | dept_type 之后 | 楼层（如"3楼"） |
| phone | VARCHAR(30) | 可选 | floor 之后 | 科室电话 |
| sort_order | INT | NOT NULL, DEFAULT 0 | status 之后 | 排序序号（数值越小越靠前） |

**新增外键：**

| 约束名 | 字段 | 引用表 | 引用字段 | 说明 |
|--------|------|--------|----------|------|
| fk_department_parent | parent_id | department | dept_id | 自引用，支持树形科室结构 |

**新增后 department 表结构（部分）：**

| 字段 | 类型 | 约束 | 含义 |
|------|------|------|------|
| dept_id | BIGINT | PRIMARY KEY | 科室ID |
| **parent_id** | **BIGINT** | **可选, FK→dept_id** | **上级科室ID** |
| dept_code | VARCHAR(50) | NOT NULL, UNIQUE | 科室编码 |
| dept_name | VARCHAR(100) | NOT NULL | 科室名称 |
| dept_type | VARCHAR(30) | NOT NULL | 科室类型 |
| **floor** | **VARCHAR(50)** | **可选** | **楼层** |
| **phone** | **VARCHAR(30)** | **可选** | **科室电话** |
| location | VARCHAR(100) | 可选 | 位置描述 |
| description | VARCHAR(255) | 可选 | 科室简介 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态 |
| **sort_order** | **INT** | **NOT NULL, DEFAULT 0** | **排序序号** |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

---

### 角色数据（由 seed-data.sql 管理）

角色数据不属于结构变更，由 `seed-data.sql` 统一管理。完整角色列表见 `schema-relationship.md` 的 4.2 节。

---

## 工具存储过程

`change.sql` 创建了以下临时存储过程用于安全执行变更，执行完毕后自动清理：

| 存储过程 | 功能 | 清理方式 |
|----------|------|----------|
| `add_column_if_not_exists` | 安全添加列（列已存在则跳过） | 执行后 DROP |
| `add_fk_if_not_exists` | 安全添加外键（约束已存在则跳过） | 执行后 DROP |
| `drop_column_if_exists` | 安全删除列（列不存在则跳过） | 执行后 DROP |