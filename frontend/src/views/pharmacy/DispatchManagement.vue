<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, Refresh, Box, Check } from '@element-plus/icons-vue'
import FeeStatusTag from '../../components/FeeStatusTag.vue'
import {
  getDispatchQueue,
  getDispenseDetail,
  markDispensed,
  getDispenseStats
} from '../../api/pharmacy'

const loading = ref(false)
const rows = ref([])
const activeRow = ref(null)
const detail = ref(null)
const filters = reactive({
  patientId: null,
  keyword: '',
  department: ''
})
const stats = ref({
  pending: 0,
  today: 0,
  lowStock: 0,
  total: 0
})

const METRICS = computed(() => [
  { label: '待发药', value: stats.value.pending, tone: 'danger', icon: Box },
  { label: '今日已发', value: stats.value.today, tone: 'success', icon: Check },
  { label: '低库存提醒', value: stats.value.lowStock, tone: 'warning', icon: Refresh },
  { label: '处方总数', value: stats.value.total, tone: 'primary', icon: Coin }
])

async function loadStats() {
  try {
    stats.value = await getDispenseStats()
  } catch (_) {
    stats.value = {
      pending: rows.value.filter((r) => r.status !== '已发药').length,
      today: rows.value.filter((r) => r.status === '已发药').length,
      lowStock: 0,
      total: rows.value.length
    }
  }
}

async function loadRows() {
  loading.value = true
  try {
    rows.value = await getDispatchQueue({
      patientId: filters.patientId || undefined,
      keyword: filters.keyword || undefined,
      department: filters.department || undefined
    })
    await loadStats()
  } finally {
    loading.value = false
  }
}

async function selectRow(row) {
  activeRow.value = row
  try {
    detail.value = await getDispenseDetail(row.dispenseId)
  } catch (_) {
    detail.value = null
  }
}

async function dispense(row) {
  await markDispensed({
    dispenseId: row.dispenseId,
    pharmacist: '药房药师'
  })
  ElMessage.success(`处方 ${row.prescriptionNo} 已发药`)
  activeRow.value = null
  detail.value = null
  await loadRows()
}

onMounted(loadRows)

watch(
  () => ({ patientId: filters.patientId, keyword: filters.keyword, department: filters.department }),
  loadRows
)
</script>

<template>
  <div class="pharmacy-page page-surface">
    <div class="page-head">
      <div>
        <p class="section-title">药房发药管理</p>
        <p class="section-subtitle">查看待发药处方队列，核对患者信息与药品明细，完成窗口发药</p>
      </div>
    </div>

    <div class="metric-grid">
      <div v-for="item in METRICS" :key="item.label" class="metric-card">
        <div>
          <p class="metric-label">{{ item.label }}</p>
          <p class="metric-value">{{ item.value }}</p>
        </div>
        <span class="metric-icon" :class="item.tone">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
      </div>
    </div>

    <div class="dashboard-grid">
      <el-card class="medical-card" shadow="never">
        <template #header>
          <div>
            <p class="section-title">待发药处方队列</p>
            <p class="section-subtitle">点击任一行查看处方详情与药品清单</p>
          </div>
        </template>
        <div class="toolbar-row">
          <div class="toolbar-item">
            <label class="filter-label">患者 ID</label>
            <el-input-number v-model="filters.patientId" :min="1" clearable controls-position="right" style="width: 150px" />
          </div>
          <div class="toolbar-item">
            <label class="filter-label">患者姓名/处方号</label>
            <el-input v-model="filters.keyword" placeholder="搜索患者或处方" clearable style="width: 220px" />
          </div>
          <div class="toolbar-item">
            <label class="filter-label">科室</label>
            <el-input v-model="filters.department" placeholder="如：呼吸内科" clearable style="width: 180px" />
          </div>
        </div>
        <el-table :data="rows" v-loading="loading" height="480" @row-click="selectRow">
          <el-table-column prop="prescriptionNo" label="处方编号" min-width="160" />
          <el-table-column prop="patientName" label="患者" min-width="100" />
          <el-table-column prop="gender" label="性别" width="80" />
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column prop="department" label="科室" min-width="120" />
          <el-table-column prop="doctorName" label="开方医生" min-width="100" />
          <el-table-column label="处方金额" width="110">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column label="支付状态" width="110">
            <template #default="{ row }">
              <FeeStatusTag :status="row.payStatus" />
            </template>
          </el-table-column>
          <el-table-column label="发药状态" width="110">
            <template #default="{ row }">
              <FeeStatusTag :status="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="开立时间" min-width="160">
            <template #default="{ row }">{{ row.createdAt }}</template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                :icon="Check"
                :disabled="row.status === '已发药' || row.payStatus !== '已支付'"
                @click.stop="dispense(row)"
              >发药</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无待发药处方" />
          </template>
        </el-table>
      </el-card>

      <el-card class="medical-card" shadow="never">
        <template #header>
          <div>
            <p class="section-title">处方发药详情</p>
            <p class="section-subtitle">核对患者信息、药品明细与用法用量</p>
          </div>
        </template>
        <div v-if="!activeRow" class="detail-empty">
          <el-empty description="请在左侧选择一条处方记录" />
        </div>
        <div v-else class="detail-panel">
          <div class="info-grid">
            <div>
              <span class="lbl">处方编号</span>
              <span class="val">{{ activeRow.prescriptionNo }}</span>
            </div>
            <div>
              <span class="lbl">患者</span>
              <span class="val">{{ activeRow.patientName }} · {{ activeRow.gender }} · {{ activeRow.age }}岁</span>
            </div>
            <div>
              <span class="lbl">科室</span>
              <span class="val">{{ activeRow.department }}</span>
            </div>
            <div>
              <span class="lbl">开方医生</span>
              <span class="val">{{ activeRow.doctorName }}</span>
            </div>
            <div>
              <span class="lbl">诊断</span>
              <span class="val">{{ activeRow.diagnosis || '—' }}</span>
            </div>
            <div>
              <span class="lbl">开立时间</span>
              <span class="val">{{ activeRow.createdAt }}</span>
            </div>
            <div>
              <span class="lbl">金额</span>
              <span class="val">¥{{ activeRow.totalAmount }}</span>
            </div>
            <div>
              <span class="lbl">状态</span>
              <span class="val"><FeeStatusTag :status="activeRow.status" /></span>
            </div>
          </div>

          <div class="drug-list">
            <h4>药品清单</h4>
            <table class="drug-table">
              <thead>
                <tr>
                  <th style="width: 60px;">序号</th>
                  <th>药品名称</th>
                  <th>规格</th>
                  <th style="width: 80px;">数量</th>
                  <th>用法用量</th>
                  <th style="width: 80px;">库存</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in (detail?.items || activeRow.items || [])" :key="item.drugId">
                  <td>{{ idx + 1 }}</td>
                  <td>{{ item.drugName }}</td>
                  <td>{{ item.specification }}</td>
                  <td>{{ item.quantity }} {{ item.unit }}</td>
                  <td>{{ item.usage }}</td>
                  <td :style="{ color: item.stock < item.quantity ? '#d9001b' : 'var(--his-text)' }">
                    {{ item.stock }}
                  </td>
                </tr>
                <tr v-if="!(detail?.items || activeRow.items || []).length">
                  <td colspan="6" style="text-align: center; color: var(--his-text-soft); padding: 20px;">暂无药品明细</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="activeRow.status !== '已发药' && activeRow.payStatus === '已支付'" class="dispense-actions">
            <el-button type="primary" :icon="Check" size="large" @click="dispense(activeRow)">确认发药</el-button>
            <el-button plain @click="activeRow = null; detail = null">关闭</el-button>
          </div>
          <div v-else-if="activeRow.status === '已发药'" class="dispense-done">
            <el-tag type="success" effect="light">该处方已发药</el-tag>
          </div>
          <div v-else-if="activeRow.payStatus !== '已支付'" class="dispense-done">
            <el-tag type="warning" effect="light">该处方尚未完成缴费，请提示患者先完成缴费</el-tag>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.pharmacy-page {
  display: grid;
  gap: 16px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(320px, 1fr);
  gap: 16px;
}

.toolbar-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  overflow-x: auto;
}

.toolbar-row .toolbar-item {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.toolbar-row .filter-label {
  font-size: 13px;
  color: var(--his-text-soft);
  white-space: nowrap;
}

.detail-empty {
  min-height: 300px;
  display: grid;
  place-items: center;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 20px;
  padding: 12px 16px;
  background: #f7fafc;
  border-radius: 8px;
  margin-bottom: 14px;
}

.info-grid > div {
  display: flex;
  gap: 6px;
  font-size: 13px;
}

.lbl {
  color: var(--his-text-soft);
  min-width: 64px;
  flex-shrink: 0;
}

.val {
  color: var(--his-text);
  font-weight: 500;
}

.drug-list h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--his-text);
}

.drug-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  margin-bottom: 14px;
}

.drug-table th,
.drug-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

.drug-table thead th {
  background: #f1f5f9;
  color: var(--his-text-soft);
  font-weight: 600;
}

.drug-table tbody tr:hover {
  background: #f8fafc;
}

.dispense-actions,
.dispense-done {
  display: flex;
  gap: 12px;
  padding: 14px 0 4px;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 1100px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>