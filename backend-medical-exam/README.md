# Medical Exam Backend

独立的检查检验后端，只负责检查/检验订单执行、报告草稿、AI 辅助草稿、报告发布和报告查询。

## 启动

```powershell
cd backend
$env:MEDICAL_EXAM_DB_USERNAME="root"
$env:MEDICAL_EXAM_DB_PASSWORD="你的MySQL密码"
$env:AI_PROVIDER="simulated"
mvn spring-boot:run
```

默认端口：`9400`

## 测试与覆盖率

后端测试代码位于 `src/test/java`，使用 JUnit5、AssertJ、Mockito、Spring Boot Test 和 H2 测试库。

运行单元测试：

```powershell
mvn test
```

运行测试并生成 JaCoCo 覆盖率报告：

```powershell
mvn verify
```

JaCoCo HTML 报告位置：

```text
target/site/jacoco/index.html
```

本次本地执行 `mvn verify` 结果：37 个测试全部通过，JaCoCo 汇总覆盖率为指令 77.02%、行 75.78%、分支 50.00%、方法 82.30%。

主要测试内容：

- `MedicalExamWorkflowServiceTest`：检查/检验待执行任务、执行校验、草稿生成、结果录入、退回重录、报告发布、病历报告查询、门诊状态同步和工作台筛选统计。
- `MedicalExamControllerTest`：控制器响应封装、医生/科室请求头传递、发布报告和工作台筛选接口。
- `ReportNoGeneratorTest`：日报告编号从空序列开始、按最大序号递增、异常序号回退处理。
- `AiPromptBuilderTest`、`AiDraftServiceTest`：AI 提示词上下文、空字段兜底和模拟 AI 草稿内容。
- `GlobalExceptionHandlerTest`、`CommonResponseTest`、`EntityMappingTest`：统一响应、业务异常响应和关键实体字段映射。

## 初始化测试数据

先确保旧项目的 `schema.sql` 已经创建好表，然后执行：

```sql
SOURCE sql/dev-seed-medical-exam.sql;
```

检查种子数据：

```sql
SELECT order_item_id, item_name, item_type, status
FROM exam_lab_order_item
WHERE order_item_id IN (910101, 910102, 920101, 920102, 920103, 920104, 920105, 920106)
ORDER BY order_item_id;
```

应该返回 8 条：胸部DR、心电图、血常规、C反应蛋白、尿常规、肝功能、肾功能、血糖。

## Postman 验收

### 1. 查询检查待执行

```http
GET http://localhost:9400/medical-exam/order/pending
X-Dept-Id: 3
X-Doctor-Type: EXAM
```

返回 `data` 中应包含 `orderItemId = 910101` 和 `910102`。

### 2. 执行胸部DR

```http
POST http://localhost:9400/medical-exam/order/execute
X-Doctor-Id: 3
X-Dept-Id: 3
X-Doctor-Type: EXAM
Content-Type: application/json

{
  "orderItemId": 910101
}
```

返回 `itemStatus` 应为 `已执行`。

### 3. 保存胸部DR草稿

```http
POST http://localhost:9400/medical-exam/report/draft
X-Doctor-Id: 3
X-Dept-Id: 3
X-Doctor-Type: EXAM
Content-Type: application/json

{
  "orderItemId": 910101,
  "resultDetail": "胸部DR示双肺纹理稍增多，右下肺可疑斑片影，心影大小形态未见明显异常。",
  "aiReportContent": ""
}
```

记录返回值里的 `data.reportId`，发布接口要用这个真实值。

### 4. 发布报告

```http
PUT http://localhost:9400/medical-exam/report/publish
X-Doctor-Id: 3
X-Dept-Id: 3
X-Doctor-Type: EXAM
Content-Type: application/json

{
  "reportId": "替换为上一步返回的 reportId",
  "doctorConclusion": "考虑支气管炎或早期感染可能，请结合临床症状及血常规结果。"
}
```

返回 `status` 应为 `已发布`。

### 5. 查询报告详情

```http
GET http://localhost:9400/medical-exam/report/detail?reportId=替换为真实reportId
```

### 6. 门诊按病历查看检查检验报告

```http
GET http://localhost:9400/medical-exam/report/by-record?recordId=9001
```

## AI 模式

默认：

```powershell
$env:AI_PROVIDER="simulated"
```

真实 Ant Ling：

```powershell
$env:AI_PROVIDER="ant-ling"
$env:ANT_LING_API_KEY="你的真实密钥"
$env:ANT_LING_BASE_URL="https://api.ant-ling.com/v1/"
$env:ANT_LING_MODEL="Ling-2.6-flash"
```

AI 调用失败时，业务不会失败，会回落到请求体里的 `aiReportContent`，没有传则保存为空字符串。
