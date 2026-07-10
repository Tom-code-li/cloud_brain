# Registration Backend Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Route the registration frontend module to the newly added `backend-registration` services while preserving the existing login flow and the already-working outpatient, medical-exam, and pharmacy integrations.

**Architecture:** Keep the current unified login on `backend:8080`, but split registration traffic away from `/api` by introducing dedicated frontend HTTP clients and Vite proxies for `module-registration:9200` and `module-ai-assistant:9600`. The registration views continue using the same Vue components and overall visual style, but their data calls stop depending on the main backend and instead target the new registration backend stack.

**Tech Stack:** Vue 3, Vite dev proxy, Axios, native `fetch` for SSE, Spring Boot registration backend modules on ports 9200/9600, Vitest.

---

## File Structure

- Modify: `frontend/vite.config.js`
- Modify: `frontend/src/api/http.js`
- Create: `frontend/src/api/registrationHttp.js`
- Create: `frontend/src/api/registrationAiHttp.js`
- Modify: `frontend/src/api/registration.js`
- Modify: `frontend/src/views/registration/OfflineRegistration.vue`
- Modify: `frontend/src/views/registration/OnlineRegistrationConfirm.vue`
- Modify: `frontend/src/views/registration/RegistrationDashboard.vue`
- Modify: `frontend/src/views/registration/FeeManagement.vue`
- Modify: `frontend/src/views/registration/FeeManagementDetail.vue`
- Modify: `frontend/src/views/registration/RefundManagement.vue`
- Create: `frontend/tests/unit/registrationBackendRouting.test.js`
- Create: `frontend/tests/unit/registrationAiStreamContract.test.js`

### Task 1: Lock the new registration routing contract with tests

**Files:**
- Create: `frontend/tests/unit/registrationBackendRouting.test.js`
- Create: `frontend/tests/unit/registrationAiStreamContract.test.js`
- Test: `frontend/tests/unit/registrationBackendRouting.test.js`
- Test: `frontend/tests/unit/registrationAiStreamContract.test.js`

- [ ] **Step 1: Write the failing proxy-routing contract test**

```javascript
import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

function read(path) {
  return fs.readFileSync(path, 'utf8');
}

describe('registration backend routing', () => {
  it('adds dedicated Vite proxies for registration business and registration AI traffic', () => {
    const source = read('vite.config.js');

    expect(source).toContain("'/registration-api'");
    expect(source).toContain("target: 'http://127.0.0.1:9200'");
    expect(source).toContain("'/registration-ai'");
    expect(source).toContain("target: 'http://127.0.0.1:9600'");
  });

  it('keeps the existing /api and /medical-exam proxies intact', () => {
    const source = read('vite.config.js');

    expect(source).toContain("'/api'");
    expect(source).toContain("target: 'http://127.0.0.1:8080'");
    expect(source).toContain("'/medical-exam'");
    expect(source).toContain("target: 'http://127.0.0.1:9400'");
  });
});
```

- [ ] **Step 2: Write the failing registration AI stream contract test**

```javascript
import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

function read(path) {
  return fs.readFileSync(path, 'utf8');
}

describe('registration AI stream contract', () => {
  it('routes registration AI stream requests to the dedicated registration AI base path', () => {
    const source = read('src/views/registration/OfflineRegistration.vue');

    expect(source).toContain('/registration-ai/ai/registration/stream');
    expect(source).not.toContain(\"const baseUrl = direct ? 'http://127.0.0.1:9600' : '/api'\");
  });
});
```

- [ ] **Step 3: Run the tests to verify they fail for the expected reason**

Run:

```powershell
npm test -- --run tests/unit/registrationBackendRouting.test.js tests/unit/registrationAiStreamContract.test.js
```

Expected:

```text
FAIL tests/unit/registrationBackendRouting.test.js
FAIL tests/unit/registrationAiStreamContract.test.js
```

- [ ] **Step 4: Commit the failing tests**

```powershell
git add frontend/tests/unit/registrationBackendRouting.test.js frontend/tests/unit/registrationAiStreamContract.test.js
git commit -m "test: cover registration backend routing contract"
```

### Task 2: Add dedicated registration transport clients

**Files:**
- Modify: `frontend/src/api/http.js`
- Create: `frontend/src/api/registrationHttp.js`
- Create: `frontend/src/api/registrationAiHttp.js`
- Modify: `frontend/src/api/registration.js`
- Test: `frontend/tests/unit/registrationBackendRouting.test.js`

- [ ] **Step 1: Extend the shared HTTP layer with a reusable client factory**

```javascript
import axios from 'axios';
import { isLocalMockEnabled, mockAdapter } from '../mock/adapter.js';
import { clearAuthSession, loadAuthSession } from '../utils/authSession.js';

export function createHttpClient({ baseURL, useMockAdapter = false }) {
  const client = axios.create({
    baseURL,
    timeout: 5000
  });

  if (useMockAdapter && isLocalMockEnabled) {
    client.defaults.adapter = mockAdapter;
  }

  client.interceptors.request.use(attachAuthToken);
  client.interceptors.response.use((response) => response, clearSessionOnUnauthorized);
  return client;
}

export const http = createHttpClient({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  useMockAdapter: true
});
```

- [ ] **Step 2: Create a business client for registration pages**

```javascript
import { createHttpClient } from './http.js';

export const registrationHttp = createHttpClient({
  baseURL: import.meta.env.VITE_REGISTRATION_API_BASE_URL || '/registration-api'
});
```

- [ ] **Step 3: Create a lightweight AI client helper for registration SSE**

```javascript
import { loadAuthSession } from '../utils/authSession.js';

export function buildRegistrationAiUrl() {
  return `${import.meta.env.VITE_REGISTRATION_AI_BASE_URL || '/registration-ai'}/ai/registration/stream`;
}

export function buildRegistrationAiHeaders() {
  const storage = typeof window !== 'undefined' ? window.localStorage : null;
  const user = storage ? loadAuthSession(storage) : null;

  return {
    'Content-Type': 'application/json',
    'X-Doctor-Id': String(user?.doctorId || '')
  };
}
```

- [ ] **Step 4: Point `registration.js` at the new registration business client**

```javascript
import { registrationHttp } from './registrationHttp.js';

export function syncPatient(payload) {
  return registrationHttp.post('/registration/patient/sync', payload);
}

export function getRegistrationDepartments() {
  return registrationHttp.get('/registration/departments');
}

export function getRegistrationDoctors(params = {}) {
  return registrationHttp.get('/registration/doctors', { params });
}
```

- [ ] **Step 5: Run targeted tests to verify the new routing contract passes**

Run:

```powershell
npm test -- --run tests/unit/registrationBackendRouting.test.js
```

Expected:

```text
PASS tests/unit/registrationBackendRouting.test.js
```

- [ ] **Step 6: Commit the transport split**

```powershell
git add frontend/src/api/http.js frontend/src/api/registrationHttp.js frontend/src/api/registrationAiHttp.js frontend/src/api/registration.js frontend/vite.config.js
git commit -m "refactor: split registration frontend transport"
```

### Task 3: Point registration views to the new backend and AI stream

**Files:**
- Modify: `frontend/src/views/registration/OfflineRegistration.vue`
- Modify: `frontend/src/views/registration/OnlineRegistrationConfirm.vue`
- Modify: `frontend/src/views/registration/RegistrationDashboard.vue`
- Modify: `frontend/src/views/registration/FeeManagement.vue`
- Modify: `frontend/src/views/registration/FeeManagementDetail.vue`
- Modify: `frontend/src/views/registration/RefundManagement.vue`
- Test: `frontend/tests/unit/registrationAiStreamContract.test.js`

- [ ] **Step 1: Replace hard-coded AI stream URL construction in `OfflineRegistration.vue`**

```javascript
import { buildRegistrationAiHeaders, buildRegistrationAiUrl } from '../../api/registrationAiHttp';

async function fetchRegistrationAiStream(params) {
  return fetch(buildRegistrationAiUrl(), {
    method: 'POST',
    headers: buildRegistrationAiHeaders(),
    body: JSON.stringify(params)
  });
}
```

- [ ] **Step 2: Remove direct fallback-to-9600 logic from the registration AI page**

```javascript
const response = await fetchRegistrationAiStream({
  sceneCode,
  patientId: patient.value?.patientId,
  query
});

if (!response.ok || !response.body) {
  throw new Error('AI stream unavailable');
}
```

- [ ] **Step 3: Keep the rest of the registration pages on the same exported API functions**

```javascript
import {
  chargeRegistration,
  confirmOnlineRegistration,
  getOnlinePendingRegistrations
} from '../../api/registration';
```

```javascript
import { getPendingFees } from '../../api/registration';
```

```javascript
import { checkRefund, refundFee } from '../../api/registration';
```

- [ ] **Step 4: Run the AI stream contract test**

Run:

```powershell
npm test -- --run tests/unit/registrationAiStreamContract.test.js
```

Expected:

```text
PASS tests/unit/registrationAiStreamContract.test.js
```

- [ ] **Step 5: Commit the registration view rewiring**

```powershell
git add frontend/src/views/registration/OfflineRegistration.vue frontend/src/views/registration/OnlineRegistrationConfirm.vue frontend/src/views/registration/RegistrationDashboard.vue frontend/src/views/registration/FeeManagement.vue frontend/src/views/registration/FeeManagementDetail.vue frontend/src/views/registration/RefundManagement.vue
git commit -m "feat: wire registration views to new backend"
```

### Task 4: Verify end-to-end runtime behavior with the new registration backend

**Files:**
- Test: `frontend/tests/unit/registrationBackendRouting.test.js`
- Test: `frontend/tests/unit/registrationAiStreamContract.test.js`

- [ ] **Step 1: Run the full frontend unit suite**

Run:

```powershell
npm test -- --run
```

Expected:

```text
12 passed
```

- [ ] **Step 2: Build the frontend**

Run:

```powershell
npm run build
```

Expected:

```text
✓ built
```

- [ ] **Step 3: Start the three backend/frontend services for runtime verification**

Run:

```powershell
cd backend
mvn spring-boot:run
```

Run:

```powershell
cd backend-medical-exam
mvn spring-boot:run
```

Run:

```powershell
cd backend-registration
mvn -pl doctor-auth spring-boot:run
```

Run:

```powershell
cd backend-registration
mvn -pl doctor-modules/module-registration spring-boot:run
```

Run:

```powershell
cd backend-registration
mvn -pl doctor-modules/module-ai-assistant spring-boot:run
```

Run:

```powershell
cd frontend
npm run dev
```

Expected:

```text
backend -> 8080
backend-medical-exam -> 9400
doctor-auth -> 9100
module-registration -> 9200
module-ai-assistant -> 9600
frontend -> 5173
```

- [ ] **Step 4: Smoke-test the registration backend from the frontend-side contracts**

Run:

```powershell
Invoke-RestMethod -Method Get -Uri 'http://127.0.0.1:9200/registration/departments' -TimeoutSec 20 | ConvertTo-Json -Depth 6
```

Expected:

```text
"code": 0
```

Run:

```powershell
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:9600/ai/registration/stream' -ContentType 'application/json; charset=utf-8' -Body '{"sceneCode":"TRIAGE","query":"{}"}' -Headers @{ 'X-Doctor-Id'='1' } -TimeoutSec 20
```

Expected:

```text
SSE stream starts or a non-auth business response is returned
```

- [ ] **Step 5: Commit only if the runtime verification passes**

```powershell
git add frontend
git commit -m "test: verify registration backend integration"
```
