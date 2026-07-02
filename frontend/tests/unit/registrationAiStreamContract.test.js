import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

function read(path) {
  return fs.readFileSync(path, 'utf8');
}

describe('registration AI stream contract', () => {
  it('routes registration AI stream requests to the dedicated registration AI base path', () => {
    const helperSource = read('src/api/registrationAiHttp.js');
    const viewSource = read('src/views/registration/OfflineRegistration.vue');

    expect(helperSource).toContain("'/registration-ai'}/ai/registration/stream");
    expect(viewSource).toContain("from '../../api/registrationAiHttp.js'");
    expect(viewSource).toContain('buildRegistrationAiUrl()');
    expect(viewSource).not.toContain("const baseUrl = direct ? 'http://127.0.0.1:9600' : '/api'");
  });
});
