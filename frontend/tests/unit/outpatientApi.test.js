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
