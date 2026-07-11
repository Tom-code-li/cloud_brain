# 基于 OceanBase 的国产数据库应用报告

## 1. 国产数据库选型背景与原因

在本项目中，管理员端原本使用 MySQL 作为核心存储数据库。为了体现系统对国产基础软件的支持能力，并验证国产数据库在真实业务系统中的可用性与兼容性，本项目选择将管理员端的核心主数据逐步迁移到国产数据库 OceanBase。

选择 OceanBase 主要基于以下原因：

1. OceanBase 是国产分布式关系型数据库，具备较强的工程实践价值，适合作为课程设计、项目汇报和系统国产化改造的展示对象。
2. OceanBase 支持 MySQL 兼容模式，能够较好复用现有 Spring Boot、MyBatis-Plus、MySQL JDBC 的开发经验，降低系统改造成本。
3. 相比一次性替换整套数据库，本项目采用“管理员域优先迁移”的方式，既可以突出国产数据库落地效果，又能保证现有业务系统平稳运行。
4. OceanBase 在高可用、扩展性、多租户等方面具备较强能力，适合作为后续系统扩容和数据库架构升级的基础。

从项目实施角度看，OceanBase 兼顾了“国产化展示价值”和“实际改造可行性”，因此被确定为本项目管理员端数据库改造的目标数据库。

参考资料：
- [OceanBase 与 MySQL 兼容性说明](https://en.oceanbase.com/docs/common-oceanbase-database-10000000000869904)
- [OceanBase Connector/J 文档](https://en.oceanbase.com/docs/oceanbase-connector-j-en)

## 2. OceanBase 核心能力概述

OceanBase 是一种国产分布式关系型数据库，支持事务处理、SQL 查询、数据高可用以及横向扩展，能够满足企业级系统对稳定性和性能的要求。

本项目重点使用并展示了 OceanBase 的以下能力：

### 2.1 MySQL 兼容能力

OceanBase 支持 MySQL 兼容模式，这意味着开发者可以沿用 MySQL 的大部分表结构设计方式、SQL 语法习惯以及 JDBC 接入模式。在本项目中，管理员端后端基本保留了原有的 Spring Boot + MyBatis-Plus 技术栈，仅通过双数据源方式完成数据库接入改造，而无需重写业务逻辑。

### 2.2 高可用能力

OceanBase 官方文档指出，其基于多副本与 Paxos 协议实现高可用能力，在少数节点故障时可保证业务持续可用。在理论上，它可以提供较高的数据安全性与服务连续性，适合作为企业级数据库平台。

### 2.3 分布式与可扩展能力

与传统单机数据库相比，OceanBase 能够支持分布式部署，并具备较好的扩展能力。虽然本次项目主要采用本地单机开发环境进行验证，但从架构设计角度看，OceanBase 更适合作为后续大规模业务系统的数据底座。

### 2.4 配套工具生态

OceanBase 还提供了较完整的配套工具体系，例如：

- `ODC`：可视化数据库开发工具
- `OMS`：迁移与同步工具

这些工具能够帮助开发者进行数据迁移、结构管理、SQL 开发与运维操作，对项目后续扩展有明显帮助。

参考资料：
- [OceanBase 高可用概述](https://en.oceanbase.com/docs/common-oceanbase-database-10000000001228330)
- [ODC 文档](https://en.oceanbase.com/docs/odc-en)
- [OMS Community Edition 文档](https://en.oceanbase.com/docs/community-oms-en-10000000001370755)

## 3. OceanBase 在本项目中的应用方式

本项目并没有一次性将所有业务数据全部切换到 OceanBase，而是采用了“管理员域优先迁移”的渐进式方案。这样既能展示国产数据库接入效果，又能避免一次性大规模切换带来的风险。

### 3.1 当前数据库架构

当前系统采用双数据源架构：

- `admin` 数据源：连接 OceanBase
- `biz` 数据源：连接原 MySQL

其中，OceanBase 主要负责管理员端的主数据和配置类数据；而交易性更强、耦合更深的业务数据暂时仍保留在 MySQL 中。

### 3.2 已迁移到 OceanBase 的管理员主数据表

本次迁移到 OceanBase 的核心表包括：

- `sys_role`
- `sys_user`
- `department`
- `doctor`
- `doctor_schedule`
- `ai_schedule_suggestion`
- `ai_schedule_suggestion_detail`

这些表共同构成了管理员端的核心主数据域，涵盖了用户、角色、科室、医生、排班以及 AI 排班建议等信息。

### 3.3 暂未迁移的业务表

为了保证现有业务平稳运行，以下交易性表仍保留在 MySQL：

- `registration`
- 费用结算相关表
- 病历相关表
- 处方、发药相关表

因此，当前管理员端已经实现“主数据走 OceanBase、业务交易暂留 MySQL”的过渡型架构。

### 3.4 后端改造方式

在后端实现上，本项目完成了以下关键改造：

1. 新增 `app.datasource.admin` 和 `app.datasource.biz` 两组配置。
2. 将 MyBatis Mapper 拆分为 `mapper.admin` 与 `mapper.biz` 两类包。
3. 管理员主数据相关 Service 改为读取 OceanBase 数据源。
4. 仍依赖挂号等交易数据的查询继续读取 MySQL 数据源。

这种改造方式保证了系统能在不推翻原有架构的前提下逐步接入国产数据库。

## 4. 技术实现过程

本项目中 OceanBase 的接入与验证，主要经过了以下几个步骤：

### 4.1 本地 OceanBase 开发环境搭建

为了完成本地验证，项目采用 Docker + WSL2 的方式启动 OceanBase。由于 OceanBase 对内存要求较高，因此在 Windows 本机中额外配置了：

- WSL2 内存上限调整为 `10GB`
- Docker Desktop 重启后重新加载资源配置

在完成资源调整后，本地 OceanBase 容器可以正常启动，并成功创建管理员端使用的 `his_admin` 数据库。

### 4.2 管理员数据库结构初始化

项目新增了 OceanBase 管理域建表脚本，用于在 OceanBase 中创建管理员端需要的核心表结构。同时新增了同步脚本，用于将原 MySQL 中的管理员主数据导入到 OceanBase 中。

对应内容包括：

- 管理员域建表脚本
- 管理员域同步脚本
- 本地 OceanBase 启动与检查脚本

### 4.3 双数据源改造

在应用层实现中，系统增加了两组数据源配置，并新增独立的 Spring 配置类来管理：

- OceanBase 管理员数据源
- MySQL 业务数据源

为了保证查询逻辑正确，项目进一步将 Mapper 包按职责拆分，并通过不同的 `SqlSessionTemplate` 实现数据路由。这样管理员相关接口将优先访问 OceanBase，而挂号等业务统计仍访问 MySQL。

### 4.4 测试与兼容性处理

在测试阶段，还对本地测试环境进行了适配：

1. 为双数据源补充了 H2 测试配置。
2. 为现有单元测试增加了新的数据源上下文验证。
3. 修复了本地 JDK 与 Mockito inline mock maker 的兼容问题，使后端测试能够稳定运行。

通过这些处理，项目不仅完成了数据库接入，还保证了开发、测试和本地联调环境的可用性。

## 5. 验证结果

为了验证 OceanBase 接入是否成功，本项目从数据库连接、表结构、数据迁移、接口功能和测试结果五个方面进行了验证。

### 5.1 数据库连接验证

目前已经能够通过 DataGrip 成功连接到本地 OceanBase 数据库，并看到管理员端使用的 `his_admin` 库。这说明 OceanBase 实例本身已经可用，且可通过常见图形化工具进行访问和管理。

### 5.2 管理员主数据表验证

在 OceanBase 的 `his_admin` 库中，已经成功创建并验证如下核心表：

- `sys_role`
- `sys_user`
- `department`
- `doctor`
- `doctor_schedule`
- `ai_schedule_suggestion`
- `ai_schedule_suggestion_detail`

### 5.3 数据迁移结果验证

当前同步到 OceanBase 的管理员主数据统计结果如下：

| 表名 | 迁移后记录数 |
|------|-------------:|
| `sys_role` | 12 |
| `sys_user` | 42 |
| `department` | 25 |
| `doctor` | 25 |
| `doctor_schedule` | 33 |
| `ai_schedule_suggestion` | 17 |
| `ai_schedule_suggestion_detail` | 27 |

其中，`doctor_schedule` 的数量少于源 MySQL 表，是因为源数据中存在重复排班记录，而目标表增加了唯一约束，因此在同步脚本中采用了 `INSERT IGNORE` 的方式跳过冲突数据。这也反映出国产数据库迁移过程中对历史脏数据进行清洗的重要性。

### 5.4 接口功能验证

在系统启动后，管理员端关键接口已经完成真实联调验证：

- 登录接口可以成功调用
- 科室列表接口可以成功返回数据
- 医生列表接口可以成功返回数据
- 排班列表接口可以成功返回数据

实际验证中，使用管理员账号 `admin_chen / Admin@123` 可以成功登录，并获取管理员端所需的基础数据，说明管理员主数据链路已经切换到 OceanBase 并能够正常工作。

### 5.5 后端测试验证

在后端测试层面，本项目已经完成：

- 双数据源上下文测试
- Mapper 路由测试
- 服务层单元测试
- 管理员端应用上下文测试

在跳过 JaCoCo 覆盖率门槛的前提下，后端测试共 `162` 项，全部通过。这说明本次 OceanBase 接入没有破坏现有管理员端后端功能，系统具备继续扩展和优化的基础。

---

## 总结

本项目通过引入 OceanBase，实现了管理员端数据库从传统单库模式向“国产数据库 + 双数据源”架构的初步演进。整个过程不仅验证了 OceanBase 在 MySQL 兼容模式下的可接入性，也说明国产数据库能够在现有 Java Web 项目中以较低成本落地。

从结果来看，OceanBase 已经成功承担管理员端主数据存储任务，并完成了数据库连接、结构初始化、数据迁移、接口联调和测试验证，为后续进一步推进业务域迁移提供了良好的基础。
