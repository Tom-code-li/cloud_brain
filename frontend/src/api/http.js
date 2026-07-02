import axios from 'axios';
import { isLocalMockEnabled, mockAdapter } from '../mock/adapter.js';
import { clearAuthSession, loadAuthSession } from '../utils/authSession.js';

function attachAuthToken(config) {
  const storage = typeof window !== 'undefined' ? window.localStorage : null;
  const token = storage ? loadAuthSession(storage)?.token : '';
  if (token && !token.startsWith('demo-token-')) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}

export function createHttpClient({ baseURL, useMockAdapter = false, timeout = 5000 }) {
  const client = axios.create({
    baseURL,
    timeout
  });

  if (useMockAdapter && isLocalMockEnabled) {
    client.defaults.adapter = mockAdapter;
  }

  client.interceptors.request.use(attachAuthToken);
  client.interceptors.response.use((response) => response, clearSessionOnUnauthorized);
  return client;
}

export function createDataHttpClient(options) {
  const client = createHttpClient(options);

  client.interceptors.response.use((response) => {
    if (response?.data && typeof response.data === 'object' && 'data' in response.data) {
      return response.data.data;
    }

    return response.data;
  }, clearSessionOnUnauthorized);

  return client;
}

export const http = createHttpClient({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  useMockAdapter: true
});

const dataHttp = createDataHttpClient({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  useMockAdapter: true
});

function clearSessionOnUnauthorized(error) {
  if (typeof window !== 'undefined' && error?.response?.status === 401) {
    clearAuthSession(window.localStorage);
  }
  return Promise.reject(error);
}

export default dataHttp;
