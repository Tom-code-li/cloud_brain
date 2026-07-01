<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import EncounterTabs from '../components/EncounterTabs.vue';
import LibraryDialog from '../components/LibraryDialog.vue';
import PageHeader from '../components/PageHeader.vue';
import PatientBanner from '../components/PatientBanner.vue';
import RequestOrderDetailDialog from '../components/RequestOrderDetailDialog.vue';
import { fetchExamItems, fetchExamLabOrders, skipExam, submitExamRequest as submitExamRequestApi } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';
import { appendUniqueByCode, clone, removeBySelectedCodes, sumItemPrices } from '../utils/outpatientCore.js';

const patientStore = usePatientStore();
const patient = computed(() => patientStore.state.activePatient);
const medicalRecord = computed(() => patientStore.state.medicalRecord);
const items = ref([]);
const selectedItems = ref([]);
const dialogVisible = ref(false);
const keyword = ref('');
const libraryItems = ref([]);
const submittedOrders = ref([]);
const activeSubmittedOrder = ref(null);
const detailVisible = ref(false);
const form = reactive({
  purpose: '',
  site: '',
  notes: ''
});
const total = computed(() => sumItemPrices(items.value));

watch(
  () => patientStore.state.examItems,
  (value) => {
    const draft = patientStore.loadRequestDraft('exam');
    items.value = clone(draft?.items || value || []);
    Object.assign(form, {
      purpose: draft?.form?.purpose || '',
      site: draft?.form?.site || '',
      notes: draft?.form?.notes || ''
    });
  },
  { immediate: true }
);

watch(
  [items, () => form.purpose, () => form.site, () => form.notes],
  () => {
    patientStore.saveRequestDraft('exam', {
      items: clone(items.value),
      form: clone(form)
    });
  },
  { deep: true }
);

async function loadLibrary() {
  const res = await fetchExamItems(keyword.value);
  libraryItems.value = res.data.list;
}

async function openDialog() {
  dialogVisible.value = true;
  await loadLibrary();
}

async function loadSubmittedOrders() {
  if (!medicalRecord.value?.visitId) {
    submittedOrders.value = [];
    return;
  }
  const res = await fetchExamLabOrders({
    visitId: medicalRecord.value.visitId,
    orderType: '检查'
  });
  submittedOrders.value = res.data.list;
}

function openOrderDetail(order) {
  activeSubmittedOrder.value = order;
  detailVisible.value = true;
}

function addItem(row) {
  const next = appendUniqueByCode(items.value, row);
  if (next.length === items.value.length) {
    ElMessage.warning('该检查项目已存在');
    return;
  }
  items.value = next;
  patientStore.setExamItems(next);
  ElMessage.success('检查项目已添加');
}

function removeItem(index) {
  items.value.splice(index, 1);
  patientStore.setExamItems(items.value);
}

function removeSelected() {
  items.value = removeBySelectedCodes(items.value, selectedItems.value);
  patientStore.setExamItems(items.value);
  selectedItems.value = [];
}

function clearRequest() {
  items.value = [];
  selectedItems.value = [];
  Object.assign(form, { purpose: '', site: '', notes: '' });
  patientStore.setExamItems([]);
  patientStore.clearRequestDraft('exam');
}

async function submitRequest() {
  if (!patient.value) {
    ElMessage.warning('请先选择患者');
    return;
  }
  if (!medicalRecord.value?.visitId) {
    ElMessage.warning('当前患者暂无就诊记录');
    return;
  }
  if (!items.value.length) {
    ElMessage.warning('请先添加检查项目');
    return;
  }
  await submitExamRequestApi({
    patientId: medicalRecord.value.patientId,
    visitId: medicalRecord.value.visitId,
    recordId: medicalRecord.value.recordId,
    examItems: clone(items.value),
    form: clone(form)
  });
  patientStore.setExamItems(items.value);
  patientStore.clearRequestDraft('exam');
  await patientStore.refreshSelectedPatient();
  await loadSubmittedOrders();
  ElMessage.success('检查申请已提交');
}

async function markSkipExam() {
  if (!patient.value || !medicalRecord.value?.recordId || !medicalRecord.value?.visitId) {
    ElMessage.warning('当前患者缺少病历上下文');
    return;
  }

  const promptResult = await ElMessageBox.prompt(
    '如有需要可填写原因，系统会记录到病历备注中。',
    '确认本次无需检查',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：症状轻，无影像学检查指征'
    }
  ).catch(() => null);

  if (!promptResult) return;

  const reason = promptResult.value || '';
  await skipExam({
    patientId: medicalRecord.value.patientId,
    visitId: medicalRecord.value.visitId,
    recordId: medicalRecord.value.recordId,
    reason
  });
  patientStore.clearRequestDraft('exam');
  patientStore.setExamItems([]);
  await patientStore.refreshSelectedPatient();
  ElMessage.success('已记录本次无需检查');
}

loadSubmittedOrders();
</script>

<template>
  <section v-if="patient">
    <PatientBanner :patient="patient" />
    <EncounterTabs />
    <PageHeader title="检查申请" description="参考截图完成检查项目、医嘱和费用信息布局。">
      <el-tag effect="light" type="info">检查申请单</el-tag>
    </PageHeader>

    <el-card class="panel">
      <div class="amount-tag">项目金额：{{ total.toFixed(2) }} 元</div>
      <div class="section-toolbar">
        <span class="hint">检查编码 / 检查名称 / 检查规格 / 单价 / 费用分类</span>
        <el-space :size="8">
          <el-button size="small" @click="markSkipExam">本次无需检查</el-button>
          <el-button size="small" :disabled="selectedItems.length === 0" @click="removeSelected">删除</el-button>
          <el-button size="small" type="primary" plain @click="openDialog">增加</el-button>
        </el-space>
      </div>

      <el-table :data="items" @selection-change="selectedItems = $event" stripe border height="220" class="dialog-table">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="code" label="检查编码" min-width="120" />
        <el-table-column prop="name" label="检查名称" min-width="180" />
        <el-table-column prop="spec" label="检查规格" min-width="120" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="feeType" label="费用分类" min-width="120" />
        <el-table-column label="操作" width="90">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="removeItem($index)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><div class="empty-hint">暂无数据</div></template>
      </el-table>

      <div class="sub-title">医嘱</div>
      <div class="medical-form with-top-gap">
        <div class="medical-row">
          <div class="medical-label">目的要求</div>
          <div class="medical-field"><el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请输入检验/检查的目的要求" /></div>
        </div>
        <div class="medical-row">
          <div class="medical-label">检查部位</div>
          <div class="medical-field"><el-input v-model="form.site" type="textarea" :rows="2" placeholder="请输入检查部位" /></div>
        </div>
        <div class="medical-row">
          <div class="medical-label">备注</div>
          <div class="medical-field"><el-input v-model="form.notes" type="textarea" :rows="3" placeholder="请输入检查事项" /></div>
        </div>
      </div>

      <div class="bottom-actions">
        <el-button type="primary" @click="submitRequest">申请提交</el-button>
        <el-button @click="clearRequest">清空表格</el-button>
      </div>
    </el-card>

    <el-card class="panel">
      <template #header>已提交申请记录</template>
      <el-table :data="submittedOrders" stripe border height="220" class="dialog-table">
        <el-table-column prop="orderNo" label="申请单号" min-width="160" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="项目数" width="90">
          <template #default="{ row }">{{ row.items?.length || 0 }}</template>
        </el-table-column>
        <el-table-column prop="appliedAt" label="申请时间" min-width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="openOrderDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
        <template #empty><div class="empty-hint">当前就诊暂无已提交检查申请</div></template>
      </el-table>
    </el-card>

    <LibraryDialog
      v-model="dialogVisible"
      v-model:keyword="keyword"
      title="新增检查项目"
      code-label="检查编码"
      name-label="检查名称"
      :items="libraryItems"
      @search="loadLibrary"
      @select="addItem"
    />

    <RequestOrderDetailDialog
      v-model="detailVisible"
      title="检查申请详情"
      :order="activeSubmittedOrder"
    />
  </section>

  <div v-else class="empty-box">
    <el-empty description="请先在患者查看中选择一个患者" />
  </div>
</template>
