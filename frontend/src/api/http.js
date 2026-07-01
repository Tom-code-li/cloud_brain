import axios from 'axios';
import { isLocalMockEnabled, mockAdapter } from '../mock/adapter.js';
import { clearAuthSession, loadAuthSession } from '../utils/authSession.js';

const baseConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 5000
};

export const http = axios.create(baseConfig);

const dataHttp = axios.create(baseConfig);

if (isLocalMockEnabled) {
  http.defaults.adapter = mockAdapter;
  dataHttp.defaults.adapter = mockAdapter;
}

function attachAuthToken(config) {
  const storage = typeof window !== 'undefined' ? window.localStorage : null;
  const token = storage ? loadAuthSession(storage)?.token : '';
  if (token && !token.startsWith('demo-token-')) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}

function clearSessionOnUnauthorized(error) {
  if (typeof window !== 'undefined' && error?.response?.status === 401) {
    clearAuthSession(window.localStorage);
  }
  return Promise.reject(error);
}

http.interceptors.request.use(attachAuthToken);
dataHttp.interceptors.request.use(attachAuthToken);
http.interceptors.response.use((response) => response, clearSessionOnUnauthorized);

dataHttp.interceptors.response.use((response) => {
  if (response?.data && typeof response.data === 'object' && 'data' in response.data) {
    return response.data.data;
  }

  return response.data;
}, clearSessionOnUnauthorized);

export default dataHttp;
