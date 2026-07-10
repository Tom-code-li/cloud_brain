<template>
  <div class="home-page">
    <section class="hero-shell">
      <div class="hero-card">
        <div class="hero-copy">
          <div class="eyebrow">患者服务平台</div>
          <h2>{{ timeGreeting }}，{{ realName || '患者' }}</h2>
          <p>挂号、报告、缴费、病历都放在一个更顺手的入口里。</p>
        </div>
        <div class="avatar">{{ (realName || '患').charAt(0) }}</div>
      </div>
    </section>

    <section class="patient-content">
      <div class="patient-section-title priority-title">
        <span>待办事项</span>
        <span class="section-hint">优先处理</span>
      </div>
      <button class="todo-card patient-card priority-card" v-if="unpaidCount > 0" @click="$router.push('/patient/fees')">
        <span class="todo-dot warning">!</span>
        <span>您有 {{ unpaidCount }} 笔待缴费订单</span>
        <span class="todo-arrow">›</span>
      </button>
      <div class="todo-card patient-card priority-card" v-else>
        <span class="todo-dot success">✓</span>
        <span>暂无待办事项</span>
      </div>

      <div class="patient-section-title">
        <span>快捷功能</span>
        <span class="section-hint">常用操作</span>
      </div>
      <div class="quick-actions">
        <button class="action-card primary" @click="goTo('/patient/appointment')">
          <span class="action-symbol">+</span>
          <span>预约挂号</span>
        </button>
        <button class="action-card" @click="$router.push('/patient/my-appointments')">
          <span class="action-symbol">≡</span>
          <span>我的挂号</span>
        </button>
        <button class="action-card" @click="$router.push('/patient/medical-records')">
          <span class="action-symbol">病</span>
          <span>病历查询</span>
        </button>
        <button class="action-card" @click="$router.push('/patient/fees')">
          <span class="action-symbol">¥</span>
          <span>在线缴费</span>
        </button>
      </div>

      <div class="patient-section-title">
        <span>功能入口</span>
        <span class="section-hint">常看常用</span>
      </div>
      <div class="feature-grid">
        <button class="feature-card patient-card" @click="$router.push('/patient/prescriptions')">
          <span class="feature-icon">Rx</span>
          <span>我的处方</span>
        </button>
        <button class="feature-card patient-card" @click="$router.push('/patient/exam-reports')">
          <span class="feature-icon">检</span>
          <span>检查报告</span>
        </button>
        <button class="feature-card patient-card" @click="$router.push('/patient/ai-center')">
          <span class="feature-icon">AI</span>
          <span>AI 就医</span>
        </button>
        <button class="feature-card patient-card" @click="$router.push('/patient/profile')">
          <span class="feature-icon">我</span>
          <span>个人信息</span>
        </button>
      </div>

    </section>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      realName: sessionStorage.getItem('realName') || '',
      unpaidCount: 0
    }
  },
  computed: {
    timeGreeting() {
      const hour = new Date().getHours()
      if (hour < 6) return '凌晨好'
      if (hour < 12) return '早上好'
      if (hour < 18) return '下午好'
      return '晚上好'
    }
  },
  mounted() {
    this.loadUnpaid()
  },
  methods: {
    goTo(path) {
      this.$router.push(path)
    },
    async loadUnpaid() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) return
      try {
        const res = await axios.get(`/api/patient/info/${userId}`)
        if (res.data.success && res.data.data) {
          const pid = res.data.data.patientId
          const feeRes = await axios.get(`/api/fee/unpaid/${pid}`)
          if (feeRes.data.success) this.unpaidCount = feeRes.data.data.length
        }
      } catch (e) {}
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100%;
  padding: 0 0 18px;
  background:
    radial-gradient(circle at top right, rgba(47, 128, 209, 0.12), transparent 34%),
    linear-gradient(180deg, #f9fcff 0%, var(--page-bg) 34%, #eef6ff 100%);
}

.hero-shell {
  position: relative;
  padding: 12px 12px 0;
}

.hero-card {
  position: relative;
  z-index: 2;
  margin: 0;
  padding: 24px 20px 26px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  color: #fff;
  border-radius: 28px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(47, 128, 209, 0.98), rgba(31, 102, 173, 0.98)),
    linear-gradient(180deg, #2f80d1, #1f66ad);
  box-shadow: 0 22px 42px rgba(31, 102, 173, 0.28);
  backdrop-filter: blur(10px);
}

.hero-card::before {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 34px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0), rgba(245, 250, 255, 0.26));
}

.hero-card::after {
  content: "";
  position: absolute;
  right: -38px;
  top: -42px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0;
  opacity: 0.9;
  margin-bottom: 10px;
}

.hero-card h2 {
  font-size: 25px;
  line-height: 1.25;
  margin-bottom: 8px;
}

.hero-card p {
  font-size: 14px;
  line-height: 1.6;
  opacity: 0.92;
  max-width: 72%;
}

.avatar {
  position: relative;
  z-index: 1;
  width: 54px;
  height: 54px;
  border-radius: 18px;
  background: rgba(255,255,255,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 800;
  flex-shrink: 0;
}

.quick-actions {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.action-card {
  min-height: 76px;
  border: 0;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(226, 237, 249, 0.88);
  box-shadow: 0 8px 18px rgba(25, 74, 150, 0.06);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 8px;
  padding: 14px 15px;
  color: var(--ink-strong);
  font-size: 14px;
  font-weight: 700;
}

.action-card.primary {
  color: #fff;
  background: linear-gradient(135deg, #2f80d1, #1f66ad);
}

.action-symbol {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.16);
  font-size: 16px;
}

.patient-content {
  padding: 16px 12px 0;
}

.priority-title {
  padding-top: 6px;
}

.patient-section-title {
  padding: 16px 2px 10px;
  font-size: 15px;
  font-weight: 800;
  color: var(--ink-strong);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-hint {
  font-size: 12px;
  color: var(--ink-muted);
  font-weight: 700;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 0;
}

.feature-card {
  min-height: 90px;
  border: 0;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(226, 237, 249, 0.88);
  box-shadow: 0 8px 18px rgba(25, 74, 150, 0.06);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 8px;
  padding: 14px 15px;
  color: var(--ink-strong);
  font-size: 14px;
  font-weight: 700;
}

.feature-icon {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: var(--medical-blue-soft);
  color: var(--medical-blue);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
}

.todo-card {
  margin: 0;
  min-height: 58px;
  border: 0;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(226, 237, 249, 0.88);
  box-shadow: 0 8px 18px rgba(25, 74, 150, 0.06);
  padding: 14px 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-main);
  font-size: 14px;
  font-weight: 700;
}

.priority-card {
  width: 100%;
  margin-bottom: 4px;
}

.todo-dot {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 800;
}

.todo-dot.warning {
  color: var(--medical-orange);
  background: #fff6df;
}

.todo-dot.success {
  color: var(--medical-green);
  background: #e8f8f2;
}

.todo-arrow {
  margin-left: auto;
  color: var(--ink-muted);
  font-size: 22px;
}
</style>
