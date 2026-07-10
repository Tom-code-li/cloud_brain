import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/authStore.js';

const http = axios.create({ baseURL: '/medical-exam' });

function resolveIdentity() {
  const user = useAuthStore().state.user;
  const moduleKey = user?.moduleKey;
  const doctorType = user?.doctorType || (moduleKey === 'lab' ? 'LAB' : moduleKey === 'exam' ? 'EXAM' : '');

  return {
    doctorId: user?.doctorId,
    deptId: user?.deptId,
    doctorType
  };
}

http.interceptors.request.use((config) => {
  const identity = resolveIdentity();
  if (!identity.doctorId || !identity.doctorType) {
    ElMessage.error('缺少医生身份信息，请重新登录');
    return Promise.reject(new Error('missing medical exam identity'));
  }

  config.headers['X-Doctor-Id'] = identity.doctorId;
  if (identity.deptId) {
    config.headers['X-Dept-Id'] = identity.deptId;
  }
  config.headers['X-Doctor-Type'] = identity.doctorType;
  return config;
});

http.interceptors.response.use(
  (response) => {
    if (response.data?.code !== 0) {
      ElMessage.error(response.data?.message || '业务错误');
      return Promise.reject(new Error(response.data?.message || 'business error'));
    }
    return response.data.data;
  },
  (error) => {
    ElMessage.error(error?.response?.data?.message || error.message || '网络错误');
    return Promise.reject(error);
  }
);

export default http;
