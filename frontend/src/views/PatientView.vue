<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import PageHeader from '../components/PageHeader.vue';
import { fetchPatients, startEncounter } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';

const router = useRouter();
const patientStore = usePatientStore();

const loading = ref(false);
const search = reactive({
  medicalNo: '',
  name: ''
});
const page = reactive({
  current: 1,
  size: 4
});
const patients = ref([]);

const pagedPatients = computed(() => {
  const start = (page.current - 1) * page.size;
  return patients.value.slice(start, start + page.size);
});

async function loadPatients() {
  loading.value = true;
  page.current = 1;
  try {
    const res = await fetchPatients({
      medicalNo: search.medicalNo,
      name: search.name,
      visitStatus: '待接诊'
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

async function startPatientEncounter(row) {
  await startEncounter({
    patientId: row.patientId,
    visitId: row.visitId
  });
  await patientStore.selectPatient(row);
  ElMessage.success('已接诊，进入病历首页');
  router.push('/medical-home');
}

onMounted(loadPatients);
</script>

<template>
  <section>
    <PageHeader title="患者查看" description="用于患者叫号和接诊，只展示当前等待就诊的患者。">
      <el-tag effect="light" type="success">待接诊 {{ patients.length }} 人</el-tag>
    </PageHeader>

    <el-card class="panel">
      <div class="page-block-title">患者叫号</div>
      <div class="search-grid">
        <el-input v-model="search.medicalNo" clearable placeholder="请输入患者病历号" />
        <el-input v-model="search.name" clearable placeholder="请输入患者姓名" />
        <div class="search-actions">
          <el-button type="primary" @click="loadPatients">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="panel">
      <el-table :data="pagedPatients" v-loading="loading" stripe border height="420">
        <el-table-column type="index" label="编号" width="70" />
        <el-table-column prop="patientName" label="患者姓名" min-width="120" />
        <el-table-column prop="patientNo" label="患者病历号" min-width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column label="患者状态" width="120">
          <template #default="{ row }">
            <el-tag effect="light" type="warning">
              {{ row.visitStatus || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registeredAt" label="挂号时间" min-width="180" />
        <el-table-column prop="queueNo" label="排队号" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space :size="6">
              <el-button size="small" type="primary" plain @click="startPatientEncounter(row)">接诊</el-button>
            </el-space>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-hint">未找到符合条件的患者</div>
        </template>
      </el-table>

      <div class="pager-row">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          layout="prev, pager, next"
          :total="patients.length"
          :pager-count="5"
        />
      </div>
    </el-card>
  </section>
</template>
