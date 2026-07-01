import { beforeEach, describe, expect, it, vi } from 'vitest';

describe('role routing', () => {
  beforeEach(() => {
    vi.resetModules();
  });

  it('prefers doctorType when resolving module and falls back to a valid workbench route', async () => {
    const { resolveRoleModule, resolveWorkbenchRoute } = await import('../../src/config/menu.js');

    expect(resolveRoleModule({ doctorType: 'LAB', roleCode: 'OUTPATIENT_DOCTOR' })).toBe('lab');
    expect(resolveWorkbenchRoute({ doctorType: 'EXAM', workbenchRoute: '/exam-workbench' })).toBe('/exam');
    expect(resolveWorkbenchRoute({ doctorType: 'PHARMACY', workbenchRoute: '/pharmacy/dispatch' })).toBe('/pharmacy/dispatch');
  });
});

describe('authStore login normalization', () => {
  beforeEach(() => {
    vi.resetModules();

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
  });

  it('keeps backend doctor identity for non-mock logins and routes by doctorType', async () => {
    vi.doMock('../../src/api/http.js', () => ({
      http: {
        post: vi.fn(async () => ({
          data: {
            code: 0,
            message: 'success',
            data: {
              token: 'real-backend-token',
              roleCode: 'OUTPATIENT_DOCTOR',
              roleName: '门诊医生',
              workbenchRoute: '/exam-workbench',
              user: {
                userId: 3001,
                username: 'EXAM2025001',
                realName: '钱检查',
                doctorId: 3,
                deptId: 9,
                doctorType: 'EXAM'
              }
            }
          }
        }))
      }
    }));

    vi.doMock('../../src/mock/adapter.js', () => ({
      isLocalMockEnabled: false
    }));

    const { useAuthStore } = await import('../../src/stores/authStore.js');
    const store = useAuthStore();

    const user = await store.login('EXAM2025001', '123456');

    expect(user.doctorId).toBe(3);
    expect(user.deptId).toBe(9);
    expect(user.name).toBe('钱检查');
    expect(user.moduleKey).toBe('exam');
    expect(user.workbenchRoute).toBe('/exam');
  });
});
