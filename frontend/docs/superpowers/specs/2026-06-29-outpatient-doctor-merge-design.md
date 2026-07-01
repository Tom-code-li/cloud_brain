# 门诊医生业务合入设计

**日期**: 2026-06-29

## 背景

当前项目 `C:/Users/李博/OneDrive/桌面/frontend/front/src` 已完成多模块融合，已经存在统一的：

- 全局样式与页面骨架
- 路由守卫与角色工作台分流
- Axios 封装与本地 mock 切换
- 挂号、检查、检验、药房等额外模块

用户提供的 `C:/Users/李博/OneDrive/桌面/门诊医生前端/frontend/src` 包含门诊医生模块的后续业务增强。本次目标不是回退当前融合项目，而是在保留现有基础设施的前提下，将提供版本中“门诊医生相关改动及其直接依赖”完整合入当前项目。

## 目标

将用户提供版本中的门诊医生业务能力完整合入当前融合项目，使门诊医生链路从“患者查看与基础录入”扩展为一条完整的就诊流程：

1. 患者查看中仅展示当前可接诊患者，并支持接诊动作
2. 病历首页支持增强后的 AI 初诊建议与 AI 结果存储
3. 检查申请与检验申请支持：
   - 申请草稿暂存
   - “本次无需检查/检验”业务动作
   - 已提交申请记录与详情查看
4. 报告回阅支持：
   - 仅展示待回阅患者
   - 查看结构化检查/检验结果
   - 生成 AI 报告后建议
   - 将报告标记为已回阅并推进到确诊阶段
5. 门诊确诊支持保存最终诊断与处理意见
6. 开设处方支持更完整的用药字段
7. 费用查询支持读取当前就诊关联费用
8. 全部链路与当前项目已有的角色路由、全局样式、Axios/mock 体系兼容

## 非目标

本次不处理以下内容：

- 重构当前融合项目的登录、角色菜单、路由守卫总体架构
- 回退或替换挂号、药房、检查医生、检验医生模块的现有页面与接口
- 将用户提供版本中非门诊医生目的的基础设施覆盖到当前项目
- 大范围重写现有样式体系或整体视觉语言

## 范围界定

### 需要合入的门诊相关文件

#### 业务接口与状态

- `src/api/outpatient.js`
- `src/stores/patientStore.js`
- `src/mock/adapter.js`
- `src/utils/aiAssistant.js`

#### 新增组件

- `src/components/EncounterTabs.vue`
- `src/components/RequestOrderDetailDialog.vue`

#### 门诊相关页面

- `src/views/PatientView.vue`
- `src/views/MedicalHomeView.vue`
- `src/views/ExamRequestView.vue`
- `src/views/LabRequestView.vue`
- `src/views/AiReportView.vue`
- `src/views/AiReportDetailView.vue`
- `src/views/DiagnosisView.vue`
- `src/views/DiagnosisDetailView.vue`
- `src/views/PrescriptionDetailView.vue`
- `src/views/FeeQueryView.vue`

#### 兼容性触点

- `src/router/routes.js`
- `src/styles/global.css`

### 明确保留不替换的基础设施

- `src/router/index.js`
- `src/api/http.js`
- 当前已融合的角色菜单解析逻辑
- 当前已存在的挂号、药房、检查医生、检验医生模块文件

## 设计原则

### 1. 基础设施保留，门诊能力增量增强

当前项目的统一路由守卫、工作台分流、Axios 封装和 mock 开关已经对多角色项目生效。用户提供版本中的旧式门诊项目基础设施更偏单模块，不应整体覆盖。

因此实现上采用“保留基础设施 + 吸收门诊业务能力”的方式：

- 保留当前 `router/index.js` 的登录态恢复与按角色分流
- 保留当前 `api/http.js` 的 `http` / `dataHttp` 与本地 mock 适配方式
- 保留当前多角色路由集合
- 仅将门诊模块自身的业务契约、状态与页面行为并入

### 2. 页面行为以用户提供版本为准

对门诊医生页面本身，优先采用用户提供版本的业务行为，而不是当前融合项目里的旧门诊实现。原因是本次请求的核心就是将这些新增业务逻辑合入整体项目。

### 3. 直接依赖一并合入

凡是门诊页面新增行为直接依赖的数据结构、工具函数、mock 接口、组件，也必须一起合入，避免出现“页面改了但上下文能力缺失”的半成品状态。

### 4. 对其他模块零回退

检查医生、检验医生、挂号、药房现有代码不做行为回退。若门诊增强要求改动共享文件，应采用追加兼容的方式，不得破坏这些模块当前使用的字段和路由。

## 核心业务链路设计

### 1. 患者查看与接诊

门诊患者列表从泛患者查询升级为“待接诊患者入口”。

保留页面入口路径 `/patients`，但行为调整为：

- 查询参数新增 `visitStatus: '待接诊'`
- 每条患者记录需携带 `patientId`、`visitId`、`visitStatus`、`registeredAt`
- 点击“接诊”时调用 `startEncounter`
- 成功后写入 `patientStore.selectPatient(row)`，跳转到 `/medical-home`

这要求 `api/outpatient.js` 和 `mock/adapter.js` 同步支持接诊动作。

### 2. 病历首页与 AI 初诊建议

病历首页仍使用当前项目的总体布局和已有全局样式类，但业务上需要升级为：

- 接入 `EncounterTabs`
- 引入 `utils/aiAssistant.js`
- AI 建议不仅返回诊断文案，还要能拆出：
  - `diagnosisDraft`
  - `examRecommendations / examSuggestions`
  - `riskFlags`
  - `evidence`
- 页面生成 AI 建议后，需要同步更新：
  - 编辑区展示文案
  - `record.diagnosisText`
  - `record.examSuggestion`
- `patientStore` 中要增加 AI 结果存储字段，供报告回阅、确诊和处方阶段复用

### 3. 检查/检验申请

检查申请和检验申请都要从“单页填写即提交”升级为“带草稿、带提交记录、带跳过动作”的流程页面。

共同能力：

- 使用 `EncounterTabs`
- 使用 `RequestOrderDetailDialog`
- 申请单草稿保存在 `sessionStorage`
- 支持已提交记录列表
- 支持查看单据详情
- 支持 `skipExam` / `skipLab`
- 跳过后需要把说明追加到病历备注 `medicalRecord.notes`

状态层要求：

- `patientStore` 新增 `saveRequestDraft` / `loadRequestDraft` / `clearRequestDraft`
- `patientStore` 增加 `visit`、`examOrderSummaries`、`labOrderSummaries`

接口层要求：

- `fetchExamLabOrders`
- `skipExam`
- `skipLab`

mock 层要求：

- 能为同一就诊记录维护检查/检验申请单、状态、金额与项目项列表

### 4. 报告回阅

报告回阅是本次合入里最关键的门诊新能力之一。

入口页 `AiReportView` 需要：

- 仅查询 `visitStatus: '报告待回阅'`
- 选择患者后进入 `/ai-report-detail`

详情页 `AiReportDetailView` 需要：

- 使用 `EncounterTabs`
- 展示报告列表与当前选中报告
- 展示结构化检查结果 `examFeatures`
- 展示结构化检验结果 `labResultItems`
- 基于报告调用 `OUTPATIENT_POST_REPORT_SUGGESTION`
- 将返回结果拆分并写入 `patientStore`：
  - `aiDiagnosisResult`
  - `aiDoctorOpinion`
  - `aiReportSummary`
  - `aiPlanDraft`
  - `aiPrescriptionSuggestions`
  - `aiRiskFlags`
- 点击进入确诊前，调用 `markExamLabReportReviewed`
- 报告回阅后将当前就诊状态推进到“待确诊”

### 5. 门诊确诊

确诊入口页 `DiagnosisView` 需要：

- 仅展示 `visitStatus: '待确诊'` 的患者
- 进入详情页时复用 `patientStore.selectPatient`

确诊详情页 `DiagnosisDetailView` 需要：

- 使用 `EncounterTabs`
- 自动回填 `patientStore` 中的 AI 诊断结果与 AI 医生意见
- 调用 `saveFinalDiagnosis`
- 成功后将 `medicalRecord.finalDiagnosis`、`medicalRecord.finalOpinion`、`medicalRecord.status` 同步写回 store

### 6. 开设处方

`PrescriptionDetailView` 需要从简单“用法用量”升级为完整的处方明细输入：

- 剂量 `dosage`
- 频次 `frequency`
- 用法 `usageMethod`
- 天数 `days`
- 数量 `count`

处方提交时，接口 payload 中也应改为携带上述字段。

### 7. 费用查询

`FeeQueryView` 需要从静态数据改为真实读取当前就诊关联费用：

- 根据当前 `visitId` 调用 `fetchFeeOrders`
- 将订单项展开为表格行
- 汇总金额使用 `amount`

mock 层需要给检查、检验、处方开立结果补足费用单数据或就诊费用记录映射。

## 数据设计

### patientStore 状态扩展

在保留当前 store 基本使用方式的前提下，新增以下状态：

- `registration`
- `visit`
- `examOrderSummaries`
- `labOrderSummaries`
- `prescriptionSummaries`
- `feeOrderSummaries`
- `aiDoctorOpinion`
- `aiReportSummary`
- `aiPlanDraft`
- `aiPrescriptionSuggestions`
- `aiRiskFlags`

保留兼容字段：

- `activePatient`
- `medicalRecord`
- `examItems`
- `labItems`
- `examLabReports`
- `activeExamLabReport`
- `aiDiagnosisResult`

### medicalRecord 扩展字段

`medicalRecord` 在当前项目中已有基础字段，合入后应补齐：

- `currentTreatment`
- `finalDiagnosis`
- `finalOpinion`

### 草稿缓存 key 设计

草稿缓存按 `kind + patientId + visitId` 组合，避免多位患者或多次就诊相互污染。

缓存种类：

- `exam`
- `lab`

## API 合入设计

`src/api/outpatient.js` 需要在保留当前已有调用方式的同时，新增以下导出：

- `markExamLabReportReviewed`
- `fetchExamLabOrders`
- `fetchFeeOrders`
- `startEncounter`
- `saveFinalDiagnosis`
- `skipExam`
- `skipLab`

并增强以下行为：

- `fetchPatients` 支持 `visitStatus` / `visitGroup`
- `fetchOutpatientAiSuggestion` 超时时间提升为 90 秒
- `normalizePatient` 保留 `status` 与 `registeredAt` 映射

## Mock 合入设计

`src/mock/adapter.js` 需要继续服务当前项目全部角色，因此本次不能使用用户提供版本直接覆盖，而是要将门诊相关能力合并进当前更大的 mock 体系。

门诊需要补齐的 mock 能力包括：

- 按就诊状态筛患者
- 接诊后推进 `registration` / `visit` / `patient` 状态
- 生成门诊上下文：
  - `registration`
  - `visit`
  - `medicalRecord`
  - `examOrders`
  - `labOrders`
  - `prescriptions`
  - `feeOrders`
- 检查/检验申请单持久化
- 报告回阅状态持久化
- 最终诊断持久化
- 费用单查询
- AI 返回结构扩展，支持：
  - 初诊建议
  - 报告后建议
  - 风险提示
  - 处置建议
  - 处方建议

## 路由设计

`src/router/routes.js` 保留当前多角色路由集合，但门诊链路使用增强后的页面组件。

原则：

- 路由路径不变，减少与菜单和守卫的耦合风险
- 仅替换门诊侧对应视图组件实现
- 不回退当前挂号、药房、检查、检验路由

`src/router/index.js` 不修改。当前角色守卫已经满足整体项目需求，用户提供版本里的 `requiresAuth` / `requiresPatient` 单模块守卫不应搬回。

## 样式设计

当前项目 `src/styles/global.css` 已经包含更多面向多模块融合后的样式扩展。用户提供版本中门诊页面新增样式包括：

- `encounter-tabs`
- 报告回阅右侧 AI 面板样式
- 检查/检验申请记录与详情样式
- 病历首页增强 AI 面板样式

实现原则：

- 仅把门诊业务页面实际需要的新增类补入当前样式文件
- 不用用户提供版本的旧全量样式覆盖当前更完整的全局样式文件
- 保持当前项目已有的移动端响应式断点与视觉层次

## 实施顺序

### 阶段 1：基础能力补齐

- 合入 `utils/aiAssistant.js`
- 合入 `EncounterTabs.vue`
- 合入 `RequestOrderDetailDialog.vue`
- 扩展 `api/outpatient.js`
- 扩展 `stores/patientStore.js`

### 阶段 2：mock 数据链路补齐

- 扩展 `mock/adapter.js`
- 补齐门诊就诊状态推进、申请单、费用单、AI 响应结构

### 阶段 3：页面行为替换

- 依次合入：
  - `PatientView`
  - `MedicalHomeView`
  - `ExamRequestView`
  - `LabRequestView`
  - `AiReportView`
  - `AiReportDetailView`
  - `DiagnosisView`
  - `DiagnosisDetailView`
  - `PrescriptionDetailView`
  - `FeeQueryView`

### 阶段 4：样式收口

- 将页面运行所需新增样式补入 `global.css`
- 不处理无实际引用的样式碎片

### 阶段 5：验证

- 运行构建
- 手动检查关键链路：
  - 患者接诊
  - AI 初诊建议
  - 检查申请提交与跳过
  - 检验申请提交与跳过
  - 报告回阅与 AI 建议
  - 门诊确诊提交
  - 处方提交
  - 费用查询

## 风险与应对

### 风险 1：共享 mock 文件过大，门诊逻辑并入时容易误伤其他模块

应对：

- 只增量补充门诊接口分支
- 不改已有挂号/药房/检查/检验接口路径和响应结构

### 风险 2：patientStore 新增字段后，页面可能假定结构已存在

应对：

- 所有新增字段提供安全默认值
- 旧字段命名保持不变

### 风险 3：样式覆盖造成其他模块视觉回退

应对：

- 不整体替换 `global.css`
- 仅补充门诊新增类名和确有差异的局部布局规则

### 风险 4：用户提供版本接口返回结构与当前项目 mock 格式不完全一致

应对：

- 统一在 `api/outpatient.js` 处理归一化
- mock 输出尽量兼容当前项目的 `{ code, message, data }` 结构

## 验收标准

满足以下条件即可认为本次合入完成：

1. 当前项目仍可正常构建
2. 登录与多角色分流不受影响
3. 门诊医生从患者接诊到费用查询的完整链路可走通
4. 检查/检验/挂号/药房已有模块不出现明显回退
5. 本地 mock 模式下可以完整演示门诊新流程
