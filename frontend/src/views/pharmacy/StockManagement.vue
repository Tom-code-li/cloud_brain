<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Plus, Minus } from '@element-plus/icons-vue'
import FeeStatusTag from '../../components/FeeStatusTag.vue'
import { getDrugStock, updateStock } from '../../api/pharmacy'

const loading = ref(false)
const rows = ref([])
const activeRow = ref(null)
const filters = reactive({
  keyword: '',
  status: ''
})

const LOW_STOCK_THRESHOLD = 20
const WARNING_STOCK_THRESHOLD = 50

const stockMetrics = computed(() => {
  const normal = rows.value.filter((row) => row.stock > WARNING_STOCK_THRESHOLD).length
  const warning = rows.value.filter((row) => row.stock <= WARNING_STOCK_THRESHOLD && row.stock > LOW_STOCK_THRESHOLD).length
  const low = rows.value.filter((row) => row.stock <= LOW_STOCK_THRESHOLD).length
  return {
    normal,
    warning,
    low,
    total: rows.value.length
  }
})

async function loadRows() {
  loading.value = true
  try {
    rows.value = (await getDrugStock({
      keyword: filters.keyword,
      status: filters.status
    })).map((row) => ({
        ...row,
        _purchaseQuantity: 0,
        _supplier: ''
      }))
    activeRow.value = null
  } finally {
    loading.value = false
  }
}

function selectRow(row) {
  activeRow.value = row
  if (!activeRow.value._purchaseQuantity) activeRow.value._purchaseQuantity = 0
  if (!activeRow.value._supplier) activeRow.value._supplier = ''
}

function stockStatus(row) {
  if (row.stock <= LOW_STOCK_THRESHOLD) return 'danger'
  if (row.stock <= WARNING_STOCK_THRESHOLD) return 'warning'
  return 'success'
}

function stockLabel(row) {
  if (row.stock <= LOW_STOCK_THRESHOLD) return '低库存'
  if (row.stock <= WARNING_STOCK_THRESHOLD) return '警戒'
  return '充足'
}

async function increasePurchase(n) {
  if (!activeRow.value) return
  activeRow.value._purchaseQuantity = Math.max(0, (activeRow.value._purchaseQuantity || 0) + n)
}

async function submitStock() {
  if (!activeRow.value) return
  const qty = Number(activeRow.value._purchaseQuantity) || 0
  if (qty <= 0) {
    ElMessage.warning('请输入购药数量')
    return
  }
  await updateStock(activeRow.value.drugId, {
    stock: activeRow.value.stock + qty,
    supplier: activeRow.value._supplier || activeRow.value.defaultSupplier,
    purchaseQuantity: qty
  })
  ElMessage.success(`已新增 ${qty} ${activeRow.value.unit}，当前库存 ${activeRow.value.stock}`)
  activeRow.value._purchaseQuantity = 0
  activeRow.value._supplier = ''
}

onMounted(loadRows)

watch(
  () => ({ keyword: filters.keyword, status: filters.status }),
  loadRows
)
</script>

<template>
  <div class="pharmacy-page page-surface">
    <div class="page-head">
      <div>
        <p class="section-title">药房库存管理</p>
        <p class="section-subtitle">查看药品库存状态，低库存药品提交购药申请并完成入库</p>
      </div>
    </div>

    <div class="metric-grid">
      <div class="metric-card">
        <div>
          <p class="metric-label">库存充足</p>
          <p class="metric-value">{{ stockMetrics.normal }}</p>
        </div>
        <span class="metric-icon success"><el-icon><component :is="Plus" /></el-icon></span>
      </div>
      <div class="metric-card">
        <div>
          <p class="metric-label">库存警戒（&le;{{ WARNING_STOCK_THRESHOLD }}）</p>
          <p class="metric-value">{{ stockMetrics.warning }}</p>
        </div>
        <span class="metric-icon warning"><el-icon><component :is="Minus" /></el-icon></span>
      </div>
      <div class="metric-card">
        <div>
          <p class="metric-label">低库存（&le;{{ LOW_STOCK_THRESHOLD }}）</p>
          <p class="metric-value">{{ stockMetrics.low }}</p>
        </div>
        <span class="metric-icon danger"><el-icon><component :is="ShoppingCart" /></el-icon></span>
      </div>
      <div class="metric-card">
        <div>
          <p class="metric-label">药品总数</p>
          <p class="metric-value">{{ stockMetrics.total }}</p>
        </div>
        <span class="metric-icon primary"><el-icon><component :is="Plus" /></el-icon></span>
      </div>
    </div>

    <div class="dashboard-grid">
      <el-card class="medical-card" shadow="never">
        <template #header>
          <div>
            <p class="section-title">药品库存列表</p>
            <p class="section-subtitle">低库存药品将高亮显示，支持搜索与状态筛选</p>
          </div>
        </template>
        <div class="toolbar-row">
          <div class="toolbar-item">
            <label class="filter-label">药品名称 / 编号</label>
            <el-input v-model="filters.keyword" placeholder="搜索药品" clearable style="width: 220px" />
          </div>
          <div class="toolbar-item">
            <label class="filter-label">库存状态</label>
            <el-select v-model="filters.status" placeholder="全部" clearable style="width: 160px">
              <el-option label="充足" value="success" />
              <el-option label="警戒" value="warning" />
              <el-option label="低库存" value="danger" />
            </el-select>
          </div>
        </div>
        <el-table :data="rows" v-loading="loading" height="480" @row-click="selectRow">
          <el-table-column prop="drugId" label="药品编号" width="100" />
          <el-table-column prop="drugName" label="药品名称" min-width="160" />
          <el-table-column prop="specification" label="规格" min-width="140" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="当前库存" width="110" align="center">
            <template #default="{ row }">
              <span :style="{ color: stockStatus(row) === 'danger' ? '#d9001b' : stockStatus(row) === 'warning' ? '#e6a23c' : '#67c23a', fontWeight: stockStatus(row) === 'danger' ? '700' : '500' }">
                {{ row.stock }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="stockStatus(row)" effect="light">{{ stockLabel(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="defaultSupplier" label="默认供应商" min-width="180" />
          <el-table-column prop="updatedAt" label="最近更新" min-width="150" />
          <template #empty>
            <el-empty description="暂无药品库存" />
          </template>
        </el-table>
      </el-card>

      <el-card class="medical-card" shadow="never">
        <template #header>
          <div>
            <p class="section-title">购药申请 / 入库</p>
            <p class="section-subtitle">选择低库存药品并提交购药数量，完成后库存将自动增加</p>
          </div>
        </template>
        <div v-if="!activeRow" class="detail-empty">
          <el-empty description="请在左侧选择药品" />
        </div>
        <div v-else class="detail-panel">
          <div class="info-grid">
            <div>
              <span class="lbl">药品编号</span>
              <span class="val">{{ activeRow.drugId }}</span>
            </div>
            <div>
              <span class="lbl">药品名称</span>
              <span class="val">{{ activeRow.drugName }}</span>
            </div>
            <div>
              <span class="lbl">规格</span>
              <span class="val">{{ activeRow.specification }}</span>
            </div>
            <div>
              <span class="lbl">单位</span>
              <span class="val">{{ activeRow.unit }}</span>
            </div>
            <div>
              <span class="lbl">当前库存</span>
              <span class="val">
                <el-tag :type="stockStatus(activeRow)" effect="dark">{{ activeRow.stock }}</el-tag>
              </span>
            </div>
            <div>
              <span class="lbl">状态</span>
              <span class="val">{{ stockLabel(activeRow) }}</span>
            </div>
          </div>

          <el-divider>购药申请</el-divider>

          <el-form class="flat-form purchase-form" label-width="100px">
            <el-form-item label="购药数量">
              <el-input-number
                v-model="activeRow._purchaseQuantity"
                :min="1"
                :max="100000"
                :step="10"
                :step-strictly="false"
                size="large"
                controls-position="right"
                style="width: 220px"
              />
              <span class="unit-label">{{ activeRow.unit }}</span>
            </el-form-item>
            <el-form-item label="供应商">
              <el-input
                v-model="activeRow._supplier"
                :placeholder="activeRow.defaultSupplier || '请输入供应商'"
                style="width: 220px"
              />
            </el-form-item>
            <el-form-item label="快捷数量">
              <el-button-group>
                <el-button size="small" @click="increasePurchase(10)">+10</el-button>
                <el-button size="small" @click="increasePurchase(50)">+50</el-button>
                <el-button size="small" @click="increasePurchase(100)">+100</el-button>
                <el-button size="small" @click="increasePurchase(200)">+200</el-button>
              </el-button-group>
            </el-form-item>
          </el-form>

          <div class="submit-actions">
            <el-button type="primary" :icon="ShoppingCart" size="large" @click="submitStock">
              提交购药申请并入库
            </el-button>
            <el-button plain @click="activeRow._purchaseQuantity = 0; activeRow._supplier = ''">
              重置
            </el-button>
          </div>

          <el-alert
            v-if="activeRow.stock <= LOW_STOCK_THRESHOLD"
            type="error"
            :closable="false"
            show-icon
            style="margin-top: 14px"
            title="该药品处于低库存状态，建议尽快提交购药申请补充库存"
          />
          <el-alert
            v-else-if="activeRow.stock <= WARNING_STOCK_THRESHOLD"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 14px"
            title="该药品库存接近警戒线，可考虑适量补充"
          />
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

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.metric-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
}

.metric-label {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: var(--his-text-soft);
}

.metric-value {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--his-text);
}

.metric-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 22px;
  color: #fff;
}

.metric-icon.primary { background: var(--his-primary); }
.metric-icon.success { background: #67c23a; }
.metric-icon.warning { background: #e6a23c; }
.metric-icon.danger { background: #f56c6c; }

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 1fr);
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
}

.info-grid > div {
  display: flex;
  gap: 6px;
  font-size: 13px;
}

.lbl { color: var(--his-text-soft); min-width: 80px; flex-shrink: 0; }
.val { color: var(--his-text); font-weight: 500; }

.purchase-form { margin-top: 10px; }

.unit-label {
  margin-left: 8px;
  color: var(--his-text-soft);
  font-size: 13px;
}

.submit-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  margin-top: 8px;
}

@media (max-width: 1100px) {
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
  .dashboard-grid { grid-template-columns: 1fr; }
}
</style>