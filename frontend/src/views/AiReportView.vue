<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '../components/PageHeader.vue';
import { fetchExamLabReports, fetchPatients } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';

const router = useRouter();
const patientStore = usePatientStore();
const loading = ref(false);
const patients = ref([]);
const search = reactive({ medicalNo: '', name: '' });

const visiblePatients = computed(() => patients.value);

async function loadPatients() {
  loading.value = true;
  try {
    const res = await fetchPatients({
      ...search,
      visitStatus: '报告待回阅'
    });
    patients.value = res.data.list;
  } finally {
    loading.value = false;
  }
}

function resetSearch() {
  search.medicalNo = '';
  search.name = '';
  loadPatients();
}

async function viewReport(row) {
  await patientStore.selectPatient(row);
  const context = patientStore.state.medicalRecord;
  const visit = patientStore.state.visit;
  const res = await fetchExamLabReports({
    patientId: context?.patientId || row.patientId,
    visitId: context?.visitId || visit?.visitId
  });
  patientStore.setExamLabReports(res.data.list);
  patientStore.setActiveExamLabReport(res.data.list[0] || null);
  router.push('/ai-report-detail');
}

onMounted(loadPatients);
</script>

<template>
  <section>
    <PageHeader title="检查/检验结果" description="只展示当前需要医生回阅报告的患者。">
      <el-tag effect="light" type="success">结果查看</el-tag>
    </PageHeader>

    <el-card class="panel">
      <template #header>患者列表</template>
      <div class="search-grid">
        <el-input v-model="search.medicalNo" clearable placeholder="患者病历号" />
        <el-input v-model="search.name" clearable placeholder="患者姓名" />
        <div class="search-actions">
          <el-button type="primary" @click="loadPatients">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
      <el-table :data="visiblePatients" v-loading="loading" border stripe height="430">
        <el-table-column type="index" label="编号" width="70" />
        <el-table-column prop="id" label="患者病历号" min-width="120" />
        <el-table-column prop="name" label="患者姓名" min-width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="viewReport(row)">回阅报告</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>
