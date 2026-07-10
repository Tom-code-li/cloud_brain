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
