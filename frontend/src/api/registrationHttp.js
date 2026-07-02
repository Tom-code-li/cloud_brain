import { createDataHttpClient } from './http.js';

export const registrationHttp = createDataHttpClient({
  baseURL: import.meta.env.VITE_REGISTRATION_API_BASE_URL || '/registration-api'
});

export default registrationHttp;
