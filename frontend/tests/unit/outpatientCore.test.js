import { describe, expect, it } from 'vitest';
import {
  appendUniqueByCode,
  clone,
  removeBySelectedCodes,
  sumItemPrices
} from '../../src/utils/outpatientCore.js';

describe('outpatientCore', () => {
  it('appends a new code and ignores a duplicate code when appending', () => {
    const first = { code: 'EXAM001', name: '胸片', price: 28 };
    const second = { code: 'EXAM002', name: '心电图', price: 35 };
    const duplicate = { code: 'EXAM001', name: '胸片', price: 28 };

    const appended = appendUniqueByCode([first], second);
    const deduplicated = appendUniqueByCode(appended, duplicate);

    expect(appended).toEqual([first, second]);
    expect(deduplicated).toEqual([first, second]);
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
