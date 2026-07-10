# 📋 后端代码审查报告

---

## 1. MyBatis-Plus 使用情况

**✓ 总体情况**：所有 Repository 都继承了 `BaseMapper<T>`，核心 CRUD 操作（selectById、selectList、selectCount、insert、updateById、deleteById）都使用 MyBatis-Plus 提供的方法，符合约定。

**⚠ 额外自定义 SQL**（部分 Repository 添加了 `@Select`，属于 MyBatis 原生语法扩展，不违反 MyBatis-Plus 使用原则，但需关注类型安全）：

| Repository | 自定义方法 | 问题 |
|---|---|---|
| `RegistrationRepository` | `findPatientNamesByRegistrationIds` | 返回 `List<Map<String, Object>>`，类型不安全 |
| `RegistrationRepository` | `countByTimeRange` | 简单统计，合理 |
| `ScheduleRepository` | `countDistinctDoctorsByWorkDateAndStatuses` | 简单统计，合理 |
| `AiConsultationRepository` | `countByTimeRangeWithResult` | 简单统计，合理 |
| `AiScheduleSuggestionRepository` | `countByTimeRange` | 简单统计，合理 |

**建议**：`findPatientNamesByRegistrationIds` 应改为返回实体 DTO 或使用更明确的类型。

---

## 2. MVC 分层架构审查

| 层 | 状态 | 问题 |
|---|---|---|
| **Controller** | ✅ 基本清晰 | 只处理 HTTP 参数转发和 `Result` 包装，无业务逻辑侵入。`DoctorAdminController` 承担了较多查询入口（分页/下拉/导出），可考虑进一步拆分。 |
| **Service** | ⚠ 部分职责过界 | CQRS 分离模式（Command/Query 分 Service）在 Schedule/Department/Doctor 上实现得很好，但其他模块（AI/Auth/Role/User/Dashboard）未分离。 |
| **Repository** | ✅ 清晰 | 仅做数据访问，无业务逻辑。 |
| **Entity/DTO** | ⚠ 边界不清晰 | Service 层多处直接接收和返回 Entity（而非 DTO），尤其是 `ISysUserService`、`ISysRoleService`。 |

**具体越界案例**：

```java
// ❌ ScheduleQueryServiceImpl 直接依赖了三个其他模块的 Repository
//    service/schedule/impl/ScheduleQueryServiceImpl.java:31-34
private final ScheduleRepository scheduleRepository;     // 自身模块 OK
private final DoctorRepository doctorRepository;         // ← 越界，应通过 IDoctorQueryService
private final DepartmentRepository departmentRepository; // ← 越界，应通过 IDepartmentQueryService
private final RegistrationRepository registrationRepository; // ← 越界

// ❌ DepartmentCommandServiceImpl 直接操作 DoctorRepository 和 ScheduleRepository
//    service/department/impl/DepartmentCommandServiceImpl.java:26-27
private final DoctorRepository doctorRepository;      // ← 越界，删除科室前校验关联
private final ScheduleRepository scheduleRepository;  // ← 越界
```

**相同问题也存在于**：
- `DoctorCommandServiceImpl` → 依赖 `ScheduleRepository`、`RegistrationRepository`
- `AIScheduleServiceImpl` → 依赖 `DoctorRepository`、`ScheduleRepository`
- `DashboardStatisticsServiceImpl` → 直接依赖 6 个 Repository，是最严重的越界案例

**推荐修复方向**：Service 层只能依赖其他模块的 **Service 接口**，不能直接依赖其他模块的 Repository。例如 ScheduleQueryService 查询医生姓名时，应调用 `IDoctorQueryService.findDoctorsByIds()` 而不是直接 `DoctorRepository.selectList()`。

---

## 3. SOLID 原则审查

| 原则 | 状态 | 说明 |
|---|---|---|
| **SRP 单一职责** | ⚠ 部分违反 | `ScheduleQueryServiceImpl.toResponse`、`DoctorQueryServiceImpl.listDoctors` 同时做查询+关联数据组装+DTO 映射。`ScheduleMapper` 已作为独立组件是个好迹象，但未充分覆盖。`AIScheduleServiceImpl` 同时负责 AI 请求、解析、数据库操作、状态流转，应至少拆成 AI 客户端 + 建议服务两部分。 |
| **OCP 开闭原则** | ✅ 良好 | 扩展新功能通过添加新 Service/方法实现，不修改已有核心逻辑。 |
| **LSP 里氏替换** | ✅ 良好 | 接口与实现关系正确。 |
| **ISP 接口隔离** | ✅ 部分良好 | Schedule/Department/Doctor 的 Command/Query 分离是优秀实践。但 `IAIScheduleService` 一个接口涵盖 5 个操作（生成建议/采纳/拒绝/明细采纳/明细拒绝），可进一步拆分。 |
| **DIP 依赖倒置** | ✅ 良好 | 所有 Controller/Service 都通过接口注入。 |

---

## 4. 其他需要关注的问题

### 4.1 "Mapper" 命名混淆
`service/impl/ScheduleMapper.java` 和 `service/department/impl/DepartmentMapper.java` 实际上是 Entity→DTO 映射器（类似 MapStruct），而非 MyBatis-Plus 的 Mapper。放在 impl 目录下且命名为 Mapper，容易引起误解。建议重命名为 `ScheduleResponseMapper` 或 `ScheduleDtoConverter`，并放在专门的 `converter/` 或 `mapper/` 目录。

### 4.2 实体直接暴露给上层 Service
`ISysUserService.findUsersByIds()` 返回 `Map<Long, SysUser>`，`ISysRoleService.findRoleById()` 返回 `SysRole`。这意味着 Entity 被暴露在 Service 层之间流通，违反数据分层原则。应改为返回 DTO。

### 4.3 事务管理不一致

| Service | 类级 `@Transactional` | 方法级 | 问题 |
|---|---|---|---|
| `ScheduleCommandServiceImpl` | ❌ 无 | 每个写操作方法有 | 合理 |
| `ScheduleQueryServiceImpl` | ❌ 无 | ❌ 无 | 读操作建议加 `@Transactional(readOnly=true)` |
| `DepartmentCommandServiceImpl` | ✅ 类级 | - | 合理 |
| `DepartmentQueryServiceImpl` | ✅ 类级 `readOnly=true` | - | 良好 |
| `DoctorCommandServiceImpl` | ❌ 无 | 每个写操作方法有 | 合理 |
| `DoctorQueryServiceImpl` | ✅ 类级 `readOnly=true` | - | 良好 |
| `SysRoleServiceImpl` | ❌ 无 | 部分方法有 | 不一致 |
| `SysUserServiceImpl` | ❌ 无 | 部分方法有 | 不一致 |
| `AIScheduleServiceImpl` | ❌ 无 | 每个方法有 | 合理 |
| `AuthServiceImpl` | ❌ 无 | ❌ 无 | 登录是读操作，但建议加事务 |
| `DashboardStatisticsServiceImpl` | ❌ 无 | ❌ 无 | 纯查询，建议加 `readOnly` |

### 4.4 状态常量散落多处，缺乏统一枚举
- `ScheduleCommandServiceImpl`: `STATUS_ACTIVE = "可预约"`, `SOURCE_MANUAL = "MANUAL"`
- `AIScheduleServiceImpl`: `STATUS_PENDING/ACCEPTED/REJECTED`, `SUGGESTION_PENDING/ACCEPTED/REJECTED`, `STATUS_AVAILABLE = "可预约"`, `SOURCE_AI = "AI_SUGGESTED"`
- `Doctor`: `STATUS_ENABLED = 1`, `STATUS_DISABLED = 0`
- `Department`: `STATUS_ENABLED = 1`, `STATUS_DISABLED = 0`
- `DashboardStatisticsServiceImpl`: `AVAILABLE_SCHEDULE_STATUSES = List.of("1", "可预约")`

**问题**：排班状态既有 `"可预约"`（中文）也有 `"1"`（数字字符串），`DashboardStatisticsServiceImpl` 用 `List.of("1", "可预约")` 来兼容，说明历史数据格式混乱。应统一迁移为枚举或单个常量。

### 4.5 `findPatientNamesByRegistrationIds` 弱类型问题
已修复字段名错误（`p.name` → `p.patient_name`），但返回 `List<Map<String, Object>>` 仍需改进：
```java
// ❌ 弱类型，编译期无法检查 key 是否正确
List<Map<String, Object>> findPatientNamesByRegistrationIds(@Param("ids") Set<Long> ids);

// ✅ 建议改为定义 Projection DTO，或者直接使用 MyBatis-Plus 的 VO 映射
```

### 4.6 `AIScheduleServiceImpl` 内的重复逻辑
`validateConflict()`、`createScheduleFromDetail()` 与 `ScheduleCommandServiceImpl` 中的 `validateConflict()`、`saveNewSchedule()` 逻辑几乎相同，存在代码重复。应抽取为共享组件或让 AI Service 调用 ScheduleCommandService。

### 4.7 AuthServiceImpl 密码明文比对
`passwordMatches()` 使用 `rawPassword.equals(storedPassword)`，未使用 BCrypt 等加密方案。生产环境必须改为加密存储+加密比对。

---

## 5. 架构总览图

```
┌────────────────────────────────────────────┐
│              Controller 层                  │
│  只处理 HTTP，不包含业务逻辑（良好）        │
└────────────────┬───────────────────────────┘
                 │ 依赖接口
┌────────────────▼───────────────────────────┐
│               Service 层                     │
│  ├─ IScheduleCommandService / IScheduleQueryService  ✅ CQRS
│  ├─ IDoctorCommandService / IDoctorQueryService      ✅ CQRS
│  ├─ IDepartmentCommandService / IDepartmentQueryService ✅ CQRS
│  ├─ IAIScheduleService (建议拆分)
│  ├─ IAISchedulingClient
│  ├─ AuthService
│  ├─ ISysRoleService  ⚠ 返回 Entity
│  └─ ISysUserService  ⚠ 返回 Entity
│  ⚠ Service 间应依赖 Service 接口，而非 Repository
└────────────────┬───────────────────────────┘
                 │ 依赖
┌────────────────▼───────────────────────────┐
│            Repository 层                    │
│  extends BaseMapper<T>，纯数据访问（良好）   │
└────────────────┬───────────────────────────┘
                 │ 依赖
┌────────────────▼───────────────────────────┐
│               Entity 层                     │
└────────────────────────────────────────────┘
```

---

## 6. 总结：优先级行动清单

| 优先级 | 任务 | 影响 |
|---|---|---|
| 🔴 高 | Service 层停止直接依赖其他模块 Repository，改为依赖对应 Service 接口 | 解耦，防止跨模块依赖 |
| 🔴 高 | `ISysUserService`、`ISysRoleService` 返回 DTO，不暴露 Entity | 维护数据分层边界 |
| 🟡 中 | 统一状态字段为枚举，清理 `AVAILABLE_SCHEDULE_STATUSES = List.of("1", "可预约")` 这类兼容代码 | 降低数据混乱度 |
| 🟡 中 | 将 `ScheduleMapper`、`DepartmentMapper` 重命名并移到 `converter/` 目录 | 消除命名歧义 |
| 🟡 中 | `AIScheduleServiceImpl` 与 `ScheduleCommandServiceImpl` 之间抽取共享的排班创建/冲突校验逻辑 | 消除代码重复 |
| 🟡 中 | 为 Query Service 统一添加 `@Transactional(readOnly = true)` | 提高性能与一致性 |
| 🟢 低 | `findPatientNamesByRegistrationIds` 改为返回类型安全的 DTO | 提升可维护性 |
| 🔴 高（安全） | `AuthServiceImpl.passwordMatches` 改为 BCrypt 加密比对 | 安全必要 |

**整体评价**：架构设计理念正确（CQRS 分离、接口依赖），但执行过程中出现了 **Service 层直接依赖其他模块 Repository** 这一关键疏漏，以及**状态字段缺乏统一管理**的问题。修复上述高优先级任务后，代码质量将有明显提升。