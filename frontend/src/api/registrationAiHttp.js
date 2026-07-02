import { loadAuthSession } from '../utils/authSession.js';

export function buildRegistrationAiUrl() {
  return `${import.meta.env.VITE_REGISTRATION_AI_BASE_URL || '/registration-ai'}/ai/registration/stream`;
}

export function buildRegistrationAiHeaders() {
  const storage = typeof window !== 'undefined' ? window.localStorage : null;
  const user = storage ? loadAuthSession(storage) : null;
  const headers = {
    'Content-Type': 'application/json',
    'X-Doctor-Id': String(user?.doctorId || '')
  };

  if (user?.token && !String(user.token).startsWith('demo-token-')) {
    headers.Authorization = `Bearer ${user.token}`;
  }

  return headers;
}
