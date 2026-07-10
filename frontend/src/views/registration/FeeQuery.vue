<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import FeeStatusTag from '../../components/FeeStatusTag.vue'
import { getFeeHistory } from '../../api/registration'

const loading = ref(false)
const history = ref(null)
const filters = reactive({
  keyword: '',
  patientId: null,
  registrationId: null
})
const page = reactive({
  current: 1,
  size: 10
})

const rows = computed(() => history.value?.orders || [])

const filteredRows = computed(() => {
  const keyword = filters.keyword.trim()
  if (!keyword) return rows.value
  return rows.value.filter((row) => Object.values(row).some((value) => String(value).includes(keyword)))
})

const pagedRows = computed(() => {
  const start = (page.current - 1) * page.size
  return filteredRows.value.slice(start, start + page.size)
})

async function query() {
  loading.value = true
  try {
    history.value = await getFeeHistory({
      patientId: filters.patientId || undefined,
      registrationId: filters.registrationId || undefined
    })
    page.current = 1
  } finally {
    loading.value = false
  }
}

watch(
  () => filters.keyword,
  () => {
    page.current = 1
  }
)
</script>

<template>
  <el-card class="medical-card page-surface" shadow="never">
    <template #header>
      <div class="toolbar-line">
        <div>
          <p class="section-title">费用查询</p>
          <p class="section-subtitle">按患者或挂号记录查询收费、退费和费用状态</p>
        </div>
        <el-input v-model="filters.keyword" class="query-input" placeholder="费用类型 / 状态">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </template>
    <el-form class="flat-form query-form" :model="filters" inline>
      <el-form-item label="患者 ID">
        <el-input-number v-model="filters.patientId" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="挂号 ID">
        <el-input-number v-model="filters.registrationId" :min="1" controls-position="right" />
      </el-form-item>
      <el-button type="primary" :icon="Search" :loading="loading" @click="query">查询</el-button>
    </el-form>
    <el-table :data="pagedRows" v-loading="loading" height="520">
      <el-table-column prop="feeOrderId" label="费用单 ID" width="110" />
      <el-table-column prop="patientName" label="患者" min-width="100" />
      <el-table-column prop="registrationId" label="挂号 ID" width="100" />
      <el-table-column prop="feeType" label="费用类型" min-width="160" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">￥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="支付状态" width="120">
        <template #default="{ row }">
          <FeeStatusTag :status="row.payStatus" />
        </template>
      </el-table-column>
      <el-table-column label="执行状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.executed ? 'warning' : 'success'" effect="plain">
            {{ row.executed ? '已执行' : '未执行' }}
          </el-tag>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="请输入条件查询费用" />
      </template>
    </el-table>
    <div class="pager-row">
      <el-pagination
        v-model:current-page="page.current"
        :page-size="page.size"
        layout="prev, pager, next, jumper"
        :total="filteredRows.length"
        :pager-count="5"
      />
    </div>
  </el-card>
</template>

<style scoped>
.query-input {
  width: min(320px, 100%);
}

.query-form {
  margin-bottom: 12px;
}

.pager-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
