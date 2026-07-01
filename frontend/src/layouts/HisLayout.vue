<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getMenuItemsByRole, getRoleLabel, resolveWorkbenchRoute } from '../config/menu.js';
import { useAuthStore } from '../stores/authStore.js';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const currentUser = computed(() => authStore.state.user);
const currentMenuItems = computed(() => getMenuItemsByRole(currentUser.value || { moduleKey: route.meta.role }));
const activeMenu = computed(() => route.meta.menuKey || currentMenuItems.value[0]?.key || '');
const moduleLabel = computed(() => currentUser.value?.roleName || getRoleLabel(currentUser.value || { moduleKey: route.meta.role }));
const displayName = computed(() => currentUser.value?.name || currentUser.value?.account || '未登录');
const workbenchRoute = computed(() => resolveWorkbenchRoute(currentUser.value || { moduleKey: route.meta.role }));
const quickEntry = computed(() => currentMenuItems.value[0] || null);

function handleMenuSelect(key) {
  const target = currentMenuItems.value.find((item) => item.key === key);
  if (target && target.route !== route.path) {
    router.push(target.route);
  }
}

function goWorkbench() {
  router.push(workbenchRoute.value);
}

function handleLogout() {
  authStore.logout();
  router.push('/login');
}
</script>

<template>
  <el-container class="his-shell">
    <el-header class="his-header">
      <div class="brand">
        <div class="brand-mark">HIS</div>
        <div class="brand-copy">
          <span class="title">东软云医院 HIS 系统</span>
          <span class="brand-subtitle">统一工作台</span>
        </div>
      </div>
      <div class="header-user">
        <div class="user-panel">
          <span class="workbench-badge">{{ moduleLabel }}</span>
          <div class="user-copy">
            <strong>{{ displayName }}</strong>
            <span>{{ currentUser?.account || '统一登录' }}</span>
          </div>
        </div>
        <el-button v-if="quickEntry" plain @click="goWorkbench">{{ quickEntry.label }}</el-button>
        <el-button text @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container class="his-body">
      <el-aside class="his-aside">
        <div class="aside-top">
          <span class="doctor-icon">◆</span>
          <span>{{ moduleLabel }}</span>
        </div>
        <el-menu class="his-menu" :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item v-for="item in currentMenuItems" :key="item.key" :index="item.key">
            {{ item.label }}
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="his-main">
        <div class="page-wrap">
          <div class="page-inner">
            <router-view v-slot="{ Component }">
              <transition name="fade-slide" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>
