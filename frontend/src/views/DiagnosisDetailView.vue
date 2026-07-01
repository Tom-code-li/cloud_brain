<script setup>
import { computed, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import EncounterTabs from '../components/EncounterTabs.vue';
import { saveFinalDiagnosis } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';

const router = useRouter();
const patientStore = usePatientStore();
const patient = computed(() => patientStore.state.activePatient);
const form = reactive({
  result: '',
  opinion: ''
});

watch(
  () => patientStore.state.aiDiagnosisResult,
  (value) => {
    if (value) {
      form.result = value;
    }
  },
  { immediate: true }
);

watch(
  () => patientStore.state.aiDoctorOpinion,
  (value) => {
    if (value) {
      form.opinion = value;
    }
  },
  { immediate: true }
);

async function submitDiagnosis() {
  if (!patient.value || !patientStore.state.medicalRecord?.visitId || !patientStore.state.medicalRecord?.recordId) {
    ElMessage.warning('当前患者缺少可提交的病历上下文');
    return;
  }
  await saveFinalDiagnosis({
    patientId: patientStore.state.medicalRecord.patientId,
    visitId: patientStore.state.medicalRecord.visitId,
    recordId: patientStore.state.medicalRecord.recordId,
    finalDiagnosis: form.result,
    finalOpinion: form.opinion
  });
  await patientStore.refreshSelectedPatient({ preserveAi: true });
  patientStore.setMedicalRecord({
    ...patientStore.state.medicalRecord,
    finalDiagnosis: form.result,
    finalOpinion: form.opinion,
    status: '已完成'
  });
  ElMessage.success('确诊结果已保存');
  router.push('/prescription-detail');
}

function resetDiagnosis() {
  form.result = '';
  form.opinion = '';
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

    <h2 class="legacy-title">门诊确诊</h2>
    <div class="legacy-layout">
      <table class="legacy-diagnosis-table">
        <tr>
          <td class="legacy-label">诊断结果：</td>
          <td class="legacy-input"><el-input v-model="form.result" type="textarea" placeholder="输入诊断结果判断" /></td>
        </tr>
        <tr>
          <td class="legacy-label">处理意见：</td>
          <td class="legacy-input"><el-input v-model="form.opinion" type="textarea" placeholder="输入治疗意见" /></td>
        </tr>
      </table>
      <div class="bottom-actions">
        <el-button type="primary" @click="submitDiagnosis">确诊提交</el-button>
        <el-button @click="resetDiagnosis">重置输入</el-button>
      </div>
    </div>
  </section>

  <div v-else class="empty-box">
    <el-empty description="请先在患者列表中选择一个患者" />
  </div>
</template>
