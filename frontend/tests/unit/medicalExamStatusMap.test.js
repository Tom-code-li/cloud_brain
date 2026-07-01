import { describe, expect, it } from 'vitest';
import { resolveOrderProgressStage, resolveStage } from '../../src/utils/medicalExamStatusMap.js';

describe('medical exam status map', () => {
  it('does not treat a blank report status as a draft report', () => {
    const item = {
      feeStatus: '已支付',
      orderStatus: '执行中',
      reportId: 19,
      reportStatus: ''
    };

    expect(resolveStage(item)).toBe(3);
    expect(resolveOrderProgressStage(item)).toBe(3);
  });

  it('moves to review only when the report is an explicit draft', () => {
    const item = {
      feeStatus: '已支付',
      orderStatus: '执行中',
      reportId: 19,
      reportStatus: '草稿'
    };

    expect(resolveStage(item)).toBe(4);
    expect(resolveOrderProgressStage(item)).toBe(4);
  });
});
