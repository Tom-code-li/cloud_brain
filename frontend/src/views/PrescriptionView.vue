<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '../components/PageHeader.vue';
import { fetchPatients } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';

const router = useRouter();
const patientStore = usePatientStore();
const loading = ref(false);
const patients = ref([]);
const search = reactive({ medicalNo: '', name: '' });

async function loadPatients() {
  loading.value = true;
  try {
    const res = await fetchPatients({
      ...search,
      visitStatus: '待处置'
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

async function openPrescription(row) {
  await patientStore.selectPatient(row);
  router.push('/prescription-detail');
}

onMounted(loadPatients);
</script>

<template>
  <section>
    <PageHeader title="开设处方" description="先选择患者，再进入处方编辑。">
      <el-tag effect="light" type="primary">处方金额</el-tag>
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
      <el-table :data="patients" v-loading="loading" border stripe height="430">
        <el-table-column type="index" label="编号" width="70" />
        <el-table-column prop="id" label="患者病历号" min-width="120" />
        <el-table-column prop="name" label="患者姓名" min-width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openPrescription(row)">开方</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>
