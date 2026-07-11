import { createRouter, createWebHistory, createMemoryHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', requiresAuth: true }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/admin/Department.vue'),
        meta: { title: '科室管理', requiresAuth: true }
      },
      {
        path: 'doctor',
        name: 'Doctor',
        component: () => import('@/views/admin/Doctor.vue'),
        meta: { title: '医生管理', requiresAuth: true }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/admin/Role.vue'),
        meta: { title: '角色管理', requiresAuth: true }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/admin/Schedule.vue'),
        meta: { title: '排班管理', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createAppHistory(),
  routes
})

export function createAppHistory(mode = import.meta.env.MODE) {
  return mode === 'test' ? createMemoryHistory() : createWebHistory()
}

function hasToken() {
  try {
    return !!localStorage.getItem('admin_token')
  } catch {
    return false
  }
}

router.beforeEach((to, from, next) => {
  const title = to.meta?.title
  document.title = (title ? title + ' - ' : '') + '医院综合管理平台'

  if (to.meta?.requiresAuth && !hasToken()) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  if (to.name === 'Login' && hasToken()) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
