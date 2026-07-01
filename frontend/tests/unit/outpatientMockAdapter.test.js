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
