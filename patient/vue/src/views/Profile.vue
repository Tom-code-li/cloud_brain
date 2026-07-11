<template>
  <div class="patient-page">
    <div class="patient-topbar">
      <span @click="$router.push('/patient/home')" class="patient-back">首页</span>
      <span class="patient-title">个人信息</span>
      <span></span>
    </div>

    <div class="patient-content">
      <section class="profile-card">
        <div class="avatar-lg">{{ (patientInfo.patientName || '患').charAt(0) }}</div>
        <div class="name">{{ patientInfo.patientName || '未设置' }}</div>
        <div class="patient-no">编号：{{ patientInfo.patientNo || '暂无' }}</div>
      </section>

      <section class="info-list patient-card">
        <div class="patient-row">
          <span class="label">姓名</span>
          <span class="value">{{ patientInfo.patientName || '-' }}</span>
        </div>
        <div class="patient-row">
          <span class="label">性别</span>
          <span class="value">{{ genderText(patientInfo.gender) }}</span>
        </div>
        <div class="patient-row">
          <span class="label">手机号</span>
          <span class="value">{{ patientInfo.phone || '-' }}</span>
        </div>
        <div class="patient-row">
          <span class="label">身份证</span>
          <span class="value">{{ patientInfo.idCard || '-' }}</span>
        </div>
        <div class="patient-row">
          <span class="label">地址</span>
          <span class="value">{{ patientInfo.address || '未设置' }}</span>
        </div>
        <div class="patient-row">
          <span class="label">过敏史</span>
          <span class="value">{{ patientInfo.allergyHistory || '无' }}</span>
        </div>
      </section>

      <section class="menu-list patient-card">
        <button class="menu-item" @click="$router.push('/patient/medical-records')">
          <span class="menu-icon">病</span><span>病历查询</span><span class="arrow">›</span>
        </button>
        <button class="menu-item" @click="$router.push('/patient/prescriptions')">
          <span class="menu-icon">方</span><span>我的处方</span><span class="arrow">›</span>
        </button>
        <button class="menu-item" @click="$router.push('/patient/exam-reports')">
          <span class="menu-icon">检</span><span>检查报告</span><span class="arrow">›</span>
        </button>
        <button class="menu-item" @click="$router.push('/patient/edit-profile')">
          <span class="menu-icon">改</span><span>修改个人信息</span><span class="arrow">›</span>
        </button>
        <button class="menu-item logout" @click="logout">
          <span class="menu-icon">退</span><span>退出登录</span><span class="arrow">›</span>
        </button>
      </section>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() {
    return { patientInfo: {} }
  },
  async mounted() {
    const userId = sessionStorage.getItem('userId')
    if (!userId) return
    const res = await axios.get(`/api/patient/info/${userId}`)
    if (res.data.success) this.patientInfo = res.data.data || {}
  },
  methods: {
    genderText(value) {
      if (value === '男' || value === 'male') return '男'
      if (value === '女' || value === 'female') return '女'
      return value || '-'
    },
    async logout() {
      const userId = sessionStorage.getItem('userId')
      if (userId) {
        try {
          await axios.post(`/api/patient/logout/${userId}`)
        } catch (e) {}
      }
      sessionStorage.clear()
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.profile-card {
  text-align: center;
  padding: 28px 16px;
  border-radius: 18px;
  color: #fff;
  background:
    linear-gradient(135deg, var(--medical-blue), var(--medical-blue-dark));
  box-shadow: var(--shadow-soft);
}

.avatar-lg {
  width: 72px;
  height: 72px;
  border-radius: 24px;
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 800;
  margin: 0 auto 12px;
}

.name {
  font-size: 20px;
  font-weight: 800;
}

.patient-no {
  font-size: 12px;
  opacity: 0.84;
  margin-top: 6px;
}

.info-list {
  margin-top: 14px;
  padding: 12px 14px;
}

.menu-list {
  margin-top: 14px;
  padding: 4px 0;
  overflow: hidden;
}

.menu-item {
  width: 100%;
  border: 0;
  background: #fff;
  min-height: 52px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-main);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.menu-item + .menu-item {
  border-top: 1px solid var(--line-soft);
}

.menu-icon {
  width: 30px;
  height: 30px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--medical-blue);
  background: var(--medical-blue-soft);
  font-size: 13px;
  font-weight: 800;
}

.menu-item.logout {
  color: var(--medical-red);
}

.menu-item.logout .menu-icon {
  color: var(--medical-red);
  background: #fff0f2;
}

.arrow {
  margin-left: auto;
  color: var(--ink-muted);
  font-size: 22px;
}
</style>
