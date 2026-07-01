<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Coin, Refresh } from '@element-plus/icons-vue'
import FeeStatusTag from '../../components/FeeStatusTag.vue'
import {
  chargeRegistration,
  confirmOnlineRegistration,
  getOnlinePendingRegistrations
} from '../../api/registration'

const loading = ref(false)
const rows = ref([])
const payMethods = ['现金', '微信', '支付宝', '银行卡', '医保', '混合支付']
const payForm = reactive({
  payMethod: '微信'
})
const page = reactive({
  current: 1,
  size: 8
})

const pagedRows = computed(() => {
  const start = (page.current - 1) * page.size
  return rows.value.slice(start, start + page.size)
})

async function loadRows() {
  loading.value = true
  try {
    rows.value = await getOnlinePendingRegistrations()
    page.current = 1
  } finally {
    loading.value = false
  }
}

async function confirm(row) {
  if (row.feeStatus !== '已支付') {
    ElMessage.warning('该线上挂号未支付，需先收取挂号费')
    return
  }
  const result = await confirmOnlineRegistration(row.registrationId)
  Object.assign(row, result)
  ElMessage.success(`线上挂号已确认，队号 ${result.queueNo}`)
  await loadRows()
}

async function charge(row) {
  const result = await chargeRegistration({
    registrationId: row.registrationId,
    payMethod: payForm.payMethod
  })
  Object.assign(row, result)
  ElMessage.success('挂号费已收取，请继续确认挂号')
}

onMounted(loadRows)
</script>

<template>
  <el-card class="medical-card page-surface" shadow="never">
    <template #header>
      <div class="toolbar-line">
        <div>
          <p class="section-title">线上挂号确认</p>
          <p class="section-subtitle">核对线上挂号记录，已支付后确认并生成候诊队号</p>
        </div>
        <div class="header-actions">
          <div class="pay-method">
            <span class="label">补缴方式</span>
            <el-select v-model="payForm.payMethod" style="width: 150px">
              <el-option v-for="method in payMethods" :key="method" :label="method" :value="method" />
            </el-select>
          </div>
          <el-button :icon="Refresh" :loading="loading" @click="loadRows">刷新</el-button>
        </div>
      </div>
    </template>
    <el-table :data="pagedRows" v-loading="loading" height="520">
      <el-table-column prop="registrationNo" label="挂号单号" min-width="160" />
      <el-table-column prop="patientName" label="患者" min-width="100" />
      <el-table-column prop="deptName" label="科室" min-width="120" />
      <el-table-column prop="doctorName" label="医生" min-width="100" />
      <el-table-column label="排班" min-width="150">
        <template #default="{ row }">
          {{ row.workDate }} {{ row.timePeriod }}
        </template>
      </el-table-column>
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
      <el-table-column prop="queueNo" label="队号" width="90" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" :icon="CircleCheck" @click="confirm(row)">确认</el-button>
          <el-button size="small" :icon="Coin" :disabled="row.feeStatus === '已支付'" @click="charge(row)">
            收费
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无待确认线上挂号" />
      </template>
    </el-table>
    <div class="pager-row">
      <el-pagination
        v-model:current-page="page.current"
        :page-size="page.size"
        layout="prev, pager, next, jumper"
        :total="rows.length"
        :pager-count="5"
      />
    </div>
  </el-card>
</template>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pay-method {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: var(--his-text-soft);
  font-size: 13px;
}

.pager-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
