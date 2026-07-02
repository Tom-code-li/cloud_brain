# 医生端后端

## 技术栈

- Java 17
- Spring Boot 3.x
- Spring Cloud Gateway
- JWT
- Maven 多模块
- MySQL 8 兼容 SQL

## 模块与端口

- `doctor-gateway`：9000
- `doctor-auth`：9100
- `module-registration`：9200
- `module-outpatient`：9300
- `module-medical-exam`：9400
- `module-pharmacy`：9500
- `module-ai-assistant`：9600

## 启动

在多个终端分别运行：

```powershell
cd doctor-platform-backend
mvn -pl doctor-auth spring-boot:run
mvn -pl doctor-gateway spring-boot:run
mvn -pl doctor-modules/module-registration spring-boot:run
mvn -pl doctor-modules/module-outpatient spring-boot:run
mvn -pl doctor-modules/module-medical-exam spring-boot:run
mvn -pl doctor-modules/module-pharmacy spring-boot:run
mvn -pl doctor-modules/module-ai-assistant spring-boot:run
```

## 数据库

- 建表脚本：`sql/schema.sql`
- 演示数据：`sql/seed.sql`

脚本包含患者、医生、挂号、分诊、费用、退费、门诊病历、检查检验、报告、处方、药房、库存、AI 调用日志等 27 张主要表，方便后续患者端和管理员端整合。

## 验证

```powershell
cd doctor-platform-backend
mvn test
```

## 业务边界

- AI 只生成辅助建议和草稿，不直接修改挂号、病历、报告、处方、库存等业务状态。
- 检查/检验报告必须由对应检查/检验医生确认发布。
- 门诊医生先写报告前初诊病历，报告完成后再补全完整病历。
- 未缴费的检查/检验和处方发药会被业务服务拦截。
