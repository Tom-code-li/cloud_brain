const SESSION_KEY = 'his-doctor-auth';

const DEMO_ACCOUNT = {
  account: 'DOC2025001',
  password: '123456',
  name: '张仲景',
  role: '门诊医生'
};

export function authenticateDoctor(account, password) {
  if (account === DEMO_ACCOUNT.account && password === DEMO_ACCOUNT.password) {
    return {
      account: DEMO_ACCOUNT.account,
      name: DEMO_ACCOUNT.name,
      role: DEMO_ACCOUNT.role
    };
  }
  return null;
}

export function saveAuthSession(storage, session) {
  storage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function loadAuthSession(storage) {
  const raw = storage.getItem(SESSION_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function clearAuthSession(storage) {
  storage.removeItem(SESSION_KEY);
}

export function getSessionKey() {
  return SESSION_KEY;
}
