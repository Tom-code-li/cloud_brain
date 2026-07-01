<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'
import FeeStatusTag from '../../components/FeeStatusTag.vue'
import { getRefundRecords, submitRefund } from '../../api/pharmacy'

const loading = ref(false)
const rows = ref([])
const activeRow = ref(null)
const form = reactive({
  keyword: '',
  status: '',
  patientId: null
})
const filters = reactive({
  keyword: '',
  status: '',
  patientId: null
})

const refundItems = ref([])
const refundReason = ref('')

async function loadRows() {
  loading.value = true
  try {
    rows.value = await getRefundRecords({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      patientId: filters.patientId || undefined
    });
    activeRow.value = null;
  } finally {
    loading.value = false;
  }
}

function selectRow(row) {
  activeRow.value = row;
  refundItems.value = (row.items || []).map((item) => ({
    ...item,
    selected: false,
    refundQuantity: 0
  }));
  refundReason.value = '';
}

const canSubmit = computed(() => {
  if (!activeRow.value || activeRow.value.status === '已退药') return false;
  const hasSelected = refundItems.value.some((item) => item.selected && item.refundQuantity > 0);
  return hasSelected && refundReason.value.trim().length > 0;
})

async function submitRefundRecord() {
  const payload = {
    dispenseId: activeRow.value.dispenseId,
    reason: refundReason.value.trim(),
    items: refundItems.value
      .filter((item) => item.selected && item.refundQuantity > 0)
      .map((item) => ({
        drugId: item.drugId,
        drugName: item.drugName,
        specification: item.specification,
        refundQuantity: item.refundQuantity,
        unit: item.unit
      }))
  };
  await submitRefund(payload);
  await loadRows();
}

function toggleAll(event) {
  const checked = event.target.checked;
  refundItems.value.forEach((item) => {
    item.selected = checked;
    if (checked && item.refundQuantity === 0) item.refundQuantity = item.quantity;
    if (!checked) item.refundQuantity = 0;
  });
}

onMounted(loadRows);

watch(
  () => ({ keyword: filters.keyword, status: filters.status, patientId: filters.patientId }),
  loadRows
);
</script>

<template>
  <div class="pharmacy-page page-surface">
    <div class="page-head">
      <div>
        <p class="section-title">药房退药管理</p>
        <p class="section-subtitle">查询已发药处方，选择药品并提交退药，完成药品库存回退</p>
      </div>
    </div>

    <div class="dashboard-grid">
      <el-card class="medical-card" shadow="never">
        <template #header>
          <div>
            <p class="section-title">退药处方列表</p>
            <p class="section-subtitle">支持按患者、处方号与退药状态筛选</p>
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
            <label class="filter-label">退药状态</label>
            <el-select v-model="filters.status" placeholder="全部" clearable style="width: 130px">
              <el-option label="可退药" value="可退药" />
              <el-option label="已退药" value="已退药" />
            </el-select>
          </div>
        </div>
        <el-table :data="rows" v-loading="loading" height="520" @row-click="selectRow">
          <el-table-column prop="prescriptionNo" label="处方编号" min-width="160" />
          <el-table-column prop="patientName" label="患者" min-width="100" />
          <el-table-column prop="gender" label="性别" width="80" />
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column prop="department" label="科室" min-width="120" />
          <el-table-column prop="doctorName" label="开方医生" min-width="100" />
          <el-table-column label="金额" width="100">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <FeeStatusTag :status="row.status" />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发药时间" min-width="160" />
          <template #empty>
            <el-empty description="暂无退药记录" />
          </template>
        </el-table>
      </el-card>

      <el-card class="medical-card" shadow="never">
        <template #header>
          <div>
            <p class="section-title">退药详情</p>
            <p class="section-subtitle">勾选需退药的药品，填写退药数量与原因后提交</p>
          </div>
        </template>

        <div v-if="!activeRow" class="detail-empty">
          <el-empty description="请在左侧选择一条已发药处方" />
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
              <span class="lbl">状态</span>
              <span class="val"><FeeStatusTag :status="activeRow.status" /></span>
            </div>
          </div>

          <el-table :data="refundItems" size="small" border style="margin: 14px 0">
            <el-table-column label="选择" width="64" align="center">
              <template #header>
                <input
                  type="checkbox"
                  :disabled="activeRow.status === '已退药'"
                  :checked="refundItems.length > 0 && refundItems.every((item) => item.selected)"
                  @change="toggleAll"
                />
              </template>
              <template #default="{ row }">
                <input
                  type="checkbox"
                  v-model="row.selected"
                  :disabled="activeRow.status === '已退药'"
                />
              </template>
            </el-table-column>
            <el-table-column prop="drugName" label="药品名称" min-width="140" />
            <el-table-column prop="specification" label="规格" min-width="120" />
            <el-table-column label="已发/可退" width="100" align="center">
              <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
            </el-table-column>
            <el-table-column label="退药数量" width="130" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.refundQuantity"
                  :min="0"
                  :max="row.quantity"
                  :step="1"
                  :disabled="activeRow.status === '已退药' || !row.selected"
                  controls-position="right"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" align="center" />
          </el-table>

          <el-form class="flat-form" :model="form" label-width="76px">
            <el-form-item label="退药原因">
              <el-input
                v-model="refundReason"
                type="textarea"
                :rows="3"
                :disabled="activeRow.status === '已退药'"
                placeholder="请填写退药原因，如：患者过敏、药品未开封、医嘱调整等"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>

          <div class="refund-actions">
            <el-button
              type="primary"
              :icon="CircleCheck"
              size="large"
              :disabled="!canSubmit"
              @click="submitRefundRecord"
            >提交退药</el-button>
            <el-button plain @click="activeRow = null">关闭</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.pharmacy-page { display: grid; gap: 16px; }

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 1fr);
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

.lbl { color: var(--his-text-soft); min-width: 64px; flex-shrink: 0; }
.val { color: var(--his-text); font-weight: 500; }

.refund-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 1100px) {
  .dashboard-grid { grid-template-columns: 1fr; }
}
</style>