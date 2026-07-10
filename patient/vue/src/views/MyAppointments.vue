<template>
  <div class="patient-page">
    <div class="patient-topbar">
      <span @click="$router.push('/patient/home')" class="patient-back">首页</span>
      <span class="patient-title">我的挂号</span>
      <span></span>
    </div>

    <div ref="filterShell" class="filter-shell">
      <button class="filter-trigger" type="button" @click="toggleFilterPanel">
        <span class="filter-trigger-label">挂号状态</span>
        <span class="filter-trigger-value">{{ currentTabLabel }}</span>
        <span class="filter-trigger-arrow" :class="{ open: filterPanelOpen }"></span>
      </button>

      <transition name="panel-fade">
        <div v-if="filterPanelOpen" class="filter-panel">
          <button
            v-for="option in filterOptions"
            :key="option.value"
            type="button"
            class="filter-option"
            :class="{ active: tab === option.value }"
            @click="selectTab(option.value)"
          >
            <span class="filter-option-title">{{ option.label }}</span>
            <span class="filter-option-count">{{ option.count }}</span>
          </button>
        </div>
      </transition>
    </div>

    <div class="patient-content appointment-list">
      <div
        class="record-card patient-card appointment-card"
        v-for="r in filteredList"
        :key="r.registrationId"
        @click="onCardClick(r)"
      >
        <div class="card-head">
          <span class="patient-status" :class="statusClass(r.status)">{{ registrationStatusText(r.status) }}</span>
          <span class="date">{{ r.registeredAt?.substring(0,10) }}</span>
        </div>
        <div class="patient-row"><span class="label">挂号编号</span><span class="value">{{ r.registrationNo }}</span></div>
        <div class="patient-row"><span class="label">科室</span><span class="value">{{ r.deptName || r.deptId }}</span></div>
        <div class="patient-row"><span class="label">医生</span><span class="value">{{ r.doctorName || '医生 ' + r.doctorId }}</span></div>
        <div class="patient-row"><span class="label">费用</span><span class="value fee">¥{{ r.registrationFee }}</span></div>
        <div class="patient-row">
          <span class="label">缴费状态</span>
          <span class="value" :class="feeStatusClass(r.feeStatus)">{{ feeStatusText(r.feeStatus) }}</span>
        </div>
        <div class="card-actions" v-if="r.status === '待支付' || r.status === '待确认'">
          <button
            class="patient-button secondary"
            :disabled="cancelingId === r.registrationId"
            @click.stop="cancelReg(r.registrationId)"
          >
            {{ cancelingId === r.registrationId ? '取消中...' : '取消挂号' }}
          </button>
          <button v-if="r.feeStatus === '待支付'" class="patient-button" @click.stop="payFee(r)">去缴费</button>
        </div>
      </div>
      <div v-if="filteredList.length === 0" class="patient-empty">暂无挂号记录</div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import feedback from '@/utils/feedback'
import { feeStatusText, registrationStatusText } from '@/utils/statusLabels'
export default {
  data() {
    return {
      tab: 'all',
      registrations: [],
      patientId: null,
      cancelingId: null,
      filterPanelOpen: false
    }
  },
  computed: {
    filterOptions() {
      const labels = [
        { value: 'all', label: '全部' },
        { value: '待支付', label: '待支付' },
        { value: '待确认', label: '待确认' },
        { value: '接诊中', label: '接诊中' },
        { value: '已取消', label: '已取消' },
        { value: '爽约', label: '爽约' },
        { value: '已退号', label: '已退号' }
      ]
      return labels.map(item => ({
        ...item,
        count: item.value === 'all'
          ? this.registrations.length
          : this.registrations.filter(r => this.statusGroup(r.status) === item.value).length
      }))
    },
    currentTabLabel() {
      const active = this.filterOptions.find(item => item.value === this.tab)
      return active ? `${active.label}${active.value === 'all' ? '' : ` · ${active.count}`}` : '全部'
    },
    filteredList() {
      if (this.tab === 'all') return this.registrations
      return this.registrations.filter(r => this.statusGroup(r.status) === this.tab)
    }
  },
  async mounted() {
    await this.loadPatient()
    if (this.patientId) await this.loadData()
    document.addEventListener('click', this.handleDocumentClick)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleDocumentClick)
  },
  methods: {
    registrationStatusText,
    feeStatusText,
    toggleFilterPanel() {
      this.filterPanelOpen = !this.filterPanelOpen
    },
    selectTab(value) {
      this.tab = value
      this.filterPanelOpen = false
    },
    onCardClick(r) {
      if (!r || this.cancelingId === r.registrationId) return
      if (r.feeStatus === '待支付') {
        this.payFee(r)
        return
      }
      feedback.toast('暂无可查看的详情')
    },
    handleDocumentClick(event) {
      const shell = this.$refs.filterShell
      if (!shell) return
      if (!shell.contains(event.target)) {
        this.filterPanelOpen = false
      }
    },
    statusGroup(status) {
      if (status === '待支付' || status === 'unpaid') return '待支付'
      if (status === '待确认' || status === 'registered' || status === 'waiting_confirmation') return '待确认'
      if (status === '接诊中' || status === 'in_visit') return '接诊中'
      if (status === '已取消' || status === 'cancelled') return '已取消'
      if (status === '爽约' || status === 'no_show') return '爽约'
      if (status === '已退号' || status === 'returned') return '已退号'
      if (status === '已完成' || status === 'completed') return '已完成'
      return status || ''
    },
    statusClass(status) {
      if (status === '待支付' || status === 'unpaid') return 'warn'
      if (status === '待确认' || status === '接诊中' || status === 'registered') return 'info'
      if (status === '已完成' || status === 'completed') return 'success'
      if (status === '已取消' || status === '已退号' || status === '爽约' || status === 'returned' || status === 'cancelled') return 'danger'
      return ''
    },
    feeStatusClass(status) {
      if (status === '待支付' || status === 'unpaid') return 'warn'
      if (status === '已退费' || status === 'refunded') return 'info-text'
      return 'success'
    },
    async loadPatient() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) return
      const res = await axios.get(`/api/patient/info/${userId}`)
      if (res.data.success && res.data.data) this.patientId = res.data.data.patientId
    },
    async loadData() {
      const res = await axios.get(`/api/registration/my/${this.patientId}`)
      if (res.data.success) this.registrations = res.data.data
    },
    async cancelReg(id) {
       if (!id) {
         feedback.toast('缺少挂号ID')
         return
       }
       if (!(await feedback.confirm('确认取消挂号？'))) return
       this.cancelingId = id
       try {
         const res = await axios.post(`/api/registration/cancel/${id}`)
         if (res.data.success) {
           feedback.toast('取消成功')
           await this.loadData()
         } else {
           feedback.toast(res.data.message || '取消失败')
         }
       } catch (e) {
         feedback.toast(e.response?.data?.message || '操作失败')
       } finally {
         this.cancelingId = null
       }
    },
    async payFee(r) {
      try {
        const feeRes = await axios.get(`/api/fee/unpaid/${this.patientId}`)
        const fees = feeRes.data.data || []
        const match = fees.find(f => f.registrationId === r.registrationId)
        if (match) this.$router.push({ path: '/patient/fees', query: { feeOrderId: match.feeOrderId } })
        else feedback.toast('暂未生成缴费单')
      } catch(e) { feedback.toast('操作失败') }
    }
  }
}
</script>

<style scoped>
.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-shell {
  position: relative;
  margin-bottom: 12px;
}

.filter-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--line-soft);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  cursor: pointer;
}

.filter-trigger-label {
  font-size: 12px;
  color: var(--ink-muted);
  flex: 0 0 auto;
}

.filter-trigger-value {
  margin-left: auto;
  color: var(--ink-strong);
  font-weight: 800;
}

.filter-trigger-arrow {
  width: 10px;
  height: 10px;
  border-right: 2px solid var(--ink-muted);
  border-bottom: 2px solid var(--ink-muted);
  transform: rotate(45deg);
  transition: transform 0.2s ease;
  margin-left: 4px;
}

.filter-trigger-arrow.open {
  transform: rotate(-135deg);
}

.filter-panel {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  right: 0;
  z-index: 20;
  padding: 12px;
  border: 1px solid var(--line-soft);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.filter-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  text-align: left;
}

.filter-option-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-strong);
}

.filter-option-count {
  min-width: 28px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #f2f6ff;
  color: var(--medical-blue);
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

.filter-option.active {
  border-color: var(--medical-blue);
  background: linear-gradient(180deg, rgba(48, 118, 255, 0.08), rgba(48, 118, 255, 0.03));
  box-shadow: 0 8px 18px rgba(48, 118, 255, 0.12);
}

.filter-option.active .filter-option-title {
  color: var(--medical-blue);
}

.filter-option.active .filter-option-count {
  background: rgba(48, 118, 255, 0.14);
  color: var(--medical-blue);
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.tabs-scroll {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
  overflow-x: auto;
  overflow-y: hidden;
  -ms-overflow-style: none;
  scrollbar-width: none;
  padding-bottom: 4px;
  margin-bottom: 12px;
  cursor: grab;
  user-select: none;
  touch-action: pan-y;
}

.tabs-scroll::-webkit-scrollbar {
  display: none;
}

.tabs-scroll:active {
  cursor: grabbing;
}

.record-card {
  padding: 14px;
}

.appointment-card {
  cursor: pointer;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.date {
  font-size: 12px;
  color: var(--ink-muted);
}

.fee {
  color: var(--medical-red);
  font-weight: 800;
}

.warn {
  color: var(--medical-orange);
  font-weight: 800;
}

.success {
  color: var(--medical-green);
  font-weight: 800;
}

.info-text {
  color: var(--medical-blue);
  font-weight: 800;
}

.card-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line-soft);
}
</style>
