import { beforeEach, describe, expect, it, vi } from 'vitest';
import fs from 'node:fs';

function read(path) {
  return fs.readFileSync(path, 'utf8');
}

describe('backend connection contract', () => {
  beforeEach(() => {
    vi.resetModules();
  });

  it('routes local api traffic to the Spring backend default port in Vite dev proxy', () => {
    const source = read('vite.config.js');

    expect(source).toContain("target: 'http://127.0.0.1:8080'");
    expect(source).not.toContain("rewrite: (path) => path.replace(/^\\/api/, '')");
  });

  it('surfaces backend business errors during login instead of crashing on an empty payload', async () => {
    vi.doMock('../../src/api/http.js', () => ({
      http: {
        post: vi.fn(async () => ({
          data: {
            code: 401,
            message: '账号或密码错误',
            data: null
          }
        }))
      }
    }));

    vi.doMock('../../src/mock/adapter.js', () => ({
      isLocalMockEnabled: false
    }));

    const localStorageMock = {
      getItem() {
        return null;
      },
      setItem() {},
      removeItem() {}
    };

    globalThis.window = {
      localStorage: localStorageMock
    };

    const { useAuthStore } = await import('../../src/stores/authStore.js');
    const store = useAuthStore();

    await expect(store.login('bad-user', 'bad-pass')).rejects.toThrow('账号或密码错误');
  });
});
