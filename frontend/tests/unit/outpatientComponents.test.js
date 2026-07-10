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
