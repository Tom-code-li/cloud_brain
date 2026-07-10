# Outpatient Doctor Merge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Merge the provided outpatient-doctor workflow enhancements into the current multi-module Vue 3 project without regressing the unified routing, styling, Axios, mock, registration, exam, lab, or pharmacy flows.

**Architecture:** Keep the current merged project's infrastructure (`router/index.js`, `api/http.js`, multi-role routes, and shared mock shell) intact, then incrementally add the outpatient-doctor business capabilities and direct dependencies. Build the merge from the inside out: first add test tooling, then extend shared logic (`api/outpatient.js`, `stores/patientStore.js`, `mock/adapter.js`, new helper/components), then replace outpatient-facing views, and finally patch only the styles actually needed by those views.

**Tech Stack:** Vue 3, Vite, Element Plus, Vue Router, Axios, local mock adapter, Vitest

---

### Task 1: Add a Minimal Test Harness for Logic-Level TDD

**Files:**
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/package.json`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/vite.config.js`
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/tests/setup/vitest.setup.js`
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/tests/unit/outpatientCore.test.js`

- [ ] **Step 1: Write the failing tests for existing utility behavior**

```js
import { describe, expect, it } from 'vitest';
import {
  appendUniqueByCode,
  clone,
  removeBySelectedCodes,
  sumItemPrices
} from '../../src/utils/outpatientCore.js';

describe('outpatientCore', () => {
  it('deduplicates items by code when appending', () => {
    const first = { code: 'EXAM001', name: '胸片', price: 28 };
    const second = { code: 'EXAM001', name: '胸片', price: 28 };

    const result = appendUniqueByCode([first], second);

    expect(result).toHaveLength(1);
    expect(result[0]).toEqual(first);
  });

  it('removes selected items by code', () => {
    const items = [
      { code: 'LAB001', name: '血常规' },
      { code: 'LAB002', name: 'CRP' }
    ];

    const result = removeBySelectedCodes(items, [{ code: 'LAB002' }]);

    expect(result).toEqual([{ code: 'LAB001', name: '血常规' }]);
  });

  it('sums numeric prices as fixed two-decimal output', () => {
    const result = sumItemPrices([
      { price: 18.5 },
      { price: '26' },
      { price: 0.015 }
    ]);

    expect(result).toBe(44.52);
  });

  it('deep clones nested values', () => {
    const source = { nested: { value: 1 } };

    const result = clone(source);
    result.nested.value = 2;

    expect(source.nested.value).toBe(1);
    expect(result.nested.value).toBe(2);
  });
});
```

- [ ] **Step 2: Run the test command to verify it fails because test tooling is missing**

Run: `npm run test -- --run tests/unit/outpatientCore.test.js`

Expected: FAIL with missing `test` script or missing Vitest dependency

- [ ] **Step 3: Add the minimal Vitest setup**

```json
{
  "name": "neuedu-his-front",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite --host 127.0.0.1",
    "build": "vite build",
    "preview": "vite preview --host 127.0.0.1",
    "test": "vitest"
  },
  "dependencies": {
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.7.2",
    "element-plus": "^2.14.2",
    "vue": "^3.4.29",
    "vue-router": "^4.3.3"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.5",
    "vite": "^5.3.1",
    "vitest": "^2.1.9"
  }
}
```

```js
// vite.config.js
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'node',
    setupFiles: ['./tests/setup/vitest.setup.js']
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:9000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/medical-exam': {
        target: 'http://127.0.0.1:9400',
        changeOrigin: true
      }
    }
  }
});
```

```js
// tests/setup/vitest.setup.js
if (typeof globalThis.structuredClone !== 'function') {
  globalThis.structuredClone = (value) => JSON.parse(JSON.stringify(value));
}
```

- [ ] **Step 4: Install dependencies**

Run: `npm install`

Expected: lockfile updated with `vitest`

- [ ] **Step 5: Run the targeted utility tests to verify they pass**

Run: `npm run test -- --run tests/unit/outpatientCore.test.js`

Expected: PASS with `4 passed`

- [ ] **Step 6: Commit**

```bash
git add package.json package-lock.json vite.config.js tests/setup/vitest.setup.js tests/unit/outpatientCore.test.js
git commit -m "test: add vitest harness for outpatient merge"
```

### Task 2: Add Tests for AI Formatting Helpers Before Creating the Helper Module

**Files:**
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/tests/unit/aiAssistant.test.js`
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/src/utils/aiAssistant.js`

- [ ] **Step 1: Write the failing tests for AI suggestion formatting**

```js
import { describe, expect, it } from 'vitest';
import {
  buildAiAssistantText,
  buildAiPanelSections,
  buildDoctorOpinionText,
  buildExamSuggestionText,
  pickDiagnosisDraft,
  requireAiSuccessData
} from '../../src/utils/aiAssistant.js';

describe('aiAssistant helpers', () => {
  it('prefers the AI diagnosis draft when present', () => {
    expect(pickDiagnosisDraft({ diagnosisDraft: '肺炎' }, '旧值')).toBe('肺炎');
    expect(pickDiagnosisDraft({}, '旧值')).toBe('旧值');
  });

  it('formats structured exam recommendations', () => {
    const text = buildExamSuggestionText({
      examRecommendations: [
        { type: '检查', name: '胸片', reason: '评估肺部感染' }
      ]
    });

    expect(text).toBe('检查：胸片（评估肺部感染）');
  });

  it('builds a readable AI assistant summary', () => {
    const text = buildAiAssistantText({
      diagnosisDraft: '社区获得性肺炎',
      possibleDiagnoses: [{ name: '肺炎', reason: '发热伴咳嗽' }],
      examSuggestions: ['血常规', '胸片'],
      riskFlags: ['青霉素过敏']
    });

    expect(text).toContain('诊断建议：社区获得性肺炎');
    expect(text).toContain('可能诊断：肺炎（发热伴咳嗽）');
    expect(text).toContain('检查检验建议：血常规、胸片');
    expect(text).toContain('风险提示：青霉素过敏');
  });

  it('builds doctor opinion text from plan, drugs, and risks', () => {
    const text = buildDoctorOpinionText({
      planDraft: '建议抗感染治疗',
      drugSuggestions: ['阿莫西林'],
      riskFlags: ['注意药物过敏']
    });

    expect(text).toContain('处理建议：建议抗感染治疗');
    expect(text).toContain('处方建议：阿莫西林');
    expect(text).toContain('风险提示：注意药物过敏');
  });

  it('builds ai panel sections in diagnosis-plan-prescription-risk order', () => {
    const sections = buildAiPanelSections({
      diagnosisDraft: '肺炎',
      planDraft: '抗感染',
      drugSuggestions: ['阿莫西林'],
      riskFlags: ['药物过敏']
    });

    expect(sections.map((item) => item.key)).toEqual(['diagnosis', 'plan', 'prescription', 'risk']);
  });

  it('rejects invalid AI responses', () => {
    expect(() => requireAiSuccessData()).toThrow('AI 接口无响应数据');
    expect(() => requireAiSuccessData({ data: { code: 1, message: 'bad' } })).toThrow('bad');
    expect(() => requireAiSuccessData({ data: { code: 0, data: null } })).toThrow('AI 接口未返回建议内容');
  });
});
```

- [ ] **Step 2: Run the targeted test and verify it fails because the helper module does not exist**

Run: `npm run test -- --run tests/unit/aiAssistant.test.js`

Expected: FAIL with module resolution error for `src/utils/aiAssistant.js`

- [ ] **Step 3: Implement the helper module exactly for the tested behaviors**

```js
function joinList(items = []) {
  return Array.isArray(items) && items.length > 0 ? items.join('、') : '';
}

export function pickDiagnosisDraft(suggestion = {}, fallback = '') {
  return suggestion.diagnosisDraft || fallback;
}

export function buildExamSuggestionText(suggestion = {}) {
  if (Array.isArray(suggestion.examRecommendations) && suggestion.examRecommendations.length > 0) {
    return suggestion.examRecommendations
      .map((item) => `${item.type}：${item.name}（${item.reason}）`)
      .join('\n');
  }
  return joinList(suggestion.examSuggestions);
}

export function buildAiAssistantText(suggestion = {}) {
  const lines = [];

  if (suggestion.diagnosisDraft) {
    lines.push(`诊断建议：${suggestion.diagnosisDraft}`);
  }
  if (Array.isArray(suggestion.possibleDiagnoses) && suggestion.possibleDiagnoses.length > 0) {
    lines.push(`可能诊断：${suggestion.possibleDiagnoses.map((item) => `${item.name}（${item.reason}）`).join('；')}`);
  }
  const examText = buildExamSuggestionText(suggestion);
  if (examText) {
    lines.push(`检查检验建议：${examText}`);
  }
  const riskText = joinList(suggestion.riskFlags);
  if (riskText) {
    lines.push(`风险提示：${riskText}`);
  }
  if (Array.isArray(suggestion.evidence) && suggestion.evidence.length > 0) {
    lines.push(`依据：${suggestion.evidence.join('；')}`);
  }

  return lines.join('\n\n');
}

export function buildDoctorOpinionText(suggestion = {}) {
  const lines = [];

  if (suggestion.planDraft) {
    lines.push(`处理建议：${suggestion.planDraft}`);
  }
  const drugText = joinList(suggestion.drugSuggestions);
  if (drugText) {
    lines.push(`处方建议：${drugText}`);
  }
  const riskText = joinList(suggestion.riskFlags);
  if (riskText) {
    lines.push(`风险提示：${riskText}`);
  }

  return lines.join('\n\n');
}

export function buildPrescriptionSuggestionText(suggestion = {}) {
  const drugText = joinList(suggestion.drugSuggestions);
  return drugText ? `处方建议：${drugText}` : '';
}

export function buildRiskText(suggestion = {}) {
  const riskText = joinList(suggestion.riskFlags);
  return riskText ? `注意事项：${riskText}` : '';
}

export function buildAiPanelSections(suggestion = {}) {
  const sections = [];

  if (suggestion.diagnosisDraft) {
    sections.push({
      key: 'diagnosis',
      title: 'AI 诊断结果',
      content: suggestion.diagnosisDraft
    });
  }
  if (suggestion.planDraft) {
    sections.push({
      key: 'plan',
      title: 'AI 处置建议',
      content: suggestion.planDraft
    });
  }
  if (Array.isArray(suggestion.drugSuggestions) && suggestion.drugSuggestions.length > 0) {
    sections.push({
      key: 'prescription',
      title: 'AI 处方建议',
      content: joinList(suggestion.drugSuggestions)
    });
  }
  if (Array.isArray(suggestion.riskFlags) && suggestion.riskFlags.length > 0) {
    sections.push({
      key: 'risk',
      title: 'AI 注意事项',
      content: joinList(suggestion.riskFlags)
    });
  }

  return sections;
}

export function requireAiSuccessData(response) {
  const payload = response?.data;
  if (!payload) {
    throw new Error('AI 接口无响应数据');
  }
  if (payload.code !== 0) {
    throw new Error(payload.message || 'AI 接口调用失败');
  }
  if (!payload.data) {
    throw new Error('AI 接口未返回建议内容');
  }
  return payload.data;
}
```

- [ ] **Step 4: Run the targeted helper tests to verify they pass**

Run: `npm run test -- --run tests/unit/aiAssistant.test.js`

Expected: PASS with `6 passed`

- [ ] **Step 5: Commit**

```bash
git add src/utils/aiAssistant.js tests/unit/aiAssistant.test.js
git commit -m "feat: add outpatient ai helper utilities"
```

### Task 3: Add Store Tests Before Expanding `patientStore`

**Files:**
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/tests/unit/patientStore.test.js`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/stores/patientStore.js`

- [ ] **Step 1: Write the failing tests for the expanded patient store**

```js
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../../src/api/outpatient.js', () => ({
  fetchPatientContext: vi.fn(async () => ({
    data: {
      data: {
        patientId: '1000013',
        registration: { registrationId: 1, status: '接诊中' },
        visit: { visitId: 'VISIT-1000013', visitStatus: '报告待回阅' },
        medicalRecord: {
          recordId: 'MR-1000013',
          chiefComplaint: '发热',
          presentIllness: '两天',
          currentTreatment: '退烧药',
          pastHistory: '无',
          allergyHistory: '青霉素',
          physicalExam: '双肺呼吸音粗',
          auxiliaryExam: '胸片待回阅',
          diagnosis: '肺炎',
          treatmentAdvice: '完善检查',
          doctorNote: '注意休息',
          finalDiagnosis: '社区获得性肺炎',
          finalOpinion: '抗感染',
          status: '待确诊'
        },
        examOrders: [{ orderNo: 'EX001' }],
        labOrders: [{ orderNo: 'LB001' }],
        prescriptions: [{ prescriptionNo: 'RX001' }],
        feeOrders: [{ feeOrderId: 1 }]
      }
    }
  }))
}));

describe('patientStore', () => {
  beforeEach(() => {
    vi.resetModules();
    sessionStorage.clear();
  });

  it('hydrates registration, visit, record, summaries, and clears stale AI data on selectPatient', async () => {
    const { usePatientStore } = await import('../../src/stores/patientStore.js');
    const store = usePatientStore();

    store.setAiDiagnosisResult('旧诊断');
    store.setAiDoctorOpinion('旧意见');
    await store.selectPatient({ patientId: '1000013', patientNo: '1000013', patientName: '梦琪' });

    expect(store.state.registration).toEqual({ registrationId: 1, status: '接诊中' });
    expect(store.state.visit).toEqual({ visitId: 'VISIT-1000013', visitStatus: '报告待回阅' });
    expect(store.state.medicalRecord.currentTreatment).toBe('退烧药');
    expect(store.state.medicalRecord.finalDiagnosis).toBe('社区获得性肺炎');
    expect(store.state.examOrderSummaries).toEqual([{ orderNo: 'EX001' }]);
    expect(store.state.labOrderSummaries).toEqual([{ orderNo: 'LB001' }]);
    expect(store.state.prescriptionSummaries).toEqual([{ prescriptionNo: 'RX001' }]);
    expect(store.state.feeOrderSummaries).toEqual([{ feeOrderId: 1 }]);
    expect(store.state.aiDiagnosisResult).toBe('');
    expect(store.state.aiDoctorOpinion).toBe('');
  });

  it('persists and reloads request drafts by patient and visit', async () => {
    const { usePatientStore } = await import('../../src/stores/patientStore.js');
    const store = usePatientStore();

    await store.selectPatient({ patientId: '1000013', patientNo: '1000013', patientName: '梦琪' });
    store.saveRequestDraft('exam', { items: [{ code: 'EX001' }], form: { purpose: '排查肺炎' } });

    expect(store.loadRequestDraft('exam')).toEqual({
      items: [{ code: 'EX001' }],
      form: { purpose: '排查肺炎' }
    });

    store.clearRequestDraft('exam');
    expect(store.loadRequestDraft('exam')).toBeNull();
  });
});
```

- [ ] **Step 2: Run the targeted store test and verify it fails because the store is missing the expanded state and methods**

Run: `npm run test -- --run tests/unit/patientStore.test.js`

Expected: FAIL with missing properties such as `registration`, `visit`, `saveRequestDraft`, or `setAiDoctorOpinion`

- [ ] **Step 3: Expand `patientStore` to match the approved data design**

```js
import { reactive, readonly } from 'vue';
import { fetchPatientContext } from '../api/outpatient.js';
import { clone } from '../utils/outpatientCore.js';

const state = reactive({
  activePatient: null,
  registration: null,
  visit: null,
  medicalRecord: null,
  diagnoses: [],
  examOrderSummaries: [],
  labOrderSummaries: [],
  prescriptionSummaries: [],
  feeOrderSummaries: [],
  examItems: [],
  labItems: [],
  examLabReports: [],
  activeExamLabReport: null,
  aiDiagnosisResult: '',
  aiDoctorOpinion: '',
  aiReportSummary: '',
  aiPlanDraft: '',
  aiPrescriptionSuggestions: [],
  aiRiskFlags: []
});

function draftKey(kind, patientId, visitId) {
  return `his-${kind}-draft-${patientId || 'none'}-${visitId || 'none'}`;
}

export function usePatientStore() {
  function clearAiSuggestion() {
    state.aiDiagnosisResult = '';
    state.aiDoctorOpinion = '';
    state.aiReportSummary = '';
    state.aiPlanDraft = '';
    state.aiPrescriptionSuggestions = [];
    state.aiRiskFlags = [];
  }

  async function selectPatient(patient) {
    state.activePatient = clone(patient);
    if (typeof window !== 'undefined') {
      window.sessionStorage.setItem('his-active-patient', JSON.stringify(patient));
    }

    const patientId = patient.patientId || patient.id;
    const res = await fetchPatientContext(patientId);
    const context = res.data.data;

    state.registration = clone(context.registration || null);
    state.visit = clone(context.visit || null);
    state.medicalRecord = {
      recordId: context.medicalRecord?.recordId || null,
      visitId: context.visit?.visitId || context.visitId || null,
      patientId: context.patientId,
      chiefComplaint: context.medicalRecord?.chiefComplaint || '',
      presentIllness: context.medicalRecord?.presentIllness || '',
      currentTreatment: context.medicalRecord?.currentTreatment || '',
      pastHistory: context.medicalRecord?.pastHistory || '',
      allergyHistory: context.medicalRecord?.allergyHistory || '',
      physicalExam: context.medicalRecord?.physicalExam || '',
      auxiliaryExam: context.medicalRecord?.auxiliaryExam || '',
      diagnosisText: context.medicalRecord?.diagnosis || '',
      examSuggestion: context.medicalRecord?.treatmentAdvice || '',
      notes: context.medicalRecord?.doctorNote || '',
      finalDiagnosis: context.medicalRecord?.finalDiagnosis || '',
      finalOpinion: context.medicalRecord?.finalOpinion || '',
      status: context.medicalRecord?.status || '初诊暂存'
    };
    state.diagnoses = [];
    state.examOrderSummaries = clone(context.examOrders || []);
    state.labOrderSummaries = clone(context.labOrders || []);
    state.prescriptionSummaries = clone(context.prescriptions || []);
    state.feeOrderSummaries = clone(context.feeOrders || []);
    state.examItems = [];
    state.labItems = [];
    state.examLabReports = [];
    state.activeExamLabReport = null;
    clearAiSuggestion();
  }

  function setMedicalRecord(record) {
    state.medicalRecord = clone(record);
  }

  function setDiagnoses(items) {
    state.diagnoses = clone(items);
  }

  function setExamItems(items) {
    state.examItems = clone(items);
  }

  function setLabItems(items) {
    state.labItems = clone(items);
  }

  function saveRequestDraft(kind, payload) {
    if (typeof window === 'undefined') return;
    const patientId = state.medicalRecord?.patientId || state.activePatient?.patientId;
    const visitId = state.medicalRecord?.visitId || state.visit?.visitId;
    window.sessionStorage.setItem(draftKey(kind, patientId, visitId), JSON.stringify(payload));
  }

  function loadRequestDraft(kind) {
    if (typeof window === 'undefined') return null;
    const patientId = state.medicalRecord?.patientId || state.activePatient?.patientId;
    const visitId = state.medicalRecord?.visitId || state.visit?.visitId;
    const raw = window.sessionStorage.getItem(draftKey(kind, patientId, visitId));
    return raw ? JSON.parse(raw) : null;
  }

  function clearRequestDraft(kind) {
    if (typeof window === 'undefined') return;
    const patientId = state.medicalRecord?.patientId || state.activePatient?.patientId;
    const visitId = state.medicalRecord?.visitId || state.visit?.visitId;
    window.sessionStorage.removeItem(draftKey(kind, patientId, visitId));
  }

  function setExamLabReports(items) {
    state.examLabReports = clone(items);
  }

  function setActiveExamLabReport(report) {
    state.activeExamLabReport = clone(report);
  }

  function setAiDiagnosisResult(value) {
    state.aiDiagnosisResult = value;
  }

  function setAiDoctorOpinion(value) {
    state.aiDoctorOpinion = value;
  }

  function setAiReportSummary(value) {
    state.aiReportSummary = value;
  }

  function setAiPlanDraft(value) {
    state.aiPlanDraft = value;
  }

  function setAiPrescriptionSuggestions(items) {
    state.aiPrescriptionSuggestions = clone(items || []);
  }

  function setAiRiskFlags(items) {
    state.aiRiskFlags = clone(items || []);
  }

  return {
    state: readonly(state),
    selectPatient,
    clearAiSuggestion,
    setMedicalRecord,
    setDiagnoses,
    setExamItems,
    setLabItems,
    saveRequestDraft,
    loadRequestDraft,
    clearRequestDraft,
    setExamLabReports,
    setActiveExamLabReport,
    setAiDiagnosisResult,
    setAiDoctorOpinion,
    setAiReportSummary,
    setAiPlanDraft,
    setAiPrescriptionSuggestions,
    setAiRiskFlags
  };
}
```

- [ ] **Step 4: Run the targeted store tests to verify they pass**

Run: `npm run test -- --run tests/unit/patientStore.test.js`

Expected: PASS with `2 passed`

- [ ] **Step 5: Commit**

```bash
git add src/stores/patientStore.js tests/unit/patientStore.test.js
git commit -m "feat: expand outpatient patient store state"
```

### Task 4: Add API Tests Before Expanding `api/outpatient.js`

**Files:**
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/tests/unit/outpatientApi.test.js`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/api/outpatient.js`

- [ ] **Step 1: Write the failing API normalization tests**

```js
import { beforeEach, describe, expect, it, vi } from 'vitest';

const http = {
  get: vi.fn(),
  post: vi.fn()
};

vi.mock('../../src/api/http.js', () => ({ http }));

describe('outpatient api', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('sends patient filters including visit status and maps status/registeredAt', async () => {
    http.get.mockResolvedValue({
      data: {
        data: [{
          patientId: '1000013',
          patientNo: '1000013',
          patientName: '梦琪',
          visitStatus: '待接诊',
          registeredAt: '2026-06-29 09:00:00'
        }]
      }
    });

    const { fetchPatients } = await import('../../src/api/outpatient.js');
    const res = await fetchPatients({ medicalNo: '1000013', name: '梦', visitStatus: '待接诊', visitGroup: 'ACTIVE' });

    expect(http.get).toHaveBeenCalledWith('/outpatient/patients', {
      params: {
        patientNo: '1000013',
        patientName: '梦',
        visitStatus: '待接诊',
        visitGroup: 'ACTIVE'
      }
    });
    expect(res.data.list[0]).toMatchObject({
      id: '1000013',
      name: '梦琪',
      status: '待接诊',
      registeredAt: '2026-06-29 09:00:00'
    });
  });

  it('normalizes order and fee list endpoints', async () => {
    http.get
      .mockResolvedValueOnce({ data: { data: [{ orderNo: 'EX001' }] } })
      .mockResolvedValueOnce({ data: { data: [{ feeOrderId: 1 }] } });

    const { fetchExamLabOrders, fetchFeeOrders } = await import('../../src/api/outpatient.js');
    const orders = await fetchExamLabOrders({ visitId: 'VISIT-1000013', orderType: '检查' });
    const fees = await fetchFeeOrders({ visitId: 'VISIT-1000013' });

    expect(orders.data.list).toEqual([{ orderNo: 'EX001' }]);
    expect(fees.data.list).toEqual([{ feeOrderId: 1 }]);
  });

  it('posts enhanced outpatient actions to the expected endpoints', async () => {
    http.post.mockResolvedValue({ data: { code: 200, data: {} } });

    const {
      fetchOutpatientAiSuggestion,
      markExamLabReportReviewed,
      saveFinalDiagnosis,
      skipExam,
      skipLab,
      startEncounter
    } = await import('../../src/api/outpatient.js');

    await startEncounter({ patientId: '1', visitId: 'VISIT-1' });
    await saveFinalDiagnosis({ patientId: '1', visitId: 'VISIT-1' });
    await skipExam({ patientId: '1' });
    await skipLab({ patientId: '1' });
    await markExamLabReportReviewed({ patientId: '1', reportId: 1 });
    await fetchOutpatientAiSuggestion({ sceneCode: 'OUTPATIENT_INITIAL_SUGGESTION' });

    expect(http.post).toHaveBeenNthCalledWith(1, '/outpatient/start-encounter', { patientId: '1', visitId: 'VISIT-1' });
    expect(http.post).toHaveBeenNthCalledWith(2, '/outpatient/final-diagnosis', { patientId: '1', visitId: 'VISIT-1' });
    expect(http.post).toHaveBeenNthCalledWith(3, '/outpatient/skip-exam', { patientId: '1' });
    expect(http.post).toHaveBeenNthCalledWith(4, '/outpatient/skip-lab', { patientId: '1' });
    expect(http.post).toHaveBeenNthCalledWith(5, '/exam-lab-reports/review', { patientId: '1', reportId: 1 });
    expect(http.post).toHaveBeenNthCalledWith(
      6,
      '/ai/outpatient/suggestions',
      { sceneCode: 'OUTPATIENT_INITIAL_SUGGESTION' },
      { timeout: 90000 }
    );
  });
});
```

- [ ] **Step 2: Run the API tests and verify they fail because the exports and mappings do not exist yet**

Run: `npm run test -- --run tests/unit/outpatientApi.test.js`

Expected: FAIL with missing exports or mismatched request payloads

- [ ] **Step 3: Expand `src/api/outpatient.js` to match the tests and approved API contract**

```js
import { http } from './http.js';

function normalizePatient(row) {
  return {
    ...row,
    id: row.patientNo,
    name: row.patientName,
    medicalNo: row.patientNo,
    status: row.visitStatus || row.status,
    registeredAt: row.registeredAt || null
  };
}

function normalizeListResponse(res, normalizeItem = (item) => item) {
  const source = Array.isArray(res.data?.data) ? res.data.data : (res.data?.list || []);
  return {
    ...res,
    data: {
      ...res.data,
      list: source.map(normalizeItem)
    }
  };
}

export async function fetchPatients(params) {
  const res = await http.get('/outpatient/patients', {
    params: {
      patientNo: params?.medicalNo,
      patientName: params?.name,
      visitStatus: params?.visitStatus,
      visitGroup: params?.visitGroup
    }
  });

  return normalizeListResponse(res, normalizePatient);
}

export function fetchPatientContext(patientId) {
  return http.get(`/outpatient/patients/${patientId}/context`);
}

export function fetchDiagnoses(keyword = '') {
  return http.get('/diagnoses', { params: { keyword } });
}

export async function fetchExamItems(keyword = '') {
  const res = await http.get('/exam-items', { params: { keyword } });
  return normalizeListResponse(res);
}

export async function fetchLabItems(keyword = '') {
  const res = await http.get('/lab-items', { params: { keyword } });
  return normalizeListResponse(res);
}

export async function fetchDrugs(keyword = '') {
  const res = await http.get('/drugs', { params: { keyword } });
  return normalizeListResponse(res);
}

export async function fetchExamLabReports(params = {}) {
  const res = await http.get('/exam-lab-reports', { params });
  return normalizeListResponse(res);
}

export function markExamLabReportReviewed(payload) {
  return http.post('/exam-lab-reports/review', payload);
}

export async function fetchExamLabOrders(params = {}) {
  const res = await http.get('/exam-lab-orders', { params });
  return {
    ...res,
    data: {
      ...res.data,
      list: Array.isArray(res.data?.data) ? res.data.data : []
    }
  };
}

export async function fetchFeeOrders(params = {}) {
  const res = await http.get('/fee-orders', { params });
  return {
    ...res,
    data: {
      ...res.data,
      list: Array.isArray(res.data?.data) ? res.data.data : []
    }
  };
}

export function saveMedicalRecord(payload) {
  return http.post('/outpatient/medical-records', payload);
}

export function startEncounter(payload) {
  return http.post('/outpatient/start-encounter', payload);
}

export function saveFinalDiagnosis(payload) {
  return http.post('/outpatient/final-diagnosis', payload);
}

export function skipExam(payload) {
  return http.post('/outpatient/skip-exam', payload);
}

export function skipLab(payload) {
  return http.post('/outpatient/skip-lab', payload);
}

export function submitExamRequest(payload) {
  return http.post('/exam-request', payload);
}

export function submitLabRequest(payload) {
  return http.post('/lab-request', payload);
}

export function submitPrescription(payload) {
  return http.post('/prescriptions', payload);
}

export function fetchOutpatientAiSuggestion(payload) {
  return http.post('/ai/outpatient/suggestions', payload, {
    timeout: 90000
  });
}
```

- [ ] **Step 4: Run the API tests to verify they pass**

Run: `npm run test -- --run tests/unit/outpatientApi.test.js`

Expected: PASS with `3 passed`

- [ ] **Step 5: Commit**

```bash
git add src/api/outpatient.js tests/unit/outpatientApi.test.js
git commit -m "feat: extend outpatient api contract"
```

### Task 5: Add Mock Adapter Tests Before Extending the Outpatient Mock Workflow

**Files:**
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/tests/unit/outpatientMockAdapter.test.js`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/mock/adapter.js`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/mock/data.js`

- [ ] **Step 1: Write the failing mock workflow tests for the new outpatient endpoints**

```js
import { describe, expect, it } from 'vitest';
import { mockAdapter } from '../../src/mock/adapter.js';

async function request(config) {
  return mockAdapter({
    method: 'get',
    url: '/',
    params: {},
    data: undefined,
    ...config
  });
}

describe('outpatient mock adapter', () => {
  it('filters outpatient patients by visit status and visit group', async () => {
    const waiting = await request({
      url: '/outpatient/patients',
      params: { visitStatus: '待接诊' }
    });
    const active = await request({
      url: '/outpatient/patients',
      params: { visitGroup: 'ACTIVE' }
    });

    expect(waiting.data.data.every((item) => item.visitStatus === '待接诊')).toBe(true);
    expect(active.data.data.every((item) => item.visitStatus !== '待接诊')).toBe(true);
  });

  it('starts an encounter and exposes visit context with registration and visit metadata', async () => {
    await request({
      method: 'post',
      url: '/outpatient/start-encounter',
      data: JSON.stringify({ patientId: '1000013', visitId: 'VISIT-1000013' })
    });

    const context = await request({
      url: '/outpatient/patients/1000013/context'
    });

    expect(context.data.data.registration).toBeTruthy();
    expect(context.data.data.visit).toBeTruthy();
    expect(context.data.data.visit.visitStatus).toBe('接诊中');
  });

  it('stores submitted exam orders and returns them by visit', async () => {
    await request({
      method: 'post',
      url: '/exam-request',
      data: JSON.stringify({
        patientId: '1000013',
        visitId: 'VISIT-1000013',
        recordId: 'MR-1000013',
        examItems: [{ code: 'XG001', name: '胸部正位片', spec: '正位', price: 28, feeType: '检查费' }],
        form: { purpose: '排查感染', site: '胸部', notes: '门诊检查' }
      })
    });

    const orders = await request({
      url: '/exam-lab-orders',
      params: { visitId: 'VISIT-1000013', orderType: '检查' }
    });

    expect(orders.data.data).toHaveLength(1);
    expect(orders.data.data[0].items[0].itemName).toBe('胸部正位片');
  });

  it('marks report review and transitions the visit to pending diagnosis', async () => {
    await request({
      method: 'post',
      url: '/exam-lab-reports/review',
      data: JSON.stringify({ patientId: '1000013', visitId: 'VISIT-1000013', reportId: 1 })
    });

    const context = await request({
      url: '/outpatient/patients/1000013/context'
    });

    expect(context.data.data.visit.visitStatus).toBe('待确诊');
  });

  it('returns normalized fee order lists by visit id', async () => {
    const res = await request({
      url: '/fee-orders',
      params: { visitId: 'VISIT-1000013' }
    });

    expect(Array.isArray(res.data.data)).toBe(true);
  });
});
```

- [ ] **Step 2: Run the mock tests and verify they fail because the endpoints and context shape are incomplete**

Run: `npm run test -- --run tests/unit/outpatientMockAdapter.test.js`

Expected: FAIL with missing endpoints or missing `registration` / `visit` / order / fee data

- [ ] **Step 3: Expand `mock/data.js` only if a base data shape is needed for the new workflow**

```js
export const patients = [
  { id: '1000013', name: '梦琪', age: 23, gender: '女', status: '医生接诊', registerTime: '2022-05-18 10:48:18' },
  { id: '1000014', name: '张伟', age: 35, gender: '男', status: '等待就诊', registerTime: '2022-05-18 11:05:22' },
  { id: '1000015', name: '李纳', age: 28, gender: '女', status: '医生接诊', registerTime: '2022-05-18 11:15:41' },
  { id: '1000016', name: '王强', age: 42, gender: '男', status: '等待就诊', registerTime: '2022-05-18 11:25:41' }
];
```

Keep the rest of the file intact unless additional seed fields are required for the adapter logic.

- [ ] **Step 4: Extend `mock/adapter.js` incrementally to support the approved outpatient workflow**

Code requirements to add:

```js
function buildPatientListRow(patient) {
  const activeRegistration = [...registrationState.registrations]
    .reverse()
    .find((item) => String(item.patientId) === String(patient.id) && item.status !== '已退号');

  let status = normalizePatientStatus(patient.status);
  let visitStatus = '待接诊';
  if (activeRegistration?.status === '接诊中') {
    status = '接诊中';
    visitStatus = '接诊中';
  } else if (activeRegistration?.status === '候诊中') {
    status = '等待就诊';
    visitStatus = '待接诊';
  }

  if (activeRegistration?.reviewStatus === '待回阅') {
    visitStatus = '报告待回阅';
  }
  if (activeRegistration?.reviewStatus === '已回阅' || activeRegistration?.status === '待确诊') {
    visitStatus = '待确诊';
  }
  if (activeRegistration?.status === '已完成') {
    visitStatus = '已完成';
  }

  return {
    patientId: patient.id,
    visitId: activeRegistration ? `VISIT-${patient.id}` : null,
    patientNo: patient.id,
    patientName: patient.name,
    idCard: patient.idCard || `11010119900101${String(patient.id).slice(-4)}`,
    age: patient.age,
    gender: patient.gender,
    status,
    visitStatus,
    queueNo: activeRegistration?.queueNo || '',
    registeredAt: activeRegistration?.createdAt || patient.registerTime || formatDateTime(),
    registerTime: activeRegistration?.createdAt || patient.registerTime || formatDateTime()
  };
}
```

```js
function buildPatientContext(patientId) {
  const record = clone(medicalRecords[patientId] || createEmptyMedicalRecord());
  const requests = clone(requestDefaults[patientId] || createEmptyRequests());
  const registration = [...registrationState.registrations].reverse().find((item) => String(item.patientId) === String(patientId));
  const visitStatus = registration?.reviewStatus === '待回阅'
    ? '报告待回阅'
    : registration?.reviewStatus === '已回阅' || registration?.status === '待确诊'
      ? '待确诊'
      : registration?.status === '接诊中'
        ? '接诊中'
        : registration?.status === '已完成'
          ? '已完成'
          : '待接诊';

  return {
    patientId,
    registration: registration ? clone(registration) : null,
    visit: {
      visitId: `VISIT-${patientId}`,
      visitStatus
    },
    medicalRecord: {
      recordId: `MR-${patientId}`,
      chiefComplaint: record.chiefComplaint || '',
      presentIllness: record.presentIllness || '',
      currentTreatment: record.currentTreatment || '',
      pastHistory: record.pastHistory || '',
      allergyHistory: record.allergyHistory || '',
      physicalExam: record.physicalExam || '',
      auxiliaryExam: record.currentTreatment || '',
      diagnosis: record.diagnosisText || '',
      treatmentAdvice: record.examSuggestion || '',
      doctorNote: record.notes || '',
      finalDiagnosis: record.finalDiagnosis || '',
      finalOpinion: record.finalOpinion || '',
      status: record.status || '初诊暂存'
    },
    examOrders: clone(examOrderStore[`VISIT-${patientId}`] || []),
    labOrders: clone(labOrderStore[`VISIT-${patientId}`] || []),
    prescriptions: clone(prescriptionStore[`VISIT-${patientId}`] || []),
    feeOrders: clone(feeOrderStore[`VISIT-${patientId}`] || [])
  };
}
```

```js
if (method === 'get' && url === '/outpatient/patients') {
  const rows = patients
    .map(buildPatientListRow)
    .filter((row) => {
      const matchedNo = !params.patientNo || row.patientNo.includes(params.patientNo);
      const matchedName = !params.patientName || row.patientName.includes(params.patientName);
      const matchedStatus = !params.visitStatus || row.visitStatus === params.visitStatus;
      const matchedGroup = !params.visitGroup || (
        params.visitGroup === 'ACTIVE'
          ? ['接诊中', '报告待回阅', '待确诊'].includes(row.visitStatus)
          : true
      );
      return matchedNo && matchedName && matchedStatus && matchedGroup;
    });
  return createResponse(config, createPayload(clone(rows)));
}
```

```js
if (method === 'post' && url === '/outpatient/start-encounter') {
  const registration = [...registrationState.registrations]
    .reverse()
    .find((item) => String(item.patientId) === String(data.patientId));
  if (!registration) {
    throw createHttpError(config, 404, '挂号记录不存在');
  }
  registration.status = '接诊中';
  registration.reviewStatus = '接诊中';
  const patient = findPatient(data.patientId);
  if (patient) patient.status = '接诊中';
  return createResponse(config, createPayload(clone(registration), '接诊成功'));
}
```

```js
if (method === 'post' && url === '/exam-lab-reports/review') {
  const registration = [...registrationState.registrations]
    .reverse()
    .find((item) => String(item.patientId) === String(data.patientId));
  if (registration) {
    registration.reviewStatus = '已回阅';
    registration.status = '待确诊';
  }
  const report = findReportById(data.reportId);
  if (report) {
    report.status = '已回阅';
  }
  return createResponse(config, createPayload({ reportId: data.reportId }, '报告已回阅'));
}
```

```js
if (method === 'get' && url === '/exam-lab-orders') {
  const visitId = params.visitId;
  const orderType = params.orderType;
  const source = orderType === '检查' ? examOrderStore[visitId] : labOrderStore[visitId];
  return createResponse(config, createPayload(clone(source || [])));
}

if (method === 'get' && url === '/fee-orders') {
  const source = feeOrderStore[params.visitId] || [];
  return createResponse(config, createPayload(clone(source)));
}
```

Also add these supporting behaviors:

- Keep separate `examOrderStore`, `labOrderStore`, and `feeOrderStore`
- When submitting exam/lab requests, create order records and fee-order records for that visit
- When calling `skipExam` / `skipLab`, append a note and preserve visit context
- When saving final diagnosis, write `finalDiagnosis`, `finalOpinion`, and mark registration/visit as complete
- Extend `buildOutpatientSuggestion()` so post-report suggestions can return `diagnosisDraft`, `reportSummary`, `planDraft`, `drugSuggestions`, and `riskFlags`

- [ ] **Step 5: Run the targeted mock tests to verify they pass**

Run: `npm run test -- --run tests/unit/outpatientMockAdapter.test.js`

Expected: PASS with `5 passed`

- [ ] **Step 6: Commit**

```bash
git add src/mock/adapter.js src/mock/data.js tests/unit/outpatientMockAdapter.test.js
git commit -m "feat: extend outpatient mock workflow"
```

### Task 6: Add the New Outpatient Components Before Wiring the Pages

**Files:**
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/src/components/EncounterTabs.vue`
- Create: `C:/Users/李博/OneDrive/桌面/frontend/front/src/components/RequestOrderDetailDialog.vue`

- [ ] **Step 1: Add minimal component smoke tests**

Create `tests/unit/outpatientComponents.test.js`:

```js
import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

describe('outpatient merge components', () => {
  it('contains encounter tab routes for the active visit workflow', () => {
    const source = fs.readFileSync('src/components/EncounterTabs.vue', 'utf8');
    expect(source).toContain('/medical-home');
    expect(source).toContain('/ai-report-detail');
    expect(source).toContain('/prescription-detail');
  });

  it('contains request order detail fields for both exam and lab orders', () => {
    const source = fs.readFileSync('src/components/RequestOrderDetailDialog.vue', 'utf8');
    expect(source).toContain('申请单号');
    expect(source).toContain('标本类型');
    expect(source).toContain('检查部位');
  });
});
```

- [ ] **Step 2: Run the component smoke tests to verify they fail because the files do not exist**

Run: `npm run test -- --run tests/unit/outpatientComponents.test.js`

Expected: FAIL with file read errors

- [ ] **Step 3: Add `EncounterTabs.vue`**

```vue
<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const tabs = [
  { key: 'medical-home', label: '病历首页', route: '/medical-home' },
  { key: 'exam-req', label: '检查申请', route: '/exam-request' },
  { key: 'lab-req', label: '检验申请', route: '/lab-request' },
  { key: 'ai-report-detail', label: '报告回阅', route: '/ai-report-detail' },
  { key: 'diagnosis-detail', label: '门诊确诊', route: '/diagnosis-detail' },
  { key: 'prescription-detail', label: '开设处方', route: '/prescription-detail' }
];

const active = computed(() => route.name);

function go(tab) {
  if (route.path !== tab.route) {
    router.push(tab.route);
  }
}
</script>

<template>
  <div class="encounter-tabs">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="encounter-tab"
      :class="{ active: active === tab.key }"
      @click="go(tab)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>
```

- [ ] **Step 4: Add `RequestOrderDetailDialog.vue`**

```vue
<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    default: '申请详情'
  },
  order: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:modelValue']);

function close() {
  emit('update:modelValue', false);
}
</script>

<template>
  <el-dialog :model-value="modelValue" :title="title" width="860px" @close="close">
    <template v-if="order">
      <div class="request-detail-meta">
        <div><strong>申请单号：</strong>{{ order.orderNo || '-' }}</div>
        <div><strong>状态：</strong>{{ order.status || '-' }}</div>
        <div><strong>金额：</strong>{{ Number(order.totalAmount || 0).toFixed(2) }} 元</div>
        <div><strong>申请时间：</strong>{{ order.appliedAt || '-' }}</div>
      </div>

      <div class="request-detail-grid">
        <div><strong>目的要求：</strong>{{ order.purpose || '无' }}</div>
        <div v-if="order.orderType === '检查'"><strong>检查部位：</strong>{{ order.examSite || '无' }}</div>
        <div v-if="order.orderType === '检验'"><strong>标本类型：</strong>{{ order.specimenType || '无' }}</div>
        <div v-if="order.orderType === '检验'"><strong>优先级：</strong>{{ order.priority || '无' }}</div>
        <div v-if="order.orderType === '检验'"><strong>采样方式：</strong>{{ order.collectionWay || '无' }}</div>
        <div><strong>备注：</strong>{{ order.remark || '无' }}</div>
      </div>

      <el-table :data="order.items || []" border stripe class="dialog-table request-detail-table">
        <el-table-column prop="itemName" label="项目名称" min-width="180" />
        <el-table-column prop="itemType" label="类型" width="90" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">{{ Number(row.unitPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">{{ Number(row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="resultSummary" label="结果摘要" min-width="220" />
      </el-table>
    </template>

    <template #footer>
      <el-button @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>
```

- [ ] **Step 5: Run the smoke tests to verify they pass**

Run: `npm run test -- --run tests/unit/outpatientComponents.test.js`

Expected: PASS with `2 passed`

- [ ] **Step 6: Commit**

```bash
git add src/components/EncounterTabs.vue src/components/RequestOrderDetailDialog.vue tests/unit/outpatientComponents.test.js
git commit -m "feat: add outpatient workflow components"
```

### Task 7: Replace the Outpatient Views with the Approved Workflow

**Files:**
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/PatientView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/MedicalHomeView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/ExamRequestView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/LabRequestView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/AiReportView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/AiReportDetailView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/DiagnosisView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/DiagnosisDetailView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/PrescriptionDetailView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/FeeQueryView.vue`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/views/ConsultRecordView.vue`

- [ ] **Step 1: Add view-level smoke tests for the new workflow hooks before editing the views**

Create `tests/unit/outpatientViewContracts.test.js`:

```js
import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

function read(path) {
  return fs.readFileSync(path, 'utf8');
}

describe('outpatient view contracts', () => {
  it('patient view starts an encounter before navigating', () => {
    const source = read('src/views/PatientView.vue');
    expect(source).toContain('startEncounter');
    expect(source).toContain("visitStatus: '待接诊'");
  });

  it('medical home uses encounter tabs and ai helper utilities', () => {
    const source = read('src/views/MedicalHomeView.vue');
    expect(source).toContain('EncounterTabs');
    expect(source).toContain('buildAiAssistantText');
    expect(source).toContain('requireAiSuccessData');
  });

  it('exam request supports skip logic and submitted order details', () => {
    const source = read('src/views/ExamRequestView.vue');
    expect(source).toContain('skipExam');
    expect(source).toContain('RequestOrderDetailDialog');
    expect(source).toContain("loadRequestDraft('exam')");
  });

  it('lab request supports skip logic and submitted order details', () => {
    const source = read('src/views/LabRequestView.vue');
    expect(source).toContain('skipLab');
    expect(source).toContain('RequestOrderDetailDialog');
    expect(source).toContain("loadRequestDraft('lab')");
  });

  it('report detail marks reviewed before diagnosis routing', () => {
    const source = read('src/views/AiReportDetailView.vue');
    expect(source).toContain('markExamLabReportReviewed');
    expect(source).toContain('buildAiPanelSections');
    expect(source).toContain("router.push('/diagnosis-detail')");
  });

  it('diagnosis detail saves final diagnosis', () => {
    const source = read('src/views/DiagnosisDetailView.vue');
    expect(source).toContain('saveFinalDiagnosis');
    expect(source).toContain('finalDiagnosis');
  });

  it('prescription detail submits dosage frequency and usage method', () => {
    const source = read('src/views/PrescriptionDetailView.vue');
    expect(source).toContain('dosage');
    expect(source).toContain('frequency');
    expect(source).toContain('usageMethod');
    expect(source).toContain('days');
  });

  it('fee query reads live fee orders for the current visit', () => {
    const source = read('src/views/FeeQueryView.vue');
    expect(source).toContain('fetchFeeOrders');
    expect(source).toContain('visitId');
  });
});
```

- [ ] **Step 2: Run the smoke tests and verify they fail against the old outpatient pages**

Run: `npm run test -- --run tests/unit/outpatientViewContracts.test.js`

Expected: FAIL on missing workflow hooks such as `startEncounter`, `skipExam`, `markExamLabReportReviewed`, or `saveFinalDiagnosis`

- [ ] **Step 3: Replace `PatientView.vue` with the enhanced encounter-entry version**

Use the provided source behavior from `C:/Users/李博/OneDrive/桌面/门诊医生前端/frontend/src/views/PatientView.vue`, including:

- import `ElMessage`
- call `fetchPatients({ ..., visitStatus: '待接诊' })`
- call `startEncounter({ patientId, visitId })`
- show the waiting-count tag
- remove the old separate “查看” action and keep the direct “接诊” flow

- [ ] **Step 4: Replace `MedicalHomeView.vue` with the enhanced AI-assisted version**

Use the provided source behavior, including:

- `EncounterTabs`
- `buildAiAssistantText`
- `buildExamSuggestionText`
- `pickDiagnosisDraft`
- `requireAiSuccessData`
- updating `record.examSuggestion` and `record.diagnosisText` after AI generation

- [ ] **Step 5: Replace `ExamRequestView.vue` with the draftable, reviewable, skippable request flow**

Use the provided source behavior, including:

- `EncounterTabs`
- `RequestOrderDetailDialog`
- `fetchExamLabOrders`
- `skipExam`
- saving and clearing exam drafts
- the “已提交申请记录” card

- [ ] **Step 6: Replace `LabRequestView.vue` with the draftable, reviewable, skippable request flow**

Use the provided source behavior, including:

- `EncounterTabs`
- `RequestOrderDetailDialog`
- `fetchExamLabOrders`
- `skipLab`
- saving and clearing lab drafts

- [ ] **Step 7: Replace `AiReportView.vue` and `AiReportDetailView.vue` with the report-review workflow**

Use the provided source behavior, including:

- `visitStatus: '报告待回阅'`
- structured report panels
- AI suggestion generation
- `markExamLabReportReviewed`
- storing AI results in `patientStore`

- [ ] **Step 8: Replace `DiagnosisView.vue` and `DiagnosisDetailView.vue` with the final-diagnosis workflow**

Use the provided source behavior, including:

- `visitStatus: '待确诊'`
- reuse of AI diagnosis/opinion
- `saveFinalDiagnosis`
- store updates for `finalDiagnosis`, `finalOpinion`, and completed status

- [ ] **Step 9: Replace `PrescriptionDetailView.vue`, `FeeQueryView.vue`, and `ConsultRecordView.vue` with the provided behavior**

Required changes:

- `PrescriptionDetailView.vue`: add dosage/frequency/usageMethod/days fields
- `FeeQueryView.vue`: call `fetchFeeOrders` using the current visit id
- `ConsultRecordView.vue`: use `visitGroup: 'ACTIVE'` and present `visitStatus` / `registeredAt`

- [ ] **Step 10: Run the view contract smoke tests to verify they pass**

Run: `npm run test -- --run tests/unit/outpatientViewContracts.test.js`

Expected: PASS with `8 passed`

- [ ] **Step 11: Commit**

```bash
git add src/views/PatientView.vue src/views/MedicalHomeView.vue src/views/ExamRequestView.vue src/views/LabRequestView.vue src/views/AiReportView.vue src/views/AiReportDetailView.vue src/views/DiagnosisView.vue src/views/DiagnosisDetailView.vue src/views/PrescriptionDetailView.vue src/views/FeeQueryView.vue src/views/ConsultRecordView.vue tests/unit/outpatientViewContracts.test.js
git commit -m "feat: merge outpatient doctor workflow views"
```

### Task 8: Patch Styles and Route Imports Needed by the New Outpatient Flow

**Files:**
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/styles/global.css`
- Modify: `C:/Users/李博/OneDrive/桌面/frontend/front/src/router/routes.js`

- [ ] **Step 1: Write a style/route smoke test before editing**

Create `tests/unit/outpatientIntegrationSmoke.test.js`:

```js
import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

describe('outpatient integration smoke', () => {
  it('routes file still exposes outpatient detail routes and multi-role routes', () => {
    const source = fs.readFileSync('src/router/routes.js', 'utf8');
    expect(source).toContain("path: 'ai-report-detail'");
    expect(source).toContain("path: 'registration/dashboard'");
    expect(source).toContain("path: 'pharmacy/dispatch'");
  });

  it('global styles include encounter tabs and request detail styles needed by the merged views', () => {
    const source = fs.readFileSync('src/styles/global.css', 'utf8');
    expect(source).toContain('.encounter-tabs');
    expect(source).toContain('.request-workspace-grid');
    expect(source).toContain('.ai-report-page');
    expect(source).toContain('.request-detail-meta');
  });
});
```

- [ ] **Step 2: Run the smoke test and verify it fails on missing style hooks**

Run: `npm run test -- --run tests/unit/outpatientIntegrationSmoke.test.js`

Expected: FAIL because the current styles do not yet include all needed outpatient merge selectors

- [ ] **Step 3: Patch `src/styles/global.css` by adding only the selectors required by the new outpatient views**

Add the provided outpatient-specific selectors if missing:

```css
.encounter-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin: 0 6px 10px; padding: 8px 10px; background: #fff; border: 1px solid #dce7f5; border-radius: 2px; }
.encounter-tab { border: 1px solid #d8e6f7; background: #f8fbff; color: #5c7187; padding: 6px 12px; font-size: 12px; cursor: pointer; border-radius: 999px; }
.encounter-tab.active { background: #eaf4ff; border-color: #bcdcff; color: #2563eb; font-weight: 700; }
.ai-report-page { display: flex; flex-direction: column; gap: 8px; min-height: calc(100vh - 96px); }
.request-workspace-grid { display: grid; grid-template-columns: minmax(0, 1.25fr) minmax(340px, 0.95fr); gap: 10px; align-items: stretch; }
.request-records-panel { grid-column: 1 / -1; }
.request-detail-meta { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-bottom: 12px; color: #475569; font-size: 12px; }
.request-detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-bottom: 12px; color: #64748b; font-size: 12px; }
.request-detail-table { margin-top: 8px; }
```

Do not replace the entire file. Append or merge the needed blocks while preserving the existing multi-module styling already present in the merged project.

- [ ] **Step 4: Patch `src/router/routes.js` only if imports or route names need alignment**

The file should still keep:

```js
{ path: 'patients', name: 'patient-view', component: PatientView, meta: { menuKey: 'patient-view', role: 'outpatient' } }
{ path: 'medical-home', name: 'medical-home', component: MedicalHomeView, meta: { menuKey: 'medical-home', role: 'outpatient' } }
{ path: 'exam-request', name: 'exam-req', component: ExamRequestView, meta: { menuKey: 'exam-req', role: 'outpatient' } }
{ path: 'lab-request', name: 'lab-req', component: LabRequestView, meta: { menuKey: 'lab-req', role: 'outpatient' } }
{ path: 'ai-report-detail', name: 'ai-report-detail', component: AiReportDetailView, meta: { menuKey: 'ai-report', role: 'outpatient' } }
{ path: 'diagnosis-detail', name: 'diagnosis-detail', component: DiagnosisDetailView, meta: { menuKey: 'diagnosis', role: 'outpatient' } }
{ path: 'prescription-detail', name: 'prescription-detail', component: PrescriptionDetailView, meta: { menuKey: 'prescription', role: 'outpatient' } }
```

Only adjust if route names and `EncounterTabs.vue` keys do not match.

- [ ] **Step 5: Run the integration smoke tests to verify they pass**

Run: `npm run test -- --run tests/unit/outpatientIntegrationSmoke.test.js`

Expected: PASS with `2 passed`

- [ ] **Step 6: Commit**

```bash
git add src/styles/global.css src/router/routes.js tests/unit/outpatientIntegrationSmoke.test.js
git commit -m "style: patch outpatient workflow styling hooks"
```

### Task 9: Run the Full Verification Suite and Build Before Claiming Completion

**Files:**
- Modify: none expected
- Verify: `C:/Users/李博/OneDrive/桌面/frontend/front`

- [ ] **Step 1: Run the full unit test suite**

Run: `npm run test -- --run`

Expected: PASS with all outpatient merge tests green

- [ ] **Step 2: Run the production build**

Run: `npm run build`

Expected: PASS with Vite build output and exit code 0

- [ ] **Step 3: Spot-check the approved requirements against the spec**

Checklist:

- [ ] Patient view starts encounters and filters `待接诊`
- [ ] Medical home uses AI helper-driven suggestion text
- [ ] Exam request supports draft / skip / submitted records
- [ ] Lab request supports draft / skip / submitted records
- [ ] Report review supports AI suggestion and reviewed status transition
- [ ] Diagnosis detail saves final diagnosis and final opinion
- [ ] Prescription detail submits enhanced drug instructions
- [ ] Fee query reads live visit fee orders
- [ ] Registration / pharmacy / exam / lab role routes remain present in `routes.js`

- [ ] **Step 4: Commit if verification changed any generated files**

```bash
git status --short
```

Expected: no unexpected modified files

- [ ] **Step 5: Final completion note**

When reporting completion, include:

- the spec path
- the plan path
- test command result
- build command result
- any residual risk, especially around the large shared `mock/adapter.js`
