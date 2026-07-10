<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '../components/PageHeader.vue';
import { fetchPatients } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';

const router = useRouter();
const patientStore = usePatientStore();
const loading = ref(false);
const patients = ref([]);
const search = reactive({
  medicalNo: '',
  name: ''
});

const visiblePatients = computed(() => patients.value);

async function loadPatients() {
  loading.value = true;
  try {
    const res = await fetchPatients({
      ...search,
      visitGroup: 'ACTIVE'
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

async function openPatient(row) {
  await patientStore.selectPatient(row);
  router.push('/medical-home');
}

onMounted(loadPatients);
</script>

<template>
  <section>
    <PageHeader title="看诊记录" description="展示当前已经进入诊疗流程、但尚未结束的患者。">
      <el-tag effect="light">当前诊疗</el-tag>
    </PageHeader>

    <el-card class="panel">
      <div class="search-grid">
        <el-input v-model="search.medicalNo" clearable placeholder="患者病历号" />
        <el-input v-model="search.name" clearable placeholder="患者姓名" />
        <div class="search-actions">
          <el-button type="primary" @click="loadPatients">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="panel">
      <el-table :data="visiblePatients" v-loading="loading" border stripe height="430">
        <el-table-column type="index" label="编号" width="70" />
        <el-table-column prop="patientNo" label="患者病历号" min-width="120" />
        <el-table-column prop="patientName" label="患者姓名" min-width="120" />
        <el-table-column prop="idCard" label="患者身份证" min-width="180" />
        <el-table-column prop="visitStatus" label="患者状态" width="120" />
        <el-table-column prop="registeredAt" label="挂号时间" min-width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openPatient(row)">继续诊疗</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>
