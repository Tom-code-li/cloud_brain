<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Coin, PhoneFilled, RefreshRight } from '@element-plus/icons-vue'
import FeeStatusTag from '../../components/FeeStatusTag.vue'
import {
  chargeRegistration,
  getOnlinePendingRegistrations,
  getPendingFees,
  getRegistrationQueue,
  refundFee
} from '../../api/registration'

const loading = ref(false)
const queue = ref([])
const pendingFees = ref([])
const onlinePending = ref([])
const page = reactive({
  current: 1,
  size: 8
})

const quick = reactive({
  registrationId: 1,
  payMethod: '现金',
  feeOrderId: 1,
  reason: '患者取消就诊'
})

const metrics = computed(() => [
  { label: '候诊人数', value: queue.value.length, tone: 'success' },
  { label: '待缴费', value: pendingFees.value.length, tone: 'danger' },
  { label: '线上待确认', value: onlinePending.value.length, tone: 'warning' },
  { label: '今日挂号', value: queue.value.length + pendingFees.value.length + onlinePending.value.length, tone: 'primary' }
])

const pagedQueue = computed(() => {
  const start = (page.current - 1) * page.size
  return queue.value.slice(start, start + page.size)
})

async function refresh() {
  loading.value = true
  try {
    const [queueData, feeData, onlineData] = await Promise.all([
      getRegistrationQueue(),
      getPendingFees(),
      getOnlinePendingRegistrations()
    ])
    queue.value = queueData
    pendingFees.value = feeData
    onlinePending.value = onlineData
    page.current = 1
  } finally {
    loading.value = false
  }
}

async function charge() {
  await chargeRegistration({
    registrationId: quick.registrationId,
    payMethod: quick.payMethod
  })
  ElMessage.success('费用已收取，患者进入后续流程')
  await refresh()
}

async function refund() {
  await refundFee({
    feeOrderId: quick.feeOrderId,
    reason: quick.reason
  })
  ElMessage.success('退费已处理')
  await refresh()
}

onMounted(refresh)
</script>

<template>
  <div class="registration-dashboard page-surface">
    <div class="toolbar-line">
      <div />
      <el-button :loading="loading" @click="refresh">刷新数据</el-button>
    </div>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <div>
          <p class="metric-label">{{ item.label }}</p>
          <p class="metric-value">{{ item.value }}</p>
        </div>
        <span class="metric-icon" :class="item.tone">
          <el-icon v-if="item.tone === 'primary'"><Bell /></el-icon>
          <el-icon v-else-if="item.tone === 'danger'"><Coin /></el-icon>
          <el-icon v-else-if="item.tone === 'warning'"><RefreshRight /></el-icon>
          <el-icon v-else><PhoneFilled /></el-icon>
        </span>
      </div>
    </div>

    <section class="dashboard-grid">
      <el-card class="medical-card" shadow="never">
        <template #header>
          <div class="toolbar-line">
            <div>
              <p class="section-title">候诊队列</p>
              <p class="section-subtitle">缴费和确认后的患者进入候诊</p>
            </div>
          </div>
        </template>
        <el-table :data="pagedQueue" v-loading="loading" height="320">
          <el-table-column prop="queueNo" label="队号" width="80" />
          <el-table-column prop="registrationNo" label="挂号单号" min-width="160" />
          <el-table-column prop="patientName" label="患者" min-width="100" />
          <el-table-column prop="deptName" label="科室" min-width="120" />
          <el-table-column prop="doctorName" label="医生" min-width="100" />
          <el-table-column label="费用" width="110">
            <template #default="{ row }">
              <FeeStatusTag :status="row.feeStatus" />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <FeeStatusTag :status="row.status" />
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无候诊患者" />
          </template>
        </el-table>
        <div class="pager-row">
          <el-pagination
            v-model:current-page="page.current"
            :page-size="page.size"
            layout="prev, pager, next, jumper"
            :total="queue.length"
            :pager-count="5"
          />
        </div>
      </el-card>

      <el-card class="medical-card" shadow="never">
        <template #header>
          <div>
            <p class="section-title">快捷收费退费</p>
            <p class="section-subtitle">窗口常用操作入口</p>
          </div>
        </template>
        <el-form class="flat-form" :model="quick" label-position="top">
          <el-form-item label="挂号 ID">
            <el-input-number v-model="quick.registrationId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="支付方式">
            <el-select v-model="quick.payMethod">
              <el-option label="现金" value="现金" />
              <el-option label="医保" value="医保" />
              <el-option label="微信" value="微信" />
              <el-option label="支付宝" value="支付宝" />
            </el-select>
          </el-form-item>
          <el-button type="primary" :icon="Coin" @click="charge">确认收款</el-button>
          <el-divider />
          <el-form-item label="费用单 ID">
            <el-input-number v-model="quick.feeOrderId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="退费原因">
            <el-input v-model="quick.reason" type="textarea" :rows="3" />
          </el-form-item>
          <el-button type="warning" plain :icon="RefreshRight" @click="refund">处理退费</el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.registration-dashboard {
  display: grid;
  gap: 16px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.7fr);
  gap: 16px;
}

.metric-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
}

.metric-icon.primary {
  color: #1d4ed8;
  background: #dbeafe;
}

.metric-icon.danger {
  color: #b91c1c;
  background: #fee2e2;
}

.metric-icon.warning {
  color: #b45309;
  background: #fef3c7;
}

.metric-icon.success {
  color: #15803d;
  background: #dcfce7;
}

.pager-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

@media (max-width: 960px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
