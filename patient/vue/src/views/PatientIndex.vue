<template>
  <div class="phone-shell">
    <div class="phone-frame">
      <div class="status-bar">
        <span>9:41</span>
        <span>5G 92%</span>
      </div>
      <div class="page-content">
        <router-view />
      </div>
      <nav class="bottom-nav" aria-label="患者端主导航">
        <button class="nav-item" :class="{ active: curRoute === '/patient/home' }" @click="goTo('/patient/home')">
          <span class="nav-icon">⌂</span>
          <span class="nav-label">首页</span>
        </button>
        <button class="nav-item" :class="{ active: curRoute === '/patient/appointment' }" @click="goTo('/patient/appointment')">
          <span class="nav-icon">＋</span>
          <span class="nav-label">挂号</span>
        </button>
        <button class="nav-item" :class="{ active: curRoute === '/patient/my-appointments' }" @click="goTo('/patient/my-appointments')">
          <span class="nav-icon">≡</span>
          <span class="nav-label">记录</span>
        </button>
        <button class="nav-item" :class="{ active: curRoute === '/patient/fees' }" @click="goTo('/patient/fees')">
          <span class="nav-icon">¥</span>
          <span class="nav-label">费用</span>
        </button>
        <button class="nav-item" :class="{ active: curRoute === '/patient/profile' }" @click="goTo('/patient/profile')">
          <span class="nav-icon">○</span>
          <span class="nav-label">我的</span>
        </button>
      </nav>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      curRoute: '/patient/home'
    }
  },
  watch: {
    '$route.path'(val) { this.curRoute = val }
  },
  mounted() { this.curRoute = this.$route.path },
  methods: {
    goTo(path) {
      if (this.curRoute !== path) this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.phone-shell {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 16px;
  background:
    radial-gradient(circle at top, rgba(47, 128, 209, 0.18), transparent 30%),
    linear-gradient(160deg, #eef7ff 0%, #f8fbff 52%, #e9f4ff 100%);
}

.phone-frame {
  width: min(390px, 100vw - 24px);
  height: min(844px, 100vh - 32px);
  min-height: 720px;
  border-radius: 32px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255, 255, 255, 0.9);
  background: var(--page-bg);
  box-shadow: 0 24px 60px rgba(42, 96, 148, 0.22);
}

.status-bar {
  flex: 0 0 auto;
  display: flex;
  justify-content: space-between;
  padding: 12px 24px 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-muted);
  background: rgba(255, 255, 255, 0.76);
}

.page-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.page-content::-webkit-scrollbar {
  display: none;
}

.bottom-nav {
  flex: 0 0 auto;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 4px;
  padding: 8px 10px 10px;
  background: rgba(255, 255, 255, 0.82);
  border-top: 1px solid rgba(221, 234, 247, 0.9);
}

.nav-item {
  border: 0;
  background: transparent;
  min-height: 52px;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  color: var(--ink-muted);
  cursor: pointer;
}

.nav-icon {
  width: 24px;
  height: 24px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  line-height: 1;
  font-weight: 800;
}

.nav-label {
  font-size: 11px;
  font-weight: 700;
}

.nav-item.active {
  color: var(--medical-blue);
  background: rgba(234, 245, 255, 0.9);
}

.nav-item.active .nav-icon {
  color: #fff;
  background: linear-gradient(135deg, var(--medical-blue), var(--medical-blue-dark));
}
</style>
