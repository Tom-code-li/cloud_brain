<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Coin, Refresh } from '@element-plus/icons-vue'
import { chargeFeeOrder, getPendingFees } from '../../api/registration'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const payLoading = ref(false)
const payingId = ref(null)
const rows = ref([])
const payForm = reactive({
  payMethod: '现金'
})
const page = reactive({
  current: 1,
  size: 8
})

const payMethods = ['现金', '微信', '支付宝', '银行卡', '医保', '混合支付']

function toNumber(value) {
  const num = Number(value)
  return Number.isFinite(num) ? num : 0
}

function toId(value) {
  const num = Number(value)
  return Number.isFinite(num) && num > 0 ? num : undefined
}

function pendingAmount(row) {
  return Math.max(
    toNumber(row.totalAmount) - toNumber(row.paidAmount) - toNumber(row.refundAmount),
    0
  )
}

const patientId = computed(() => toId(route.params.patientId))
const registrationId = computed(() => toId(route.query.registrationId))

const summary = computed(() => {
  if (!rows.value.length) {
    return null
  }

  const patientName = rows.value[0]?.patientName || '未知患者'
  const ids = new Set()
  let total = 0

  for (const row of rows.value) {
    if (row.registrationId !== undefined && row.registrationId !== null) {
      ids.add(row.registrationId)
    }
    total += pendingAmount(row)
  }

  return {
    patientName,
    patientId: rows.value[0]?.patientId || patientId.value,
    registrationIds: Array.from(ids).sort((a, b) => toNumber(a) - toNumber(b)),
    itemCount: rows.value.length,
    totalAmount: total
  }
})

const pagedRows = computed(() => {
  const start = (page.current - 1) * page.size
  return rows.value.slice(start, start + page.size)
})

function formatAmount(value) {
  return `￥${toNumber(value).toFixed(2)}`
}

function statusType(status) {
  if (status === '已支付') return 'success'
  if (status === '待支付') return 'warning'
  if (status === '部分退费') return 'info'
  if (status === '已退费') return 'danger'
  return 'info'
}

async function loadDetail() {
  if (!patientId.value) {
    rows.value = []
    return
  }

  loading.value = true
  try {
    rows.value = await getPendingFees({
      patientId: patientId.value,
      registrationId: registrationId.value
    })
    page.current = 1
  } catch (error) {
    rows.value = []
    ElMessage.error(error?.message || '加载患者缴费详情失败')
  } finally {
    loading.value = false
  }
}

async function payOne(row) {
  payingId.value = row.feeOrderId
  try {
    await chargeFeeOrder({
      feeOrderId: row.feeOrderId,
      payMethod: payForm.payMethod
    })
    ElMessage.success(`费用单 ${row.feeOrderId} 已收款`)
    await loadDetail()
  } catch (error) {
    ElMessage.error(error?.message || '收款失败')
  } finally {
    payingId.value = null
  }
}

async function payAll() {
  if (!rows.value.length) {
    return
  }

  payLoading.value = true
  try {
    const snapshot = [...rows.value]
    for (const row of snapshot) {
      await chargeFeeOrder({
        feeOrderId: row.feeOrderId,
        payMethod: payForm.payMethod
      })
    }
    ElMessage.success('该患者待缴费项目已全部收款')
    await loadDetail()
    if (!rows.value.length) {
      router.push({ name: 'registration-fee-management' })
    }
  } catch (error) {
    ElMessage.error(error?.message || '批量收款失败')
  } finally {
    payLoading.value = false
  }
}

onMounted(loadDetail)

watch(
  () => [route.params.patientId, route.query.registrationId],
  loadDetail
)
</script>

<template>
  <div class="registration-fee-detail page-surface">
    <el-card class="medical-card" shadow="never">
      <template #header>
        <div class="page-head">
          <div>
            <p class="section-title">患者缴费详情</p>
            <p class="section-subtitle">查看该患者所有待缴费项目，并在此选择支付方式完成收款</p>
          </div>
          <div class="header-actions">
            <el-button :icon="ArrowLeft" @click="router.push({ name: 'registration-fee-management' })">
              返回列表
            </el-button>
            <el-button :icon="Refresh" :loading="loading" @click="loadDetail">刷新</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!summary && !loading" description="暂无待缴费项目" />

      <div v-else class="detail-stack">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="患者姓名">{{ summary?.patientName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="患者ID">{{ summary?.patientId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="待缴费项目">{{ summary?.itemCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="涉及挂号ID" :span="2">
            <div class="tag-wrap">
              <el-tag v-for="id in summary?.registrationIds || []" :key="id" size="small" effect="plain">
                {{ id }}
              </el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="待收金额">
            {{ formatAmount(summary?.totalAmount || 0) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="action-bar">
          <div class="pay-method">
            <span class="label">支付方式</span>
            <el-select v-model="payForm.payMethod" style="width: 160px">
              <el-option v-for="method in payMethods" :key="method" :label="method" :value="method" />
            </el-select>
          </div>
          <el-button type="primary" :icon="Coin" :loading="payLoading" @click="payAll">全部收款</el-button>
        </div>

        <el-table :data="pagedRows" v-loading="loading" height="520">
          <el-table-column prop="feeOrderId" label="费用单ID" width="110" />
          <el-table-column prop="registrationId" label="挂号ID" width="100" />
          <el-table-column prop="feeType" label="费用类型" min-width="110" />
          <el-table-column label="待收金额" width="110">
            <template #default="{ row }">{{ formatAmount(pendingAmount(row)) }}</template>
          </el-table-column>
          <el-table-column label="已收金额" width="110">
            <template #default="{ row }">{{ formatAmount(row.paidAmount) }}</template>
          </el-table-column>
          <el-table-column label="退费金额" width="110">
            <template #default="{ row }">{{ formatAmount(row.refundAmount) }}</template>
          </el-table-column>
          <el-table-column label="支付状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusType(row.payStatus)" effect="plain">{{ row.payStatus || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="业务状态" width="110">
            <template #default="{ row }">
              <el-tag type="info" effect="plain">{{ row.businessStatus || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                :icon="Coin"
                :loading="payingId === row.feeOrderId"
                @click="payOne(row)"
              >
                收款
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="该患者暂无待缴费项目" />
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
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.registration-fee-detail {
  display: grid;
  gap: 16px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.detail-stack {
  display: grid;
  gap: 16px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pay-method {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  color: var(--his-text-soft);
  font-size: 13px;
}

.tag-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.pager-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
