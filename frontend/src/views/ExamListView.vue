<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWorkbench } from '../api/medicalExamOrder.js'
import { WORKBENCH_STATUS, actionLabel } from '../utils/medicalExamStatusMap.js'
import { fmtTime, calcAge } from '../utils/medicalExamFormat.js'

const router = useRouter()
const activeFilter = ref('all')
const keyword = ref('')
const selectedItem = ref('')
const items = ref([])
const stats = ref({ allCount: 0, pendingCount: 0, progressCount: 0, publishedCount: 0 })

async function load() {
  try {
    const data = await getWorkbench(activeFilter.value, keyword.value, selectedItem.value)
    items.value = data.items
    stats.value = data.stats
  } catch (_) {}
}

onMounted(load)

function setFilter(f) {
  activeFilter.value = f
  load()
}

const TABS = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待处理' },
  { key: 'progress', label: '进行中' },
  { key: 'published', label: '已发布' }
]

const ITEM_OPTIONS = [
  { value: '', label: '全部检查项目' },
  { value: '胸部DR正位片', label: '胸部DR正位片' },
  { value: '心电图', label: '心电图' }
]
</script>

<template>
  <div class="page medical-exam-page">
    <main class="container">
      <div class="page-head">
        <div>
          <span class="eyebrow">检查工作台</span>
          <h1>待检查申请队列</h1>
          <p>项目范围：心电图、胸部CT。请先确认费用已支付，再执行检查并录入结果。</p>
        </div>
        <div class="status-row">
          <span class="badge badge-progress">全部 {{ stats.allCount }}</span>
          <span class="badge badge-unpaid">待处理 {{ stats.pendingCount }}</span>
          <span class="badge badge-ai">进行中 {{ stats.progressCount }}</span>
          <span class="badge badge-published">已发布 {{ stats.publishedCount }}</span>
        </div>
      </div>

      <div class="card filter-card">
        <div class="page-block-title">申请筛选</div>
        <div class="filter-bar">
          <input class="search-input" type="text" placeholder="请输入患者姓名" v-model="keyword" @keyup.enter="load" />
          <select class="search-input item-select" v-model="selectedItem" @change="load">
            <option v-for="item in ITEM_OPTIONS" :key="item.value || 'all'" :value="item.value">
              {{ item.label }}
            </option>
          </select>
          <button class="btn btn-primary" @click="load">搜索</button>
          <div class="filter-tabs">
            <button
              v-for="tab in TABS" :key="tab.key"
              class="filter-tab" :class="{ 'is-active': activeFilter === tab.key }"
              @click="setFilter(tab.key)"
            >{{ tab.label }}</button>
          </div>
        </div>
      </div>

      <div class="card table-card">
        <div class="req-list">
          <div class="req-row req-head">
            <div>编号</div>
            <div>患者姓名</div>
            <div>检查项目</div>
            <div>临床诊断</div>
            <div>状态</div>
            <div>申请时间</div>
            <div>操作</div>
          </div>
          <div v-if="items.length === 0" class="empty-state">
            没有符合条件的检查申请<br><span style="font-size:12.5px;">换个筛选条件或搜索词试试</span>
          </div>
          <div
            v-for="(r, i) in items" :key="r.orderItemId"
            class="req-row"
            :class="{ 'is-unpaid': r.feeStatus !== '已支付' }"
            :data-status="WORKBENCH_STATUS[r.workbenchStatus]?.dataStatus"
          >
            <div class="req-index">{{ i + 1 }}</div>
            <div class="req-patient">
              <div class="name">{{ r.patientName }}</div>
              <div class="meta">{{ r.gender }} · {{ calcAge(r.birthday) }}岁</div>
            </div>
            <div class="req-item-col"><span class="badge badge-item">{{ r.itemName }}</span></div>
            <div class="req-info">
              <div class="diag">{{ r.clinicalDiagnosis }}</div>
              <div class="dept">{{ r.executeDeptName }} · {{ r.applyDoctorName }}医生申请</div>
            </div>
            <div class="req-status-col">
              <span v-if="r.feeStatus !== '已支付'" class="badge badge-unpaid"><span class="badge-dot"></span>未支付</span>
              <span v-else class="badge badge-published"><span class="badge-dot"></span>已支付</span>
              <span class="badge" :class="WORKBENCH_STATUS[r.workbenchStatus]?.badgeClass">
                <span class="badge-dot"></span>{{ WORKBENCH_STATUS[r.workbenchStatus]?.label }}
              </span>
            </div>
            <div class="req-time">{{ fmtTime(r.appliedAt) }}</div>
            <div class="req-action">
              <button class="btn btn-primary btn-sm" @click="router.push('/exam/' + r.orderItemId)">
                {{ actionLabel(r.workbenchStatus) }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
    <footer class="foot">检查/检验医生工作站</footer>
  </div>
</template>
