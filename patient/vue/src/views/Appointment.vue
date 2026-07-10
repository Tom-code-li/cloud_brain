<template>
  <div class="patient-page">
    <div class="patient-topbar">
      <span @click="$router.push('/patient/home')" class="patient-back">首页</span>
      <span class="patient-title">预约挂号</span>
      <span></span>
    </div>

    <div class="step-indicator">
      <div class="step" :class="{ active: step >= 1, done: step > 1 }">选科室</div>
      <div class="step-line"></div>
      <div class="step" :class="{ active: step >= 2, done: step > 2 }">选医生</div>
      <div class="step-line"></div>
      <div class="step" :class="{ active: step >= 3 }">选时间</div>
    </div>

    <div v-if="step === 1" class="patient-content">
      <div class="search-card patient-card">
        <input v-model="deptSearch" placeholder="搜索科室" class="search-input" />
      </div>
      <button v-if="selectedParentDept" class="step-back patient-card" @click="backToParentDepts">‹ 返回一级科室</button>
      <div v-if="selectedParentDept" class="selected-info patient-card">一级科室：{{ selectedParentDept.deptName }}</div>
      <div class="list">
        <button class="list-item patient-card" v-for="d in visibleDepts" :key="d.deptId" @click="handleDeptClick(d)">
          <span class="list-icon">科</span>
          <span class="list-info">
            <span class="list-title">{{ d.deptName }}</span>
            <span class="list-desc">{{ deptDescription(d) }}</span>
          </span>
          <span class="arrow">›</span>
        </button>
        <div v-if="visibleDepts.length === 0" class="patient-empty">暂无科室数据</div>
      </div>
    </div>

    <div v-if="step === 2" class="patient-content">
      <button class="step-back patient-card" @click="backStep">‹ 上一级</button>
      <div class="selected-info patient-card">已选科室：{{ selectedDept?.deptName }}</div>
      <div class="list">
        <button class="list-item patient-card doctor-item" :class="{ full: isDoctorFull(d) }" v-for="d in doctors" :key="d.doctorId" @click="handleDoctorClick(d)">
          <span class="doctor-avatar">{{ (d.realName || d.title || d.doctorType || '医').charAt(0) }}</span>
          <span class="list-info">
            <span class="list-title">{{ d.realName || '未命名医生' }}</span>
            <span class="list-desc">{{ d.doctorType || '医生' }} {{ d.title || '' }} · {{ d.doctorNo || '' }}</span>
            <span class="list-desc">{{ d.specialty || '暂无介绍' }}</span>
          </span>
        </button>
        <div v-if="doctors.length === 0" class="patient-empty">该科室暂无医生排班</div>
      </div>
    </div>

    <div v-if="step === 3" class="patient-content">
      <button class="step-back patient-card" @click="backStep">‹ 上一级</button>
      <div class="selected-info patient-card">{{ selectedDept?.deptName }} · {{ selectedDoctor?.realName || selectedDoctor?.doctorType }} · {{ selectedDoctor?.title || '' }}</div>
      <div class="list">
        <button class="list-item patient-card schedule-item" :class="{ full: isScheduleFull(s) }" v-for="s in schedules" :key="s.scheduleId" @click="handleScheduleClick(s)">
          <span class="list-icon">时</span>
          <span class="list-info">
            <span class="list-title">{{ s.workDate }} {{ periodText(s.timePeriod) }}</span>
            <span class="list-desc">{{ s.startTime }} - {{ s.endTime }} · 剩余 {{ s.remainQuota }} / {{ s.totalQuota }} 号</span>
          </span>
          <span v-if="isAlreadyBooked(s)" class="doctor-status full">已挂号</span>
          <span v-else-if="scheduleStatus(s) === '可预约'" class="fee-tag">¥{{ s.registrationFee }}</span>
          <span v-else class="doctor-status full">{{ scheduleStatusText(s) }}</span>
        </button>
        <div v-if="schedules.length === 0" class="patient-empty">暂无排班信息</div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import feedback from '@/utils/feedback'
import { timePeriodText } from '@/utils/statusLabels'
export default {
  data() {
    return {
      step: 1,
      deptSearch: '',
      departments: [],
      selectedParentDept: null,
      selectedDept: null,
      doctors: [],
      selectedDoctor: null,
      schedules: [],
      patientId: null,
      myRegistrations: []
    }
  },
  computed: {
    parentDepts() {
      return this.departments.filter(d => !d.parentId)
    },
    childDeptsByParent() {
      return this.departments.reduce((grouped, dept) => {
        if (dept.parentId) {
          if (!grouped[dept.parentId]) grouped[dept.parentId] = []
          grouped[dept.parentId].push(dept)
        }
        return grouped
      }, {})
    },
    visibleDepts() {
      const source = this.deptSearch
        ? this.departments
        : (this.selectedParentDept ? this.childDeptsByParent[this.selectedParentDept.deptId] || [] : this.parentDepts)
      if (!this.deptSearch) return source
      return source.filter(d => (d.deptName || '').includes(this.deptSearch))
    }
  },
  async mounted() {
    await this.loadDepartments()
    await this.loadPatient()
    if (this.patientId) await this.loadMyRegistrations()
  },
  methods: {
    periodText(value) {
      return timePeriodText(value)
    },
    async loadPatient() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) return
      try {
        const res = await axios.get(`/api/patient/info/${userId}`)
        if (res.data.success && res.data.data) {
          this.patientId = res.data.data.patientId
        }
      } catch(e) {}
    },
    async loadMyRegistrations() {
      try {
        const res = await axios.get(`/api/registration/my/${this.patientId}`)
        if (res.data.success) {
          this.myRegistrations = res.data.data || []
        }
      } catch(e) {}
    },
    async loadDepartments() {
      try {
        const res = await axios.get('/api/registration/departments')
        if (res.data.success) this.departments = res.data.data
      } catch(e) {}
    },
    deptDescription(d) {
      const childCount = this.childDeptsByParent[d.deptId]?.length || 0
      if (!d.parentId && childCount > 0) return `${childCount} 个二级科室`
      return d.location || d.description || d.deptType || '可预约科室'
    },
    handleDeptClick(d) {
      const children = this.childDeptsByParent[d.deptId] || []
      if (!d.parentId && children.length > 0) {
        this.selectedParentDept = d
        this.deptSearch = ''
        return
      }
      this.selectDept(d)
    },
    backToParentDepts() {
      this.selectedParentDept = null
      this.deptSearch = ''
    },
    async selectDept(d) {
      this.selectedDept = d
      this.selectedParentDept = null
      this.deptSearch = ''
      this.step = 2
      try {
        const res = await axios.get(`/api/registration/doctors/${d.deptId}`)
        if (res.data.success) this.doctors = res.data.data
      } catch(e) {}
    },
    isDoctorFull(d) {
      return (d.appointmentStatus || '约满') !== '可预约' || Number(d.availableQuota || 0) <= 0
    },
    handleDoctorClick(d) {
      if (this.isDoctorFull(d)) {
        feedback.toast('该医生不可预约')
        return
      }
      this.selectDoctor(d)
    },
    async selectDoctor(d) {
      this.selectedDoctor = d
      this.step = 3
      try {
        const res = await axios.get(`/api/registration/schedules/${d.doctorId}`)
        if (res.data.success) this.schedules = res.data.data
      } catch(e) {}
      if (this.patientId) await this.loadMyRegistrations()
    },
    activeRegistrationForSchedule(s) {
      if (!s || !this.selectedDoctor) return null
      return this.myRegistrations.find(r =>
        String(r.doctorId) === String(this.selectedDoctor.doctorId) &&
        String(r.scheduleId) === String(s.scheduleId) &&
        ['待支付', '待确认', '接诊中'].includes(r.status)
      ) || null
    },
    isAlreadyBooked(s) {
      return !!this.activeRegistrationForSchedule(s)
    },
    isScheduleFull(s) {
      return this.isAlreadyBooked(s) || this.scheduleStatus(s) !== '可预约'
    },
    scheduleStatus(s) {
      if (!s) return '停用'
      const status = s.status || '停用'
      if (status === '已过期') return '已过期'
      if (status === '停用') return '停用'
      if (Number(s.remainQuota || 0) <= 0) return '约满'
      return '可预约'
    },
    scheduleStatusText(s) {
      const status = this.scheduleStatus(s)
      if (status === '可预约') return '可预约'
      if (status === '停用') return '停用'
      return '约满'
    },
    handleScheduleClick(s) {
      if (this.isAlreadyBooked(s)) {
        feedback.toast('您已挂过这个号')
        return
      }
      const status = this.scheduleStatus(s)
      if (status === '可预约') {
        this.confirmBooking(s)
        return
      }
      if (status === '停用') {
        feedback.toast('该时间段已停用')
        return
      }
      if (status === '约满') {
        feedback.toast('该时间段可预约人数为零')
        return
      }
    },
    backStep() {
      if (this.step === 3) {
        this.step = 2
        this.selectedDoctor = null
        this.schedules = []
      } else if (this.step === 2) {
        this.step = 1
        this.selectedDept = null
        this.selectedParentDept = null
        this.doctors = []
      }
    },
    async confirmBooking(s) {
      if (!this.patientId) { feedback.toast('请先登录'); return }
      const reg = {
        patientId: this.patientId,
        doctorId: this.selectedDoctor.doctorId,
        deptId: this.selectedDept.deptId,
        scheduleId: s.scheduleId,
        registrationFee: s.registrationFee,
        source: '线上'
      }
      try {
        const res = await axios.post('/api/registration/book', reg)
        if (res.data.success) {
          feedback.toast('挂号成功，请按时就诊')
          this.$router.push('/patient/my-appointments')
        } else {
          feedback.toast(res.data.message)
        }
      } catch(e) {
        feedback.toast('挂号失败')
      }
    }
  }
}
</script>

<style scoped>
.step-indicator {
  display: flex;
  align-items: center;
  padding: 14px 16px 4px;
  gap: 8px;
  background: var(--page-bg);
}

.step {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid var(--line-soft);
  color: var(--ink-muted);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.step.active {
  color: #fff;
  background: linear-gradient(135deg, var(--medical-blue), var(--medical-blue-dark));
  border-color: transparent;
}

.step.done {
  color: var(--medical-blue);
  background: var(--medical-blue-soft);
}

.step-line {
  flex: 1;
  height: 2px;
  background: var(--line-soft);
}

.search-card {
  padding: 10px;
  margin-bottom: 12px;
}

.search-input {
  width: 100%;
  height: 38px;
  border: 0;
  outline: none;
  border-radius: 10px;
  padding: 0 12px;
  font-size: 14px;
  background: #f7fbff;
  color: var(--ink-main);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-item {
  width: 100%;
  border: 0;
  min-height: 72px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  color: var(--ink-main);
  cursor: pointer;
}

.list-icon,
.doctor-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: var(--medical-blue-soft);
  color: var(--medical-blue);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  flex: 0 0 auto;
}

.doctor-avatar {
  border-radius: 50%;
}

.list-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.list-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--ink-strong);
}

.list-desc {
  font-size: 12px;
  color: var(--ink-muted);
  line-height: 1.4;
}

.arrow {
  color: var(--ink-muted);
  font-size: 22px;
}

.doctor-item.full {
  color: #94a3b8;
  background: #f3f6f9;
  cursor: not-allowed;
}

.doctor-item.full .doctor-avatar {
  background: #e5eaf0;
  color: #94a3b8;
}

.doctor-item.full .list-title,
.doctor-item.full .list-desc {
  color: #94a3b8;
}

.doctor-status {
  min-width: 52px;
  padding: 4px 8px;
  border-radius: 999px;
  background: var(--medical-blue-soft);
  color: var(--medical-blue);
  font-size: 12px;
  font-weight: 800;
  text-align: center;
  flex: 0 0 auto;
}

.doctor-status.full {
  background: #e5eaf0;
  color: #94a3b8;
}

.fee-tag {
  color: var(--medical-red);
  font-size: 15px;
  font-weight: 800;
  flex: 0 0 auto;
}

.schedule-item.full {
  color: #94a3b8;
  background: #f3f6f9;
  cursor: not-allowed;
}

.schedule-item.full .list-icon {
  background: #e5eaf0;
  color: #94a3b8;
}

.schedule-item.full .list-title,
.schedule-item.full .list-desc {
  color: #94a3b8;
}

.selected-info {
  padding: 12px 14px;
  margin-bottom: 12px;
  color: var(--medical-blue);
  font-size: 14px;
  font-weight: 700;
}

.step-back {
  width: 100%;
  min-height: 42px;
  margin-bottom: 10px;
  border: 0;
  padding: 0 14px;
  text-align: left;
  color: var(--medical-blue);
  background: #fff;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}
</style>
