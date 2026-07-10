# 挂号医生新后端联调设计

**日期**: 2026-07-02

## 背景

当前项目已经拆分为三套后端：

- `backend`：主后端，负责统一登录、门诊医生、药房，以及门诊侧检查/检验申请与结果回阅
- `backend-medical-exam`：检查医生、检验医生工作台
- `backend-registration`：新拉下来的挂号医生后端，为独立多模块工程

当前前端 `frontend` 已具备多工作台路由和统一登录能力，但挂号页面仍然默认请求主后端 `/api`。本次目标是在不破坏现有门诊、检查检验、药房联调结果的前提下，让挂号医生页面切到新挂号后端。

## 目标

让前端挂号模块与 `backend-registration` 成功联调，同时保留现有结构：

1. 登录仍走主后端 `backend`
2. 登录后按身份分流到对应工作台
3. 挂号页面业务接口改走 `backend-registration`
4. 挂号 AI 建议改走 `backend-registration` 的 AI 模块
5. 门诊、检查/检验、药房已有接口和页面行为不回退

## 非目标

- 不把 `backend-registration` 的登录体系并入当前统一登录
- 不重构当前多后端拆分结构
- 不改现有门诊医生、检查医生、检验医生页面整体风格
- 不在本次里统一三套后端的鉴权架构

## 可选方案

### 方案 A：前端直连挂号模块

- 登录仍走 `backend:8080`
- 挂号页面业务接口单独走 `backend-registration`：
  - 挂号业务 -> `module-registration:9200`
  - 挂号 AI -> `module-ai-assistant:9600`
- 前端通过新增代理区分挂号后端和主后端

优点：

- 改动最小
- 不影响当前已打通的门诊与检查检验链路
- 失败面局限在挂号模块

缺点：

- 前端会同时依赖多套后端入口

### 方案 B：全部挂号请求走队友网关

- 挂号模块走 `doctor-gateway:9000`
- 需要解决当前统一登录 token 与队友 JWT 链路兼容问题

优点：

- 更贴近队友原始设计

缺点：

- 鉴权耦合高
- 最容易影响当前统一登录

### 方案 C：把挂号后端再并回主后端

- 将 `backend-registration` 中挂号模块逐步并入 `backend`

优点：

- 长期结构更集中

缺点：

- 改动最大
- 短期最容易破坏现有已联调能力

## 推荐方案

采用 **方案 A：前端直连挂号模块**。

## 设计

### 1. 后端职责保持不变

- `backend` 继续提供：
  - `/auth/**`
  - `/outpatient/**`
  - `/exam-request`
  - `/lab-request`
  - `/prescriptions`
  - `/fee-orders`
  - `/ai/outpatient/**`
- `backend-registration` 提供：
  - `/registration/**`
  - `/ai/registration/**`

### 2. 前端代理新增挂号后端入口

在 `frontend/vite.config.js` 中保留当前：

- `/api -> 8080`
- `/medical-exam -> 9400`

新增：

- `/registration-api -> 9200`
- `/registration-ai -> 9600`

### 3. 登录流不变

- 登录页仍调用主后端 `/api/auth/login`
- `authStore` 继续以主后端登录返回为唯一身份来源
- 前端只把挂号业务页面的接口目标切换到新后端，不切换登录来源

### 4. 挂号接口层拆分

`frontend/src/api/registration.js` 需要从通用 `http` 中拆出：

- 业务接口走 `registration-api` 客户端
- AI 建议接口走 `registration-ai` 客户端

同时保留当前页面调用函数名不变，避免挂号页面大面积改动。

### 5. 鉴权兼容

优先假设挂号业务接口本身不强依赖当前主后端 token。

如果 `backend-registration` 的 AI 或挂号模块必须依赖 `X-Doctor-Id` / `Authorization`：

- 复用当前前端已保存的登录态
- 由挂号专用客户端附加请求头

不改当前统一登录流程。

### 6. 风险控制

- 不动门诊 `outpatient.js`
- 不动检查/检验 `medicalExam*.js`
- 不动药房 `pharmacy.js`
- 只改挂号 API 客户端、挂号 AI 调用和 Vite 代理

## 需要修改的文件

- `frontend/vite.config.js`
- `frontend/src/api/registration.js`
- 如需要：`frontend/src/views/registration/OfflineRegistration.vue`
- 如需要：`frontend/src/views/registration/OnlineRegistrationConfirm.vue`
- 如需要：`frontend/src/views/registration/RegistrationDashboard.vue`

## 验证

### 挂号模块验证

1. 统一登录成功
2. 挂号医生进入挂号工作台
3. 科室列表、医生列表、排班列表正常返回
4. 线下挂号提交成功
5. 线上待确认列表正常返回
6. 收费、退费、候诊队列页面正常返回
7. AI 分诊建议接口正常返回

### 回归验证

1. 门诊医生登录和工作台正常
2. 检查/检验工作台正常
3. 药房页面正常
4. 现有前端测试继续通过

## 成功标准

满足以下条件即可认为本次联调完成：

1. 挂号页面不再依赖主后端 `8080` 的挂号接口
2. 新挂号后端 `backend-registration` 成功为前端挂号模块提供数据
3. 挂号 AI 能通过新后端返回建议
4. 门诊、检查检验、药房既有联调能力不回退
