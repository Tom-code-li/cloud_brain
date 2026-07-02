import axios from 'axios';
import { clearAuthSession, loadAuthSession } from '../utils/authSession.js';

const pharmacyHttp = axios.create({
  baseURL: import.meta.env.VITE_PHARMACY_API_BASE_URL || 'http://127.0.0.1:8083/api',
  timeout: 8000
});

function attachAuthToken(config) {
  const storage = typeof window !== 'undefined' ? window.localStorage : null;
  const token = storage ? loadAuthSession(storage)?.token : '';
  if (token && !token.startsWith('demo-token-')) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}

function unwrapResponse(response) {
  const payload = response?.data;
  if (payload && typeof payload === 'object' && 'code' in payload) {
    if (payload.code === 200) {
      return payload.data;
    }
    const error = new Error(payload.message || '请求失败');
    error.response = response;
    return Promise.reject(error);
  }
  return payload;
}

function clearSessionOnUnauthorized(error) {
  if (typeof window !== 'undefined' && error?.response?.status === 401) {
    clearAuthSession(window.localStorage);
  }
  return Promise.reject(error);
}

pharmacyHttp.interceptors.request.use(attachAuthToken);
pharmacyHttp.interceptors.response.use(unwrapResponse, clearSessionOnUnauthorized);

export default pharmacyHttp;
