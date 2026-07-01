<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import EncounterTabs from '../components/EncounterTabs.vue';
import LibraryDialog from '../components/LibraryDialog.vue';
import PageHeader from '../components/PageHeader.vue';
import PatientBanner from '../components/PatientBanner.vue';
import RequestOrderDetailDialog from '../components/RequestOrderDetailDialog.vue';
import { fetchExamLabOrders, fetchLabItems, skipLab, submitLabRequest as submitLabRequestApi } from '../api/outpatient.js';
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
  specimen: '静脉血',
  notes: '',
  priority: '普通',
  collectionWay: '门诊采样'
});
const total = computed(() => sumItemPrices(items.value));

watch(
  () => patientStore.state.labItems,
  (value) => {
    const draft = patientStore.loadRequestDraft('lab');
    items.value = clone(draft?.items || value || []);
    Object.assign(form, {
      purpose: draft?.form?.purpose || '',
      specimen: draft?.form?.specimen || '静脉血',
      notes: draft?.form?.notes || '',
      priority: draft?.form?.priority || '普通',
      collectionWay: draft?.form?.collectionWay || '门诊采样'
    });
  },
  { immediate: true }
);

watch(
  [items, () => form.purpose, () => form.specimen, () => form.notes, () => form.priority, () => form.collectionWay],
  () => {
    patientStore.saveRequestDraft('lab', {
      items: clone(items.value),
      form: clone(form)
    });
  },
  { deep: true }
);

async function loadLibrary() {
  const res = await fetchLabItems(keyword.value);
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
    orderType: '检验'
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
    ElMessage.warning('该检验项目已存在');
    return;
  }
  items.value = next;
  patientStore.setLabItems(next);
  ElMessage.success('检验项目已添加');
}

function removeItem(index) {
  items.value.splice(index, 1);
  patientStore.setLabItems(items.value);
}

function removeSelected() {
  items.value = removeBySelectedCodes(items.value, selectedItems.value);
  patientStore.setLabItems(items.value);
  selectedItems.value = [];
}

function clearRequest() {
  items.value = [];
  selectedItems.value = [];
  Object.assign(form, {
    purpose: '',
    specimen: '静脉血',
    notes: '',
    priority: '普通',
    collectionWay: '门诊采样'
  });
  patientStore.setLabItems([]);
  patientStore.clearRequestDraft('lab');
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
    ElMessage.warning('请先添加检验项目');
    return;
  }
  await submitLabRequestApi({
    patientId: medicalRecord.value.patientId,
    visitId: medicalRecord.value.visitId,
    recordId: medicalRecord.value.recordId,
    labItems: clone(items.value),
    form: clone(form)
  });
  patientStore.setLabItems(items.value);
  patientStore.clearRequestDraft('lab');
  await patientStore.refreshSelectedPatient();
  await loadSubmittedOrders();
  ElMessage.success('检验申请已提交');
}

async function markSkipLab() {
  if (!patient.value || !medicalRecord.value?.recordId || !medicalRecord.value?.visitId) {
    ElMessage.warning('当前患者缺少病历上下文');
    return;
  }

  const promptResult = await ElMessageBox.prompt(
    '如有需要可填写原因，系统会记录到病历备注中。',
    '确认本次无需检验',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：已有近期外院结果，暂不重复送检'
    }
  ).catch(() => null);

  if (!promptResult) return;

  const reason = promptResult.value || '';
  await skipLab({
    patientId: medicalRecord.value.patientId,
    visitId: medicalRecord.value.visitId,
    recordId: medicalRecord.value.recordId,
    reason
  });
  patientStore.clearRequestDraft('lab');
  patientStore.setLabItems([]);
  await patientStore.refreshSelectedPatient();
  ElMessage.success('已记录本次无需检验');
}

loadSubmittedOrders();
</script>

<template>
  <section v-if="patient">
    <PatientBanner :patient="patient" />
    <EncounterTabs />
    <PageHeader title="检验申请" description="按照同一套 HIS 风格自定义设计，保持简洁可扩展。">
      <el-tag effect="light" type="success">检验申请单</el-tag>
    </PageHeader>

    <div class="request-workspace-grid">
      <el-card class="panel request-order-panel lab-order-panel">
        <div class="amount-tag">项目金额：{{ total.toFixed(2) }} 元</div>
        <div class="section-toolbar">
          <span class="hint">检验编码 / 检验名称 / 检验规格 / 单价 / 费用分类</span>
          <el-space :size="8">
            <el-button size="small" @click="markSkipLab">本次无需检验</el-button>
            <el-button size="small" :disabled="selectedItems.length === 0" @click="removeSelected">删除</el-button>
            <el-button size="small" type="primary" plain @click="openDialog">增加</el-button>
          </el-space>
        </div>

        <el-table :data="items" @selection-change="selectedItems = $event" stripe border height="320" class="dialog-table">
          <el-table-column type="selection" width="48" />
          <el-table-column prop="code" label="检验编码" min-width="110" />
          <el-table-column prop="name" label="检验名称" min-width="160" />
          <el-table-column prop="spec" label="检验规格" min-width="120" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="feeType" label="费用分类" min-width="120" />
          <el-table-column label="操作" width="90">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="removeItem($index)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="empty-hint lab-empty-hint">
              <div class="lab-empty-title">本次尚未添加检验项目</div>
              <div class="lab-empty-text">先从项目库选择需要的检验项目，再补充申请信息。</div>
            </div>
          </template>
        </el-table>
      </el-card>

      <el-card class="panel request-form-panel lab-form-panel">
        <template #header>检验申请单信息</template>
        <div class="lab-form-header">
          <div class="lab-form-title">当前申请单</div>
          <div class="lab-form-tags">
            <el-tag effect="light">优先级：{{ form.priority }}</el-tag>
            <el-tag effect="light" type="warning">采样方式：{{ form.collectionWay }}</el-tag>
          </div>
        </div>

        <el-form label-position="top" :model="form" class="lab-compact-form">
          <el-form-item label="检验目的">
            <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请输入检验目的，例如：评估炎症指标、排查感染" />
          </el-form-item>

          <div class="lab-form-inline">
            <el-form-item label="标本类型">
              <el-select v-model="form.specimen" placeholder="请选择标本类型" style="width:100%">
                <el-option label="静脉血" value="静脉血" />
                <el-option label="尿液" value="尿液" />
                <el-option label="粪便" value="粪便" />
                <el-option label="痰液" value="痰液" />
              </el-select>
            </el-form-item>

            <el-form-item label="优先级">
              <el-radio-group v-model="form.priority">
                <el-radio-button label="普通" />
                <el-radio-button label="加急" />
              </el-radio-group>
            </el-form-item>
          </div>

          <el-form-item label="采样方式">
            <el-select v-model="form.collectionWay" placeholder="请选择采样方式" style="width:100%">
              <el-option label="门诊采样" value="门诊采样" />
              <el-option label="床旁采样" value="床旁采样" />
              <el-option label="自行留取" value="自行留取" />
            </el-select>
          </el-form-item>

          <el-form-item label="备注">
            <el-input v-model="form.notes" type="textarea" :rows="4" placeholder="请输入备注，例如：空腹、立即送检、特殊采样要求" />
          </el-form-item>
        </el-form>

        <div class="lab-form-note">
          <div>1. 采样前请核对患者身份和标本类型。</div>
          <div>2. 如需空腹检验，请在备注中明确标识。</div>
          <div>3. 可根据临床需要调整优先级和采样方式。</div>
        </div>

        <div class="bottom-actions request-form-actions">
          <el-button type="primary" @click="submitRequest">提交申请</el-button>
          <el-button @click="clearRequest">清空</el-button>
        </div>
      </el-card>

      <el-card class="panel request-records-panel">
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
          <template #empty><div class="empty-hint">当前就诊暂无已提交检验申请</div></template>
        </el-table>
      </el-card>
    </div>

    <LibraryDialog
      v-model="dialogVisible"
      v-model:keyword="keyword"
      title="新增检验项目"
      code-label="检验编码"
      name-label="检验名称"
      :items="libraryItems"
      @search="loadLibrary"
      @select="addItem"
    />

    <RequestOrderDetailDialog
      v-model="detailVisible"
      title="检验申请详情"
      :order="activeSubmittedOrder"
    />
  </section>

  <div v-else class="empty-box">
    <el-empty description="请先在患者查看中选择一个患者" />
  </div>
</template>
