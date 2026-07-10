import { useAuthStore as useBaseAuthStore } from './authStore.js';

export function useAuthStore() {
  const store = useBaseAuthStore();

  return {
    get token() {
      return store.state.user?.token || '';
    },
    get doctor() {
      if (!store.state.user) {
        return null;
      }

      return {
        doctorId: store.state.user.doctorId,
        doctorName: store.state.user.name
      };
    },
    get user() {
      return store.state.user || null;
    },
    login: store.login,
    logout: store.logout,
    restore: store.restore,
    state: store.state
  };
}
