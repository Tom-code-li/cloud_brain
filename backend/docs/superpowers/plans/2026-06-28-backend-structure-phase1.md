# Backend Structure Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure the backend into a safer enterprise-style Spring Boot + MyBatis layout without changing runtime behavior.

**Architecture:** Keep the current domain-first style, but make boundaries more standard by splitting framework-level concerns into `infrastructure`, generic shared code into `shared`, and consolidating the outpatient main flow into `modules.outpatient`. Preserve service logic and HTTP contracts during this phase.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Maven, JUnit 5, H2 test profile

---

### Task 1: Move common framework code into `shared` and `infrastructure`

**Files:**
- Modify: `backend/src/main/java/com/doctor/platform/DoctorPlatformApplication.java`
- Move: `backend/src/main/java/com/doctor/platform/common/api/ApiResponse.java`
- Move: `backend/src/main/java/com/doctor/platform/common/config/SecurityConfig.java`
- Move: `backend/src/main/java/com/doctor/platform/common/config/WebConfig.java`
- Move: `backend/src/main/java/com/doctor/platform/common/exception/BusinessException.java`
- Move: `backend/src/main/java/com/doctor/platform/common/exception/GlobalExceptionHandler.java`
- Test: `backend/src/test/java/com/doctor/platform/ai/AiSuggestionClientWiringTest.java`

- [ ] **Step 1: Create the target package directories**

Run:

```powershell
New-Item -ItemType Directory -Force `
  'backend\src\main\java\com\doctor\platform\shared\api', `
  'backend\src\main\java\com\doctor\platform\infrastructure\config', `
  'backend\src\main\java\com\doctor\platform\infrastructure\exception'
```

Expected: Directories are created without touching business code.

- [ ] **Step 2: Move `ApiResponse` into `shared.api`**

Update package declaration from:

```java
package com.doctor.platform.common.api;
```

to:

```java
package com.doctor.platform.shared.api;
```

Expected: All business modules import the same response model from a neutral shared package.

- [ ] **Step 3: Move config and exception classes into `infrastructure`**

Update package declarations:

```java
package com.doctor.platform.infrastructure.config;
package com.doctor.platform.infrastructure.exception;
```

Expected: Framework wiring and global exception handling are no longer mixed into a business-named `common` package.

- [ ] **Step 4: Rewrite imports that reference old `common` packages**

Use a project-wide search to replace:

```text
com.doctor.platform.common.api
com.doctor.platform.common.config
com.doctor.platform.common.exception
```

with:

```text
com.doctor.platform.shared.api
com.doctor.platform.infrastructure.config
com.doctor.platform.infrastructure.exception
```

Expected: Compilation points to the new neutral package layout.

- [ ] **Step 5: Run a focused regression test**

Run:

```powershell
cd backend
mvn -q "-Dtest=AiSuggestionClientWiringTest" test
```

Expected: PASS

### Task 2: Consolidate outpatient main-flow domain packages

**Files:**
- Move: `backend/src/main/java/com/doctor/platform/patient/entity/Patient.java`
- Move: `backend/src/main/java/com/doctor/platform/patient/mapper/PatientMapper.java`
- Move: `backend/src/main/java/com/doctor/platform/visit/entity/OutpatientVisit.java`
- Move: `backend/src/main/java/com/doctor/platform/visit/mapper/OutpatientVisitMapper.java`
- Move: `backend/src/main/java/com/doctor/platform/record/entity/MedicalRecord.java`
- Move: `backend/src/main/java/com/doctor/platform/record/mapper/MedicalRecordMapper.java`
- Move: `backend/src/main/java/com/doctor/platform/registration/entity/Registration.java`
- Move: `backend/src/main/java/com/doctor/platform/registration/mapper/RegistrationMapper.java`
- Move: `backend/src/main/java/com/doctor/platform/outpatient/controller/OutpatientWorkbenchController.java`
- Move: `backend/src/main/java/com/doctor/platform/outpatient/service/OutpatientWorkbenchService.java`
- Move: `backend/src/main/java/com/doctor/platform/outpatient/dto/*.java`
- Test: `backend/src/test/java/com/doctor/platform/FallbackDatabaseIntegrationTest.java`
- Test: `backend/src/test/java/com/doctor/platform/order/OrderWorkflowIntegrationTest.java`

- [ ] **Step 1: Create the new outpatient module directories**

Run:

```powershell
New-Item -ItemType Directory -Force `
  'backend\src\main\java\com\doctor\platform\modules\outpatient\controller', `
  'backend\src\main\java\com\doctor\platform\modules\outpatient\service', `
  'backend\src\main\java\com\doctor\platform\modules\outpatient\dto', `
  'backend\src\main\java\com\doctor\platform\modules\outpatient\entity', `
  'backend\src\main\java\com\doctor\platform\modules\outpatient\mapper'
```

Expected: New domain home exists before any class movement.

- [ ] **Step 2: Move outpatient entities and mappers into the new module**

Update packages so these types live under:

```java
com.doctor.platform.modules.outpatient.entity
com.doctor.platform.modules.outpatient.mapper
```

Expected: `Patient`, `OutpatientVisit`, `MedicalRecord`, and `Registration` become part of one coherent outpatient domain boundary.

- [ ] **Step 3: Move outpatient DTOs, controller, and service**

Update packages so these types live under:

```java
com.doctor.platform.modules.outpatient.dto
com.doctor.platform.modules.outpatient.controller
com.doctor.platform.modules.outpatient.service
```

Expected: Main patient selection, context loading, and medical record saving now sit with the same domain data they orchestrate.

- [ ] **Step 4: Rewrite imports across dependent modules**

Update all references in:

```text
ai
examlab
prescription
fee
pharmacy
auth
tests
```

to point at:

```text
com.doctor.platform.modules.outpatient.*
```

Expected: Cross-module references stay explicit, while outpatient internals stop leaking through several top-level packages.

- [ ] **Step 5: Run a focused integration regression**

Run:

```powershell
cd backend
mvn -q "-Dtest=FallbackDatabaseIntegrationTest,OrderWorkflowIntegrationTest" test
```

Expected: PASS

### Task 3: Clean package scanning and verify no behavior change

**Files:**
- Modify: `backend/src/main/java/com/doctor/platform/DoctorPlatformApplication.java`
- Test: `backend/src/test/java/com/doctor/platform/ai/AiEnvConfigurationTest.java`
- Test: `backend/src/test/java/com/doctor/platform/ai/AiReportAnalysisIntegrationTest.java`
- Test: `backend/src/test/java/com/doctor/platform/examlab/ExamLabReportControllerTest.java`

- [ ] **Step 1: Verify `@MapperScan` still matches the moved mapper packages**

Keep or adjust:

```java
@MapperScan("com.doctor.platform.**.mapper")
```

Expected: MyBatis continues to find moved mappers and does not scan service interfaces.

- [ ] **Step 2: Run AI-focused regression tests**

Run:

```powershell
cd backend
mvn -q "-Dtest=AiEnvConfigurationTest,AiSuggestionClientWiringTest,AiReportAnalysisIntegrationTest,ExamLabReportControllerTest" test
```

Expected: PASS

- [ ] **Step 3: Run the full backend test suite**

Run:

```powershell
cd backend
mvn -q test
```

Expected: PASS

- [ ] **Step 4: Document the new package layout in the final summary**

Report the new structure in terms of:

```text
shared
infrastructure
modules.outpatient
remaining phase-2 domains
```

Expected: The user gets a clear migration checkpoint and knows what still belongs to phase 2.
