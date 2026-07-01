# 检查/检验医生工作站 · 前端开发计划（Vite + Vue3）

> 配套后端：`API.md`（基础路径 `http://localhost:9400/medical-exam`）
> 设计参考：此前交付的纯前端 HTML demo（样式、交互流程）
> 身份信息（`X-Doctor-Id` / `X-Dept-Id` / `X-Doctor-Type`）对接方式：**待定，由已有登录系统提供，后续补充**

---

## 1. 技术选型

| 项 | 选择 | 说明 |
|---|---|---|
| 构建工具 | Vite | — |
| 框架 | Vue 3 + `<script setup>` | 组合式 API |
| 路由 | Vue Router | 对应旧 demo 的多页结构 |
| 请求库 | axios | 统一拦截器处理 Header 注入与错误码 |
| 状态管理 | 轻量 store（`reactive`/`ref`，按需引入 Pinia） | 主要用于身份信息与少量全局状态 |
| 样式 | 复用旧 HTML demo 的 CSS 变量与类名 | 不引入 UI 组件库，保持视觉一致 |
| 开发期跨域 | Vite dev proxy → `http://localhost:9400` | 避免本地联调 CORS 问题 |

---

## 2. 项目结构

```
medical-exam-frontend/
├─ vite.config.js          # proxy 转发到 http://localhost:9400
├─ .env.development
├─ .env.production
└─ src/
   ├─ main.js
   ├─ App.vue
   ├─ router/
   │  └─ index.js          # 路由 + 按 doctorType 的访问守卫
   ├─ api/
   │  ├─ http.js            # axios 实例：请求拦截器注入身份 Header，响应拦截器统一处理 code/message
   │  ├─ order.js            # pending / workbench / execute / sample / item-detail
   │  ├─ result.js           # result/exam, result/lab
   │  ├─ schema.js            # item/schema
   │  └─ report.js            # draft / publish / reject / detail / by-record
   ├─ stores/
   │  ├─ auth.js             # ★扩展点：doctorId / deptId / doctorType，对接登录系统
   │  └─ toast.js             # 全局提示
   ├─ composables/
   │  ├─ useWorkbench.js      # 列表筛选 / 搜索 / 统计
   │  ├─ useItemDetail.js     # 详情页状态机
   │  └─ useSchemaForm.js     # 按 schemaType 动态渲染表单
   ├─ components/
   │  ├─ layout/              # TopBar, StatusBadge, Stepper
   │  ├─ workbench/           # FilterBar, RequestRow
   │  └─ detail/
   │     ├─ PatientBanner.vue
   │     ├─ PaymentAlert.vue
   │     ├─ ExecuteCard.vue
   │     ├─ ResultForm/
   │     │  ├─ EcgForm.vue
   │     │  ├─ CtForm.vue
   │     │  └─ LabPanelForm.vue
   │     ├─ AiAssistRail.vue
   │     ├─ ReportReview.vue
   │     └─ TimelinePanel.vue
   ├─ views/
   │  ├─ HomeView.vue
   │  ├─ ExamListView.vue
   │  ├─ ExamDetailView.vue
   │  ├─ LabListView.vue
   │  └─ LabDetailView.vue
   ├─ styles/
   │  └─ tokens.css           # 旧 demo 的 :root 设计令牌原样搬入
   └─ utils/
      ├─ format.js            # 时间/数值格式化
      └─ statusMap.js         # 状态 → 阶段 / 文案 / 颜色 映射
```

---

## 3. 身份信息（Header）扩展点

- `stores/auth.js` 只暴露三个字段：`doctorId` / `deptId` / `doctorType`，以及一个 `setIdentity(payload)` 方法。
- `api/http.js` 的请求拦截器从该 store 读取三个值并注入到请求 Header；**缺失时直接拦截请求并提示，不允许带空身份发出请求**。
- 对接已有登录系统时，只需要在登录成功处调用一次 `auth.setIdentity({ doctorId, deptId, doctorType })`（或在拦截器里改为读取已有的 token / 用户上下文），其余模块均无需改动。

> ⏳ **待你补充**：登录系统具体如何把这三个值交给前端（解析 token？专门的 `/me` 接口？还是登录响应里直接带？）

---

## 4. 路由设计

| 路径 | 组件 | 对应旧 demo | 说明 |
|---|---|---|---|
| `/` | HomeView | `index.html` | 角色入口 + 统计 |
| `/exam` | ExamListView | `exam-list.html` | `doctorType === 'EXAM'` 时可访问 |
| `/exam/:orderItemId` | ExamDetailView | `exam-detail.html` | 心电图 / 胸部CT |
| `/lab` | LabListView | `lab-list.html` | `doctorType === 'LAB'` 时可访问 |
| `/lab/:orderItemId` | LabDetailView | `lab-detail.html` | 检验详情 |

路由守卫按 `auth.doctorType` 拦截越权访问（检查医生进不了 `/lab/*`，反之亦然）。

---

## 5. API 封装对应表

| 函数 | 方法 / 路径 | 用途 |
|---|---|---|
| `getWorkbench(status, keyword)` | `GET /workbench` | 列表页主数据源（含统计） |
| `getItemDetail(orderItemId)` | `GET /order/item-detail` | 详情页主数据源 |
| `getItemSchema(itemName)` | `GET /item/schema` | 动态渲染结果录入表单 |
| `executeOrder(orderItemId)` | `POST /order/execute` | 检查类“开始执行” |
| `confirmSample(orderItemId, sampleId)` | `POST /order/sample` | 检验类“确认采样” |
| `saveExamResult(payload)` | `POST /result/exam` | 心电图 / CT 结果保存 |
| `saveLabResult(payload)` | `POST /result/lab` | 检验指标保存 |
| `generateAiDraft(payload)` | `POST /report/draft` | AI 报告草稿 |
| `publishReport(reportId, doctorConclusion)` | `PUT /report/publish` | 审核发布 |
| `rejectReport(orderItemId)` | `POST /report/reject` | 退回重做 |
| `getReportDetail(reportId)` | `GET /report/detail` | 已发布报告只读展示 |

---

## 6. 详情页状态机

用 `utils/statusMap.js` 中的一个 `resolveStage(itemDetail)` 函数统一判定，Stepper 与各 Card 组件都只读取这一个函数的结果，不各自判断：

| 条件 | 阶段 |
|---|---|
| `feeStatus === '待支付'` | ① 缴费确认（拦住，不可继续） |
| 已支付 且 `itemStatus === '待执行'` | ② 执行 / 采样 |
| `itemStatus === '已执行'` 且无 `reportId` | ③ 结果录入（右侧 AI 草稿按钮可用） |
| `reportStatus === '草稿'` | ④ 审核发布（展示 `aiDraft`，编辑 `doctorConclusion`） |
| `reportStatus === '已发布'` | ⑤ 只读展示 `conclusion` + `publishedAt` |

---

## 7. 动态表单渲染

`schemaType` 为 `ecg` / `ct` / `lab`，分别对应 `EcgForm.vue` / `CtForm.vue` / `LabPanelForm.vue` 三个表单组件。

通用渲染规则：`field.options` 非空 → 渲染下拉框；否则 → 数值输入框，并按 `low` / `high` 做异常高亮。

> ⏳ **待你补充**：枚举型检验字段（如尿常规“阴性 / 弱阳性”）在 `/item/schema` 真实响应中的具体结构，文档示例均为数值型（`options` / `normal` 为 `null`）。结构一旦不同，按上述分支调整即可。

---

## 8. AI 报告生成流程

1. 结果保存成功后，前端按 `itemType` 自动拼一段摘要文字（例：“心率78次/分，PR间期160ms…窦性心律，正常电轴。”），作为 `resultDetail` 的默认值，医生可编辑。
2. 提交编辑后的文本调用 `POST /report/draft`。
3. 返回的 `aiDraft` 只读展示；`doctorConclusion` 用单独的输入框，留给医生填写最终结论。
4. **发布用 `reportId`，退回用 `orderItemId`**（两个接口参数不同，组件状态中需同时保存这两个 id）。

---

## 9. 样式迁移

- 把旧 `style.css` 中的 `:root` 设计令牌原样搬入 `styles/tokens.css`，全局引入。
- `.card` / `.btn` / `.badge` / `.flag` 等通用类名继续沿用。
- 紫色 AI 辅助区、数值 mono 字体等视觉细节保持不变。
- 组件专属样式用 `<style scoped>` 补充，不污染全局。

---

## 10. 建议实施顺序

1. 脚手架 + 路由 + 样式令牌搬运 → 首页和两个列表页先接通 `/workbench` 跑起来
2. 详情页只读部分（`item-detail` + Stepper + PatientBanner）
3. 执行 / 采样写操作
4. 结果录入表单（先打通 ECG，再扩展 CT / Lab）
5. AI 草稿生成 + 发布 / 退回
6. 边界状态打磨（未支付、已发布只读、错误提示文案直接复用文档中列出的 `message`）

---

## 11. 待确认事项清单

- [ ] 登录系统具体如何把 `doctorId` / `deptId` / `doctorType` 交给前端（解析 token？专门的 `/me` 接口？全局状态注入？）
- [ ] 枚举型检验字段在 `/item/schema` 中的真实响应结构
