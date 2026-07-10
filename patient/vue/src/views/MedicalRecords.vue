<template>
  <div class="patient-page">
    <div class="patient-topbar">
      <span @click="$router.push('/patient/home')" class="patient-back">首页</span>
      <span class="patient-title">病历查询</span>
      <span></span>
    </div>

    <div class="patient-content record-list">
      <div class="record-card patient-card" v-for="r in records" :key="r.recordId" @click="$router.push('/patient/medical-record-detail/' + r.recordId)">
        <div class="card-head">
          <span class="date">{{ r.createdAt?.substring(0,10) }}</span>
          <span class="patient-status" :class="recordStatusClass(r.status)">{{ recordStatusText(r.status) }}</span>
        </div>
        <div class="patient-row"><span class="label">主诉</span><span class="value">{{ r.chiefComplaint || '无' }}</span></div>
        <div class="patient-row"><span class="label">诊断</span><span class="value">{{ r.diagnosis || '待诊断' }}</span></div>
        <div class="card-footer">查看详情 ›</div>
      </div>
      <div v-if="records.length === 0" class="patient-empty">暂无病历记录</div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { registrationStatusText } from '@/utils/statusLabels'
export default {
  data() {
    return { records: [], patientId: null }
  },
  async mounted() {
    await this.loadPatient()
    if (this.patientId) await this.loadData()
  },
  methods: {
    recordStatusText(value) {
      return registrationStatusText(value)
    },
    recordStatusClass(status) {
      if (status === '已完成' || status === 'completed') return 'success'
      return 'info'
    },
    async loadPatient() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) return
      const res = await axios.get(`/api/patient/info/${userId}`)
      if (res.data.success && res.data.data) this.patientId = res.data.data.patientId
    },
    async loadData() {
      const res = await axios.get(`/api/medical-record/my/${this.patientId}`)
      if (res.data.success) this.records = res.data.data
    }
  }
}
</script>

<style scoped>
.record-list { display: flex; flex-direction: column; gap: 12px; }
.record-card { padding: 14px; cursor: pointer; }
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.date { font-size: 12px; color: var(--ink-muted); }
.card-footer { text-align: right; font-size: 13px; color: var(--medical-blue); font-weight: 700; margin-top: 8px; }
</style>
