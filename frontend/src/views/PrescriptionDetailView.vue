<script setup>
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import EncounterTabs from '../components/EncounterTabs.vue';
import LibraryDialog from '../components/LibraryDialog.vue';
import { fetchDrugs, submitPrescription as submitPrescriptionApi } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';
import { appendUniqueByCode, sumItemPrices } from '../utils/outpatientCore.js';

const patientStore = usePatientStore();
const patient = computed(() => patientStore.state.activePatient);
const medicalRecord = computed(() => patientStore.state.medicalRecord);
const drugs = ref([]);
const selectedDrugs = ref([]);
const dialogVisible = ref(false);
const keyword = ref('');
const libraryItems = ref([]);

const total = computed(() => sumItemPrices(drugs.value));

async function loadDrugLibrary() {
  const res = await fetchDrugs(keyword.value);
  libraryItems.value = res.data.list;
}

async function openDialog() {
  dialogVisible.value = true;
  await loadDrugLibrary();
}

function addDrug(row) {
  const next = appendUniqueByCode(drugs.value, {
    ...row,
    dosage: '',
    frequency: '',
    usageMethod: '',
    days: 1,
    count: 1
  });
  if (next.length === drugs.value.length) {
    ElMessage.warning('该药品已存在');
    return;
  }
  drugs.value = next;
}

function removeDrug(index) {
  drugs.value.splice(index, 1);
}

function clearDrugs() {
  drugs.value = [];
  selectedDrugs.value = [];
}

async function submitPrescription() {
  if (!patient.value || !medicalRecord.value?.visitId) {
    ElMessage.warning('请先选择患者');
    return;
  }
  if (!drugs.value.length) {
    ElMessage.warning('请先添加药品');
    return;
  }
  await submitPrescriptionApi({
    patientId: medicalRecord.value.patientId,
    visitId: medicalRecord.value.visitId,
    recordId: medicalRecord.value.recordId,
    diagnosis: medicalRecord.value.diagnosisText,
    usageNote: '门诊处方',
    items: drugs.value.map((item) => ({
      drugId: item.drugId || item.id,
      code: item.code,
      name: item.name,
      spec: item.spec,
      price: item.price,
      quantity: item.count,
      dosage: item.dosage,
      frequency: item.frequency,
      usageMethod: item.usageMethod,
      days: item.days
    }))
  });
  ElMessage.success('处方已开立');
}
</script>

<template>
  <section v-if="patient">
    <div class="patient-banner">
      <span class="label">患者信息：</span>
      <span class="chip">姓名：{{ patient.name }}</span>
      <span class="chip">病历号：{{ patient.id }}</span>
      <span class="chip">年龄：{{ patient.age }}</span>
      <span class="chip">性别：{{ patient.gender }}</span>
    </div>
    <EncounterTabs />

    <h2 class="legacy-title">开设处方</h2>
    <div class="legacy-layout">
      <div class="legacy-prescription-head">
        <div class="amount-tag">处方金额：{{ total.toFixed(2) }} 元</div>
      </div>
      <div class="legacy-toolbar">
        <button class="legacy-link-btn" :disabled="selectedDrugs.length === 0">删除</button>
        <button class="legacy-link-btn primary" @click="openDialog">新增</button>
      </div>
      <table class="legacy-table">
        <thead>
          <tr>
            <th><input type="checkbox" /></th>
            <th>药品名称</th>
            <th>药品规格</th>
            <th>单价</th>
            <th>剂量</th>
            <th>频次</th>
            <th>用法</th>
            <th>天数</th>
            <th>数量</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in drugs" :key="row.code">
            <td><input type="checkbox" /></td>
            <td>{{ row.name }}</td>
            <td>{{ row.spec }}</td>
            <td>{{ Number(row.price).toFixed(2) }}</td>
            <td><el-input v-model="row.dosage" size="small" placeholder="剂量" /></td>
            <td><el-input v-model="row.frequency" size="small" placeholder="频次" /></td>
            <td><el-input v-model="row.usageMethod" size="small" placeholder="用法" /></td>
            <td><el-input-number v-model="row.days" :min="1" size="small" /></td>
            <td><el-input-number v-model="row.count" :min="1" size="small" /></td>
            <td><button class="legacy-link-btn danger" @click="removeDrug(index)">删除</button></td>
          </tr>
        </tbody>
      </table>
      <div class="bottom-actions">
        <el-button type="primary" @click="submitPrescription">开立处方</el-button>
        <el-button @click="clearDrugs">重置处方</el-button>
      </div>
    </div>

    <LibraryDialog
      v-model="dialogVisible"
      v-model:keyword="keyword"
      title="添加药品"
      code-label="药品编码"
      name-label="药品名称"
      :items="libraryItems"
      @search="loadDrugLibrary"
      @select="addDrug"
    />
  </section>

  <div v-else class="empty-box">
    <el-empty description="请先在患者列表中选择一个患者" />
  </div>
</template>
