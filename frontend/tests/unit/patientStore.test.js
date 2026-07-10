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

function createSessionStorageMock() {
  const store = new Map();

  return {
    getItem(key) {
      return store.has(key) ? store.get(key) : null;
    },
    setItem(key, value) {
      store.set(key, String(value));
    },
    removeItem(key) {
      store.delete(key);
    },
    clear() {
      store.clear();
    }
  };
}

describe('patientStore', () => {
  beforeEach(() => {
    vi.resetModules();
    const sessionStorageMock = createSessionStorageMock();
    globalThis.sessionStorage = sessionStorageMock;
    globalThis.window = {
      sessionStorage: sessionStorageMock
    };
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
    expect(store.state.medicalRecord.auxiliaryExam).toBe('胸片待回阅');
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

  it('refreshes the selected patient context without clearing ai data when requested', async () => {
    const { usePatientStore } = await import('../../src/stores/patientStore.js');
    const store = usePatientStore();

    await store.selectPatient({ patientId: '1000013', patientNo: '1000013', patientName: '梦琪' });
    store.setAiDiagnosisResult('AI 诊断');
    store.setAiDoctorOpinion('AI 意见');

    await store.refreshSelectedPatient({ preserveAi: true });

    expect(store.state.visit).toEqual({ visitId: 'VISIT-1000013', visitStatus: '报告待回阅' });
    expect(store.state.aiDiagnosisResult).toBe('AI 诊断');
    expect(store.state.aiDoctorOpinion).toBe('AI 意见');
  });
});
