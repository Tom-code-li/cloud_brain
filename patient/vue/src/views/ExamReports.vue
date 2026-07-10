<template>
  <div class="patient-page">
    <div class="patient-topbar">
      <span @click="$router.back()" class="patient-back">‹ 返回</span>
      <span class="patient-title">检查报告</span>
      <span></span>
    </div>
    <div class="patient-tabs">
      <button class="patient-tab" :class="{ active: tab === 'order' }" @click="tab='order'">检查单</button>
      <button class="patient-tab" :class="{ active: tab === 'report' }" @click="tab='report'">报告单</button>
    </div>
    <div class="patient-content report-list">
      <template v-if="tab === 'order'">
        <div class="report-card patient-card" v-for="o in orderList" :key="o.orderId">
          <div class="card-head">
            <span class="date">{{ o.createdAt?.substring(0,10) }}</span>
            <span class="patient-status" :class="orderStatusClass(o.status)">{{ orderStatusText(o.status) }}</span>
          </div>
          <div class="patient-row"><span class="label">检查单号</span><span class="value">{{ o.orderNo }}</span></div>
          <div class="patient-row"><span class="label">类型</span><span class="value">{{ o.orderType }}</span></div>
          <div class="patient-row"><span class="label">临床信息</span><span class="value">{{ o.clinicalInfo || o.clinicalDiagnosis }}</span></div>
          <div class="patient-row"><span class="label">申请目的</span><span class="value">{{ o.purpose || '暂无' }}</span></div>
        </div>
        <div v-if="orderList.length === 0" class="patient-empty">暂无检查单</div>
      </template>

      <template v-else>
        <div class="report-card patient-card" v-for="r in reportList" :key="r.reportId">
          <div class="card-head">
            <span class="date">{{ r.createdAt?.substring(0,10) }}</span>
            <span class="patient-status" :class="reportStatusClass(r.status)">{{ reportStatusText(r.status) }}</span>
          </div>
          <div class="patient-row"><span class="label">报告编号</span><span class="value">{{ r.reportNo }}</span></div>
          <div class="patient-row"><span class="label">类型</span><span class="value">{{ r.reportType }}</span></div>
          <div class="patient-row"><span class="label">结果</span><span class="value">{{ r.findings || '暂无' }}</span></div>
          <div class="patient-row"><span class="label">结论</span><span class="value">{{ r.conclusion || '待审核' }}</span></div>
        </div>
        <div v-if="reportList.length === 0" class="patient-empty">暂无报告单</div>
      </template>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { itemTypeText, reportStatusText } from '@/utils/statusLabels'
export default {
  data() {
    return { tab: 'order', orderList: [], reportList: [], patientId: null }
  },
  methods: {
    itemTypeText,
    reportStatusText,
    orderStatusText(status) {
      if (status === 'completed' || status === '已完成') return '已完成'
      if (status === 'pending' || status === '待执行' || status === '待缴费') return '待执行'
      return status || ''
    },
    orderStatusClass(status) {
      if (status === 'completed' || status === '已完成') return 'success'
      return 'warning'
    },
    reportStatusClass(status) {
      if (status === 'published' || status === '已发布') return 'success'
      return 'warning'
    }
  },
  async mounted() {
    const userId = sessionStorage.getItem('userId')
    if (!userId) return
    const res = await axios.get(`/api/patient/info/${userId}`)
    if (res.data.success && res.data.data) {
      this.patientId = res.data.data.patientId
      const [ordersRes, reportsRes] = await Promise.all([
        axios.get(`/api/exam/orders/${this.patientId}`),
        axios.get(`/api/exam/reports/${this.patientId}`)
      ])
      if (ordersRes.data.success) this.orderList = ordersRes.data.data
      if (reportsRes.data.success) this.reportList = reportsRes.data.data
    }
  }
}
</script>

<style scoped>
.report-list { display: flex; flex-direction: column; gap: 12px; }
.report-card { padding: 14px; }
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.date { font-size: 12px; color: var(--ink-muted); }
</style>
