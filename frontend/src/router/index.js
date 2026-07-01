import { createRouter, createWebHistory } from 'vue-router';
import { routes } from './routes.js';
import { resolveRoleModule, resolveWorkbenchRoute } from '../config/menu.js';
import { useAuthStore } from '../stores/authStore.js';

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const authStore = useAuthStore();

  if (!authStore.state.user) {
    authStore.restore();
  }

  const user = authStore.state.user;

  if (to.meta.public) {
    if (user && to.path === '/login') {
      return { path: resolveWorkbenchRoute(user), replace: true };
    }
    return true;
  }

  if (!user) {
    return {
      path: '/login',
      query: to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : {}
    };
  }

  if (to.meta.role && to.meta.role !== resolveRoleModule(user)) {
    return { path: resolveWorkbenchRoute(user), replace: true };
  }

  return true;
});

export default router;
