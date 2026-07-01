# 检查/检验医生工作站 API 对接文档
    
## 概述

| 项目 | 说明 |
|------|------|
| 基础路径 | `http://localhost:9400/medical-exam` |
| 响应格式 | `{"code": 0, "message": "success", "data": ...}` |
| code=0 成功 | 非0为业务错误，message 包含错误描述 |
| 日期时间格式 | `2026-06-25T17:08:27` (ISO 8601) |
| 金额类型 | `BigDecimal`，JSON 中为数字（如 `80.00`） |

## 认证头

所有接口都需要三个 Header：

| Header | 类型 | 说明 | 示例 |
|--------|------|------|------|
| `X-Doctor-Id` | Long | 医生ID | `3` |
| `X-Dept-Id` | Long | 科室ID | `3` |
| `X-Doctor-Type` | String | 医生类型 | `EXAM` 或 `LAB` |

> 检查科医生只能操作"检查"类项目，检验科医生只能操作"检验"类项目。

---

## 业务状态流转

```
┌──────────┐   缴费     ┌──────────┐   执行/采样  ┌──────────┐
│  待缴费   │ ───────> │  待执行   │ ──────────> │  已执行   │
└──────────┘           └──────────┘             └──────────┘
                                                     │
                    ┌─────────────────────────────────┤
                    │                                 │
                    ▼                                 ▼
             POST /result/exam               POST /report/draft
             POST /result/lab                (AI 生成草稿)
                    │                                 │
                    ▼                                 ▼
              结果数据入库                       ┌──────────┐
                                               │  草稿    │
                                               └──────────┘
                                                 │        │
                                    PUT /publish  │        │ POST /reject
                                    (审核发布)    │        │ (退回重做)
                                                 ▼        ▼
                                            ┌──────────┐  ┌──────────┐
                                            │  已发布   │  │  已执行   │
                                            └──────────┘  └──────────┘

状态值对照:
  feeStatus:  待支付 / 已支付
  orderStatus: 待缴费 / 待执行 / 已执行 / 已完成
  itemStatus: 待缴费 / 待执行 / 已执行 / 已完成
  reportStatus: 草稿 / 已发布
  abnormalFlag: NORMAL / LOW / HIGH / ABNORMAL
```

---

## 接口列表

### 1. 待处理列表

> 获取当前科室待处理的检查/检验项目（未完成且没有已发布报告的）

```
GET /medical-exam/order/pending
Headers: X-Dept-Id, X-Doctor-Type
```

**响应示例：**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "orderId": 1,
      "orderItemId": 101,
      "recordId": 1,
      "patientId": 1,
      "patientName": "张晓雨",
      "gender": "女",
      "itemName": "胸部DR正位片",
      "itemType": "检查",
      "amount": 80.00,
      "feeStatus": "已支付",
      "orderStatus": "待执行",
      "itemStatus": "待执行",
      "clinicalDiagnosis": "急性上呼吸道感染待查",
      "purpose": "评估肺部感染",
      "appliedAt": "2026-06-25T17:13:57",
      "executedAt": null
    }
  ]
}
```

---

### 2. 工作台

> 带统计 + 筛选的工作台列表

```
GET /medical-exam/workbench?status=all&keyword=张晓雨
Headers: X-Dept-Id, X-Doctor-Type
```

**参数：**

| 参数 | 必填 | 说明 |
|------|------|------|
| status | 否 | all / pending / progress / published，默认 all |
| keyword | 否 | 患者姓名模糊搜索 |

**响应示例：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "stats": {
      "allCount": 2,
      "pendingCount": 1,
      "progressCount": 0,
      "publishedCount": 1,
      "activeCount": 1
    },
    "items": [
      {
        "orderId": 1,
        "orderItemId": 101,
        "recordId": 1,
        "patientId": 1,
        "patientName": "张晓雨",
        "gender": "女",
        "birthday": "1992-04-18",
        "applyDoctorName": "王门诊",
        "executeDeptName": "医学影像科",
        "itemName": "胸部DR正位片",
        "itemType": "检查",
        "amount": 80.00,
        "feeStatus": "已支付",
        "orderStatus": "待执行",
        "itemStatus": "待执行",
        "clinicalDiagnosis": "急性上呼吸道感染待查",
        "purpose": "评估肺部感染",
        "appliedAt": "2026-06-25T17:13:57",
        "executedAt": null,
        "reportId": null,
        "reportStatus": null,
        "publishedAt": null,
        "workbenchStatus": "pending"
      }
    ]
  }
}
```

**workbenchStatus 含义：**
| 值 | 含义 |
|----|------|
| pending | 待处理（未执行，无报告） |
| progress | 进行中（已执行或已有草稿） |
| published | 已完成（已发布或项目已完成） |

---

### 3. 执行项目

> 将检查项目标记为"已执行"

```
POST /medical-exam/order/execute
Headers: X-Doctor-Id, X-Dept-Id, X-Doctor-Type
Content-Type: application/json
```

**请求体：**
```json
{"orderItemId": 101}
```

**响应：** 返回更新后的 ExamLabTaskView

**业务规则：** 仅限检查类（EXAM），须已支付，不可重复执行。

---

### 4. 标本采样确认

> 检验科专用：确认标本已采集，标记为"已执行"

```
POST /medical-exam/order/sample
Headers: X-Doctor-Id, X-Dept-Id, X-Doctor-Type
Content-Type: application/json
```

**请求体：**
```json
{
  "orderItemId": 102,
  "sampleId": "SMP20260625001"
}
```

**响应：** `{"code": 0, "message": "success"}`

**业务规则：** 仅限检验类（LAB），不可重复采样。

---

### 5. 保存检查结果

> 录入检查类项目（心电图、CT）的结构化结果

```
POST /medical-exam/result/exam
Headers: X-Doctor-Id, X-Dept-Id, X-Doctor-Type
Content-Type: application/json
```

**心电图请求体：**
```json
{
  "orderItemId": 101,
  "itemName": "心电图",
  "resultData": {
    "hr": "78",
    "pr": "160",
    "qrs": "92",
    "qtc": "420",
    "rhythm": "窦性心律",
    "axis": "正常电轴"
  }
}
```

**CT 请求体：**
```json
{
  "orderItemId": 101,
  "itemName": "胸部CT",
  "resultData": {
    "findings": ["双肺纹理清晰", "右肺上叶结节"],
    "notes": "建议3个月后复查"
  }
}
```

**响应：** `{"code": 0, "message": "success"}`

**业务规则：** 项目须为"已执行"状态，会先清除旧结果再写入。异常标记自动计算（数值型：LOW/HIGH/NORMAL，枚举型：选非正常值即为 ABNORMAL）。

---

### 6. 保存检验结果

> 录入检验类项目（血常规、尿常规等）的指标值

```
POST /medical-exam/result/lab
Headers: X-Doctor-Id, X-Dept-Id, X-Doctor-Type
Content-Type: application/json
```

**请求体：**
```json
{
  "orderItemId": 102,
  "itemName": "血常规",
  "values": {
    "WBC": "8.5",
    "RBC": "4.6",
    "HGB": "138",
    "PLT": "215",
    "NEUT": "58"
  }
}
```

**响应：** `{"code": 0, "message": "success"}`

---

### 7. 获取录入模板

> 获取某个检查/检验项目的字段定义（参考范围、选项等），前端据此渲染录入表单

```
GET /medical-exam/item/schema?itemName=心电图
```

**检查类响应（心电图）：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "itemName": "心电图",
    "schemaType": "ecg",
    "fields": [
      {"key": "hr", "label": "心率", "unit": "次/分", "type": "numeric", "low": "60", "high": "100", "def": "96", "options": null, "normal": null},
      {"key": "pr", "label": "PR 间期", "unit": "ms", "type": "numeric", "low": "120", "high": "200", "def": "168", "options": null, "normal": null},
      {"key": "qrs", "label": "QRS 时限", "unit": "ms", "type": "numeric", "low": "80", "high": "120", "def": "92", "options": null, "normal": null},
      {"key": "qtc", "label": "QTc", "unit": "ms", "type": "numeric", "low": "350", "high": "450", "def": "432", "options": null, "normal": null}
    ],
    "findingOptions": null,
    "rhythmOptions": ["窦性心律", "窦性心动过速", "窦性心动过缓", "心房颤动", "室性早搏"],
    "axisOptions": ["正常电轴", "电轴左偏", "电轴右偏"]
  }
}
```

**检验类响应（血常规）：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "itemName": "血常规",
    "schemaType": "lab",
    "fields": [
      {"key": "WBC", "label": "白细胞计数", "unit": "×10⁹/L", "type": "numeric", "low": "4", "high": "10", "def": "6.2", "options": null, "normal": null},
      {"key": "RBC", "label": "红细胞计数", "unit": "×10¹²/L", "type": "numeric", "low": "3.5", "high": "5.5", "def": "4.6", "options": null, "normal": null},
      {"key": "HGB", "label": "血红蛋白", "unit": "g/L", "type": "numeric", "low": "110", "high": "160", "def": "138", "options": null, "normal": null},
      {"key": "PLT", "label": "血小板计数", "unit": "×10⁹/L", "type": "numeric", "low": "100", "high": "300", "def": "215", "options": null, "normal": null},
      {"key": "NEUT", "label": "中性粒细胞百分比", "unit": "%", "type": "numeric", "low": "40", "high": "75", "def": "58", "options": null, "normal": null}
    ],
    "findingOptions": null,
    "rhythmOptions": null,
    "axisOptions": null
  }
}
```

**已支持的检查/检验项目：**
| 项目 | schemaType | 类型 |
|------|------------|------|
| 心电图 | ecg | 检查 |
| 胸部CT | ct | 检查 |
| 血常规 | lab | 检验 |
| 尿常规 | lab | 检验 |
| C反应蛋白 | lab | 检验 |
| 肝功能 | lab | 检验 |
| 肾功能 | lab | 检验 |
| 血糖 | lab | 检验 |

---

### 8. 生成 AI 报告草稿

> 调用 AI（AntLing）生成检查/检验报告草稿

```
POST /medical-exam/report/draft
Headers: X-Doctor-Id, X-Dept-Id, X-Doctor-Type
Content-Type: application/json
```

**请求体：**
```json
{
  "orderItemId": 101,
  "resultDetail": "胸部DR示双肺纹理稍增多，右肺上叶见小结节影，直径约3mm",
  "aiReportContent": ""
}
```

| 字段 | 说明 |
|------|------|
| orderItemId | 项目ID（须已执行） |
| resultDetail | 检查所见 / 结果摘要（自由文本，喂给AI） |
| aiReportContent | AI调用失败时的备用文本（可选） |

**响应示例：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "reportId": 1,
    "orderId": 1,
    "orderItemId": 101,
    "recordId": 1,
    "patientId": 1,
    "patientName": "张晓雨",
    "itemName": "胸部DR正位片",
    "reportType": "检查",
    "findings": "胸部DR示双肺纹理稍增多，右肺上叶见小结节影，直径约3mm",
    "conclusion": "",
    "aiDraft": "AI辅助草稿：\n胸部DR正位片示双肺纹理稍增多，右肺上叶见小结节影，直径约3mm。\n\n可能方向：\n可能是良性结节，建议结合临床随访观察...\n\n风险级别：\nLOW\n\n提示：AI内容仅供辅助参考，请医生确认后发布。",
    "doctorReview": "AI内容仅供辅助参考，请医生确认后发布。",
    "status": "草稿",
    "createdAt": "2026-06-25T17:08:27",
    "publishedAt": null
  }
}
```

**业务规则：** 项目须已执行，同项目只允许一个草稿（退回后可重新生成）。

---

### 9. 发布报告

> 医生确认结论后发布报告，标记项目完成

```
PUT /medical-exam/report/publish
Headers: X-Doctor-Id, X-Dept-Id, X-Doctor-Type
Content-Type: application/json
```

**请求体：**
```json
{
  "reportId": 1,
  "doctorConclusion": "支气管炎可能，建议随访观察"
}
```

**响应：** 返回更新后的 ExamLabReportView，status="已发布"

**业务规则：** reportId 对应的报告须为"草稿"状态，conclusion 不能为空。同一订单下所有项目都完成后，订单自动标记为"已完成"。

---

### 10. 退回报告

> 删除草稿报告，将项目状态退回"已执行"（允许修改结果后重新生成）

```
POST /medical-exam/report/reject
Headers: X-Doctor-Id, X-Dept-Id, X-Doctor-Type
Content-Type: application/json
```

**请求体：**
```json
{"orderItemId": 101}
```

**响应：** `{"code": 0, "message": "success"}`

**业务规则：** 仅限草稿状态的报告，已发布的不可退回。

---

### 11. 报告详情

```
GET /medical-exam/report/detail?reportId=1
```

**响应：** ExamLabReportView（格式同接口8的 data 字段）

---

### 12. 按病历查报告

```
GET /medical-exam/report/by-record?recordId=1
```

**响应：** `List<ExamLabReportView>`

---

### 13. 项目详情（含结果数据）

> 获取单个 orderItem 的完整信息，含已录入的结果特征/指标

```
GET /medical-exam/order/item-detail?orderItemId=101
```

**响应示例：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": 1,
    "orderItemId": 101,
    "recordId": 1,
    "patientId": 1,
    "patientName": "张晓雨",
    "gender": "女",
    "birthday": "1992-04-18",
    "applyDoctorName": "王门诊",
    "executeDeptName": "医学影像科",
    "itemName": "胸部DR正位片",
    "itemType": "检查",
    "amount": 80.00,
    "feeStatus": "已支付",
    "orderStatus": "已执行",
    "itemStatus": "已执行",
    "clinicalDiagnosis": "急性上呼吸道感染待查",
    "purpose": "评估肺部感染",
    "appliedAt": "2026-06-25T17:13:57",
    "executedAt": "2026-06-25T17:15:00",
    "sampleId": null,
    "reportId": null,
    "reportNo": null,
    "reportStatus": null,
    "findings": null,
    "conclusion": null,
    "aiDraft": null,
    "doctorReview": null,
    "publishedAt": null,
    "resultFeatures": [
      {"featureName": "心率", "featureValue": "78", "unit": "次/分", "abnormalFlag": "NORMAL", "sortOrder": 1}
    ],
    "labResultItems": []
  }
}
```

---

## 错误码

| code | 说明 |
|------|------|
| 0 | 成功 |
| 4000 | 业务异常（message 字段包含具体原因） |

常见 message：
- "检查/检验项目未缴费，暂不可执行"
- "检查/检验项目未执行，暂不可生成报告"
- "报告草稿已存在，不可重复生成"
- "请医生确认报告结论后再发布"
- "只有草稿报告可以发布"
- "不支持的检查/检验项目: xxx"
- "医生类型不能处理该项目"

---

## 启动方式

```bash
# H2 内存库 + 真实AI（无需MySQL，适合开发联调）
mvn spring-boot:run -Dspring-boot.run.profiles=h2

# 生产模式（MySQL + 真实AI）
set AI_PROVIDER=ant-ling
mvn spring-boot:run
```

**h2 模式预置数据：**
- 医生：钱检查（EXAM, deptId=3, doctorId=3）、孙检验（LAB, deptId=4, doctorId=4）
- 患者：张晓雨（patientId=1）
- 项目：胸部DR正位片（101, 检查）、血常规（102, 检验）