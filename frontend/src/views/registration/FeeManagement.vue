<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Coin, Refresh, Search } from '@element-plus/icons-vue'
import { getPendingFees } from '../../api/registration'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const filters = reactive({
  patientId: null,
  registrationId: null,
  keyword: ''
})
const page = reactive({
  current: 1,
  size: 8
})

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

const patientRows = computed(() => {
  const grouped = new Map()

  for (const row of rows.value || []) {
    const patientId = row.patientId ?? 'unknown'
    const key = String(patientId)

    if (!grouped.has(key)) {
      grouped.set(key, {
        patientId,
        patientName: row.patientName || '未知患者',
        registrations: new Set(),
        items: [],
        pendingCount: 0,
        pendingAmount: 0
      })
    }

    const group = grouped.get(key)
    group.patientName = group.patientName || row.patientName || '未知患者'
    if (row.registrationId !== undefined && row.registrationId !== null) {
      group.registrations.add(row.registrationId)
    }
    group.items.push(row)
    group.pendingCount += 1
    group.pendingAmount += pendingAmount(row)
  }

  return Array.from(grouped.values())
    .map((item) => ({
      ...item,
      registrationIds: Array.from(item.registrations).sort((a, b) => toNumber(a) - toNumber(b))
    }))
    .filter((item) => {
      const keyword = String(filters.keyword || '').trim().toLowerCase()
      if (!keyword) {
        return true
      }
      return (
        String(item.patientName || '').toLowerCase().includes(keyword) ||
        String(item.patientId || '').toLowerCase().includes(keyword) ||
        item.registrationIds.some((id) => String(id).includes(keyword))
      )
    })
    .sort((a, b) => {
      if (b.pendingAmount !== a.pendingAmount) {
        return b.pendingAmount - a.pendingAmount
      }
      return toNumber(a.patientId) - toNumber(b.patientId)
    })
})

const pagedRows = computed(() => {
  const start = (page.current - 1) * page.size
  return patientRows.value.slice(start, start + page.size)
})

async function loadRows() {
  loading.value = true
  try {
    rows.value = await getPendingFees({
      patientId: toId(filters.patientId),
      registrationId: toId(filters.registrationId)
    })
    page.current = 1
  } catch (error) {
    ElMessage.error(error?.message || '加载待缴费患者失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.patientId = null
  filters.registrationId = null
  filters.keyword = ''
  loadRows()
}

function openDetail(row) {
  const query = {}
  if (row.registrationIds.length === 1) {
    query.registrationId = row.registrationIds[0]
  }

  router.push({
    name: 'registration-fee-management-detail',
    params: { patientId: row.patientId },
    query
  })
}

function formatAmount(value) {
  return `￥${toNumber(value).toFixed(2)}`
}

onMounted(loadRows)
</script>

<template>
  <div class="registration-fee-management page-surface">
    <el-card class="medical-card" shadow="never">
      <template #header>
        <div class="page-head">
          <div>
            <p class="section-title">收费管理</p>
            <p class="section-subtitle">按患者汇总待缴费项目，进入详情页后再完成收款</p>
          </div>
          <el-button :icon="Refresh" :loading="loading" @click="loadRows">刷新</el-button>
        </div>
      </template>

      <el-form class="flat-form toolbar-form" inline @submit.prevent>
        <el-form-item label="患者ID">
          <el-input-number v-model="filters.patientId" :min="1" controls-position="right" style="width: 150px" />
        </el-form-item>
        <el-form-item label="挂号ID">
          <el-input-number v-model="filters.registrationId" :min="1" controls-position="right" style="width: 150px" />
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="filters.keyword" clearable placeholder="输入姓名关键字" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" :loading="loading" @click="loadRows">查询</el-button>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="pagedRows" v-loading="loading" height="520">
        <el-table-column label="患者" min-width="160">
          <template #default="{ row }">
            <div class="patient-cell">
              <div class="patient-name">{{ row.patientName }}</div>
              <div class="patient-meta">患者ID {{ row.patientId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="挂号记录" width="110">
          <template #default="{ row }">{{ row.registrationIds.length }}</template>
        </el-table-column>
        <el-table-column label="待缴费项目" width="120">
          <template #default="{ row }">{{ row.pendingCount }}</template>
        </el-table-column>
        <el-table-column label="待收金额" width="120">
          <template #default="{ row }">{{ formatAmount(row.pendingAmount) }}</template>
        </el-table-column>
        <el-table-column label="涉及挂号ID" min-width="200">
          <template #default="{ row }">
            <div class="tag-wrap">
              <el-tag v-for="id in row.registrationIds" :key="id" size="small" effect="plain">
                {{ id }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" :icon="Coin" @click="openDetail(row)">收款</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无待缴费患者" />
        </template>
      </el-table>
      <div class="pager-row">
        <el-pagination
          v-model:current-page="page.current"
          :page-size="page.size"
          layout="prev, pager, next, jumper"
          :total="patientRows.length"
          :pager-count="5"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.registration-fee-management {
  display: grid;
  gap: 16px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.toolbar-form {
  margin-bottom: 12px;
}

.patient-cell {
  display: grid;
  gap: 4px;
}

.patient-name {
  font-weight: 600;
  color: var(--his-text);
}

.patient-meta {
  font-size: 12px;
  color: var(--his-text-soft);
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
