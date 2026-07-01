import { reactive, readonly } from 'vue';
import { clearAuthSession, loadAuthSession, saveAuthSession } from '../utils/authSession.js';
import { http } from '../api/http.js';
import { getRoleLabel, resolveRoleModule, resolveWorkbenchRoute } from '../config/menu.js';
import { isLocalMockEnabled } from '../mock/adapter.js';

const storage = typeof window !== 'undefined'
  ? window.localStorage
  : {
      getItem() {
        return null;
      },
      setItem() {},
      removeItem() {}
    };

const state = reactive({
  user: normalizeUser(loadAuthSession(storage))
});

function normalizeDoctorType(doctorType) {
  return String(doctorType || '').trim().toUpperCase();
}

function normalizeUser(user) {
  if (!user) {
    return null;
  }

  if (!isLocalMockEnabled && (!user.token || user.token.startsWith('demo-token-'))) {
    clearAuthSession(storage);
    return null;
  }

  const moduleKey = resolveRoleModule(user);
  const normalizedMedicalIdentity = normalizeMedicalExamIdentity(user, moduleKey);

  return {
    ...user,
    doctorType: normalizeDoctorType(user.doctorType),
    ...normalizedMedicalIdentity,
    moduleKey,
    roleName: user.roleName || getRoleLabel({ moduleKey }),
    workbenchRoute: resolveWorkbenchRoute({ ...user, moduleKey })
  };
}

function normalizeMedicalExamIdentity(user, moduleKey) {
  const isDemoLogin = isLocalMockEnabled && String(user.token || '').startsWith('demo-token-');
  if (!isDemoLogin) {
    return {};
  }

  if (moduleKey === 'exam' && Number(user.doctorId) === 3) {
    return {
      name: user.name === '钱检查' ? '于景澄' : user.name,
      doctorId: 11,
      deptId: 11,
      userId: user.userId === 3001 ? 16 : user.userId
    };
  }

  if (moduleKey === 'lab' && Number(user.doctorId) === 4) {
    return {
      name: user.name === '孙检验' ? '韩书瑶' : user.name,
      doctorId: 16,
      deptId: 6,
      userId: user.userId === 4001 ? 21 : user.userId
    };
  }

  return {};
}

export function useAuthStore() {
  async function login(account, password) {
    const res = await http.post('/auth/login', {
      username: account,
      password
    });
    if (res.data?.code !== 0 || !res.data?.data) {
      throw new Error(res.data?.message || '登录失败');
    }
    const payload = res.data.data;
    const loginUser = payload.user || payload;
    const user = normalizeUser({
      token: payload.token,
      roleCode: payload.roleCode,
      roleName: payload.roleName,
      workbenchRoute: payload.workbenchRoute,
      account: loginUser.username || payload.username || account,
      name: loginUser.realName || payload.realName || '',
      doctorId: loginUser.doctorId ?? payload.doctorId ?? null,
      deptId: loginUser.deptId ?? payload.deptId ?? null,
      doctorType: loginUser.doctorType || payload.doctorType || '',
      userId: loginUser.userId ?? payload.userId ?? null
    });
    state.user = user;
    saveAuthSession(storage, user);
    return user;
  }

  function logout() {
    state.user = null;
    clearAuthSession(storage);
  }

  function restore() {
    state.user = normalizeUser(loadAuthSession(storage));
  }

  return {
    state: readonly(state),
    login,
    logout,
    restore
  };
}
