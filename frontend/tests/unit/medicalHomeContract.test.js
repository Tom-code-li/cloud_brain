import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

function read(path) {
  return fs.readFileSync(path, 'utf8');
}

describe('medical home contract', () => {
  it('keeps auxiliary exam separate from current treatment when saving records', () => {
    const source = read('src/views/MedicalHomeView.vue');

    expect(source).toContain('auxiliaryExam: record.auxiliaryExam ||');
    expect(source).not.toContain('auxiliaryExam: record.currentTreatment ||');
  });

  it('loads prescription candidates from the pending disposal queue', () => {
    const source = read('src/views/PrescriptionView.vue');

    expect(source).toContain("visitStatus: '待处置'");
  });
});
