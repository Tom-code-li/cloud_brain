<template>
  <div class="patient-page">
    <div class="patient-topbar">
      <span @click="$router.back()" class="patient-back">‹ 返回</span>
      <span class="patient-title">我的处方</span>
      <span></span>
    </div>
    <div class="patient-content prescription-list">
      <div class="prescription-card patient-card" v-for="p in list" :key="p.prescriptionId" @click="$router.push('/patient/prescription-detail/' + p.prescriptionId)">
        <div class="card-head">
          <span class="date">{{ p.createdAt?.substring(0,10) }}</span>
          <span class="patient-status" :class="statusClass(p.status)">{{ prescriptionStatusText(p.status) }}</span>
        </div>
        <div class="patient-row"><span class="label">处方编号</span><span class="value">{{ p.prescriptionNo }}</span></div>
        <div class="patient-row"><span class="label">诊断</span><span class="value">{{ p.diagnosis || '无' }}</span></div>
        <div class="patient-row"><span class="label">总金额</span><span class="value fee">¥{{ p.totalAmount }}</span></div>
        <div class="card-footer">查看详情 ›</div>
      </div>
      <div v-if="list.length === 0" class="patient-empty">暂无处方记录</div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { prescriptionStatusText } from '@/utils/statusLabels'
export default {
  data() {
    return { list: [], patientId: null }
  },
  async mounted() {
    const userId = sessionStorage.getItem('userId')
    if (!userId) return
    const res = await axios.get(`/api/patient/info/${userId}`)
    if (res.data.success && res.data.data) {
      this.patientId = res.data.data.patientId
      const r2 = await axios.get(`/api/prescription/my/${this.patientId}`)
      if (r2.data.success) this.list = r2.data.data
    }
  },
  methods: {
    prescriptionStatusText,
    statusClass(status) {
      if (status === '待缴费' || status === '待发药' || status === '发药中' || status === 'active' || status === 'pending') return 'info'
      if (status === '已发药' || status === '已完成' || status === 'dispensed' || status === 'completed') return 'success'
      if (status === '已退药' || status === 'cancelled' || status === 'returned') return 'danger'
      return ''
    }
  }
}
</script>
<style scoped>
.prescription-list { display: flex; flex-direction: column; gap: 12px; }
.prescription-card { padding: 14px; cursor: pointer; }
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.date { font-size: 12px; color: var(--ink-muted); }
.fee { color: var(--medical-red); font-weight: 800; }
.card-footer { text-align: right; font-size: 13px; color: var(--medical-blue); font-weight: 700; margin-top: 8px; }
</style>
