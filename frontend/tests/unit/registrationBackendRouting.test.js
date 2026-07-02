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
    expect(source).toContain("path.replace(/^\\/registration-api/, '')");
    expect(source).toContain("'/registration-ai'");
    expect(source).toContain("target: 'http://127.0.0.1:9600'");
    expect(source).toContain("path.replace(/^\\/registration-ai/, '')");
  });

  it('keeps the existing /api and /medical-exam proxies intact', () => {
    const source = read('vite.config.js');

    expect(source).toContain("'/api'");
    expect(source).toContain("target: 'http://127.0.0.1:8080'");
    expect(source).toContain("'/medical-exam'");
    expect(source).toContain("target: 'http://127.0.0.1:9400'");
  });
});
