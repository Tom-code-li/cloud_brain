<template>
  <div class="app-layout">
    <!-- ========== 顶部蓝色导航条 ========== -->
    <header class="top-bar">
      <div class="top-bar-left">
        <div class="top-logo">
          <span class="top-logo-acronym">HIS</span>
        </div>
        <div class="top-logo-text">
          <div class="top-title">东软云医院 HIS 系统</div>
          <div class="top-subtitle">统一工作台</div>
        </div>
      </div>

      <div class="top-bar-right">
        <div class="info-group">
          <div class="info-item">
            <el-icon class="info-icon"><UserFilled /></el-icon>
            <span>{{ userStore.roleName || '管理员' }}</span>
          </div>
          <div class="info-sep"></div>
          <div class="info-item">
            <span>{{ userStore.realName || userStore.username || '-' }}</span>
          </div>
          <div class="info-sep"></div>
          <div class="info-item">
            <span>管理工作台</span>
          </div>
        </div>
        <span class="logout-btn" @click="handleLogout">
          <el-icon class="logout-icon"><SwitchButton /></el-icon>
          <span>退出登录</span>
        </span>
      </div>
    </header>

    <!-- ========== 下方内容区 ========== -->
    <main class="content-wrapper">
      <!-- 左侧菜单卡片 -->
      <aside class="menu-card">
        <div class="menu-card-title">
          <span class="title-dot"></span>
          <span>管理功能</span>
        </div>
        <div class="menu-list">
          <div
            v-for="item in menuList"
            :key="item.path"
            class="menu-item"
            :class="{ active: activeMenu === item.path }"
            @click="goto(item.path)"
          >
            <el-icon class="menu-item-icon"><component :is="item.icon" /></el-icon>
            <span class="menu-item-text">{{ item.title }}</span>
          </div>
        </div>
      </aside>

      <!-- 右侧内容卡片 -->
      <section class="content-card">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  OfficeBuilding,
  UserFilled,
  Lock,
  Calendar,
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menuList = computed(() => {
  const full = [
    { path: '/dashboard', title: '工作台', icon: HomeFilled },
    { path: '/department', title: '科室管理', icon: OfficeBuilding },
    { path: '/doctor', title: '医生管理', icon: UserFilled },
    { path: '/schedule', title: '排班管理', icon: Calendar },
    { path: '/role', title: '角色管理', icon: Lock }
  ]
  return full
})

const activeMenu = computed(() => route.path)

function goto(path) {
  if (path !== activeMenu.value) router.push(path)
}

function handleLogout() {
  ElMessageBox.confirm('确认退出当前登录账号？', '退出登录', {
    confirmButtonText: '确认退出',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  })
    .then(() => {
      userStore.logout()
      router.replace({ name: 'Login' })
    })
    .catch(() => {})
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #e8efff 0%, #f5f7fa 30%);
}

/* ========== 顶部蓝色导航条 ========== */
.top-bar {
  background: linear-gradient(135deg, #1e4fd6 0%, #2e6fe8 100%);
  padding: 10px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(30, 79, 214, 0.2);
  flex-shrink: 0;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.top-logo {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.top-logo-acronym {
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
}

.top-logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.top-title {
  font-size: 17px;
  font-weight: 600;
  line-height: 1.2;
}

.top-subtitle {
  font-size: 12px;
  opacity: 0.9;
  line-height: 1.2;
}

/* 右侧标签组 */
.top-bar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-group {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 10px;
  padding: 0 6px;
  height: 34px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  font-size: 13px;
  color: #ffffff;
  font-weight: 500;
  white-space: nowrap;
}

.info-icon {
  font-size: 13px;
}

.info-sep {
  width: 1px;
  height: 14px;
  background: rgba(255, 255, 255, 0.25);
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  padding: 0 14px;
  height: 34px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
  background: #e74c3c;
  border: 1px solid #c0392b;
  color: #ffffff;
  white-space: nowrap;
}

.logout-btn:hover {
  background: #c0392b;
  border-color: #a5281b;
}

.logout-icon {
  font-size: 13px;
}

/* ========== 下方内容区 ========== */
.content-wrapper {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px 20px 20px;
  min-height: 0;
  overflow: hidden;
}

/* 左侧菜单卡片 */
.menu-card {
  width: 200px;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 16px;
  padding: 16px 12px;
  box-shadow: 0 2px 12px rgba(30, 79, 214, 0.08);
  border: 1px solid rgba(30, 79, 214, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.menu-card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  padding: 4px 10px 14px;
}

.title-dot {
  width: 6px;
  height: 6px;
  background: #1e4fd6;
  border-radius: 50%;
  flex-shrink: 0;
}

.menu-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.18s;
  font-weight: 500;
}

.menu-item-icon {
  font-size: 15px;
  color: #606266;
  flex-shrink: 0;
}

.menu-item:hover {
  background: rgba(30, 79, 214, 0.06);
  color: #1e4fd6;
}

.menu-item:hover .menu-item-icon {
  color: #1e4fd6;
}

.menu-item.active {
  background: linear-gradient(135deg, rgba(30, 79, 214, 0.12), rgba(46, 111, 232, 0.06));
  color: #1e4fd6;
  font-weight: 600;
}

.menu-item.active .menu-item-icon {
  color: #1e4fd6;
}

/* 右侧内容卡片 */
.content-card {
  flex: 1;
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(30, 79, 214, 0.06);
  border: 1px solid rgba(30, 79, 214, 0.06);
  overflow-y: auto;
  min-width: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>