<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import EncounterTabs from '../components/EncounterTabs.vue';
import PageHeader from '../components/PageHeader.vue';
import PatientBanner from '../components/PatientBanner.vue';
import { fetchOutpatientAiSuggestion, saveMedicalRecord } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';
import { clone } from '../utils/outpatientCore.js';
import { buildAiAssistantText, buildExamSuggestionText, pickDiagnosisDraft, requireAiSuccessData } from '../utils/aiAssistant.js';
import { createEmptyMedicalRecord } from '../mock/adapter.js';
import { medicalHomeDiagnosisLayout } from '../config/medicalHome.js';

const patientStore = usePatientStore();
const patient = computed(() => patientStore.state.activePatient);
const record = reactive(createEmptyMedicalRecord());
const aiDraft = ref('点击“生成初诊建议”后，这里会展示 DeepSeek 返回的完整诊断建议。');
const aiDiagnosisDraft = ref('');
const aiLoading = ref(false);

watch(
  () => patientStore.state.medicalRecord,
  (value) => {
    Object.assign(record, createEmptyMedicalRecord(), clone(value || createEmptyMedicalRecord()));
    aiDraft.value = value?.diagnosisText || '点击“生成初诊建议”后，这里会展示 DeepSeek 返回的完整诊断建议。';
    aiDiagnosisDraft.value = value?.diagnosisText || '';
  },
  { immediate: true }
);

function useAiDraft() {
  record.diagnosisText = pickDiagnosisDraft({ diagnosisDraft: aiDiagnosisDraft.value }, record.diagnosisText);
  ElMessage.success('已填入 AI 建议');
}

async function generateInitialSuggestion() {
  if (!patient.value) {
    ElMessage.warning('请先选择患者');
    return;
  }
  aiLoading.value = true;
  try {
    const res = await fetchOutpatientAiSuggestion({
      sceneCode: 'OUTPATIENT_INITIAL_SUGGESTION',
      patientId: record.patientId || patient.value.id,
      visitId: record.visitId,
      recordId: record.recordId,
      currentChiefComplaint: record.chiefComplaint,
      currentPresentIllness: record.presentIllness,
      currentPhysicalExam: record.physicalExam,
      currentDiagnosis: record.diagnosisText
    });
    const suggestion = requireAiSuccessData(res);
    aiDiagnosisDraft.value = suggestion.diagnosisDraft || '';
    aiDraft.value = buildAiAssistantText(suggestion) || aiDraft.value;
    const examSuggestionText = buildExamSuggestionText(suggestion);
    if (examSuggestionText) {
      record.examSuggestion = examSuggestionText;
    }
    if (suggestion.diagnosisDraft) {
      record.diagnosisText = suggestion.diagnosisDraft;
    }
    ElMessage.success('已生成初诊建议');
  } catch (error) {
    ElMessage.error(error?.message || 'AI 初诊建议生成失败');
  } finally {
    aiLoading.value = false;
  }
}

function resetForm() {
  const currentRecord = patientStore.state.medicalRecord || {};
  Object.assign(record, createEmptyMedicalRecord(), {
    patientId: currentRecord.patientId || patient.value?.patientId || patient.value?.id || null,
    visitId: currentRecord.visitId || patientStore.state.visit?.visitId || null,
    recordId: currentRecord.recordId || null,
    status: currentRecord.status || '初诊暂存',
    finalDiagnosis: currentRecord.finalDiagnosis || '',
    finalOpinion: currentRecord.finalOpinion || ''
  });
  aiDraft.value = '点击“生成初诊建议”后，这里会展示 DeepSeek 返回的完整诊断建议。';
}

async function saveRecord() {
  if (!patient.value) {
    ElMessage.warning('请先选择患者');
    return;
  }

  await saveMedicalRecord({
    recordId: record.recordId || null,
    patientId: record.patientId || patient.value.patientId || patient.value.id,
    visitId: record.visitId || patientStore.state.visit?.visitId,
    chiefComplaint: record.chiefComplaint,
    presentIllness: record.presentIllness,
    currentTreatment: record.currentTreatment,
    pastHistory: record.pastHistory,
    allergyHistory: record.allergyHistory,
    physicalExam: record.physicalExam,
    auxiliaryExam: record.auxiliaryExam || '',
    diagnosis: record.diagnosisText,
    treatmentAdvice: record.examSuggestion,
    doctorNote: record.notes,
    status: record.status || '初诊暂存'
  });
  patientStore.setMedicalRecord(record);
  ElMessage.success('病历首页已保存');
}
</script>

<template>
  <section v-if="patient">
    <PatientBanner :patient="patient" />
    <EncounterTabs />
    <PageHeader title="病历首页" description="诊断改为直接文本输入，右侧保留一块紧凑的 AI 助手区域。">
      <el-tag effect="light" type="primary">门诊医生工作站</el-tag>
    </PageHeader>

    <div class="medical-home-grid">
      <el-card class="panel medical-card medical-editor-card">
        <div class="medical-form">
          <div class="medical-row">
            <div class="medical-label">主诉</div>
            <div class="medical-field"><el-input v-model="record.chiefComplaint" type="textarea" :rows="2" placeholder="请输入主诉" /></div>
          </div>
          <div class="medical-row">
            <div class="medical-label">现病史</div>
            <div class="medical-field"><el-input v-model="record.presentIllness" type="textarea" :rows="2" placeholder="请输入现病史" /></div>
          </div>
          <div class="medical-row">
            <div class="medical-label">现病治疗情况</div>
            <div class="medical-field"><el-input v-model="record.currentTreatment" type="textarea" :rows="2" placeholder="请输入现病治疗情况" /></div>
          </div>
          <div class="medical-row">
            <div class="medical-label">既往史</div>
            <div class="medical-field"><el-input v-model="record.pastHistory" type="textarea" :rows="2" placeholder="请输入既往史" /></div>
          </div>
          <div class="medical-row">
            <div class="medical-label">过敏史</div>
            <div class="medical-field"><el-input v-model="record.allergyHistory" type="textarea" :rows="2" placeholder="请输入过敏史" /></div>
          </div>
          <div class="medical-row">
            <div class="medical-label">体格检查</div>
            <div class="medical-field"><el-input v-model="record.physicalExam" type="textarea" :rows="2" placeholder="请输入体格检查信息" /></div>
          </div>
          <div class="medical-row diagnosis-wide-row">
            <div class="medical-label">诊断</div>
            <div class="medical-field diagnosis-editor">
              <el-input
                v-model="record.diagnosisText"
                type="textarea"
                :rows="medicalHomeDiagnosisLayout.editor.rows"
                :placeholder="medicalHomeDiagnosisLayout.editor.placeholder"
              />
            </div>
          </div>
          <div class="medical-row">
            <div class="medical-label">检查检验建议</div>
            <div class="medical-field"><el-input v-model="record.examSuggestion" type="textarea" :rows="2" placeholder="请输入检查/检验建议" /></div>
          </div>
          <div class="medical-row">
            <div class="medical-label">注意事项</div>
            <div class="medical-field"><el-input v-model="record.notes" type="textarea" :rows="2" placeholder="请输入注意事项" /></div>
          </div>
        </div>

        <div class="bottom-actions">
          <el-button type="primary" @click="saveRecord">保存</el-button>
          <el-button @click="resetForm">清空</el-button>
        </div>
      </el-card>

      <el-card class="panel ai-assistant-card">
        <template #header>{{ medicalHomeDiagnosisLayout.assistant.title }}</template>
        <div class="ai-assistant">
          <div class="medical-home-ai-hero">
            <div class="medical-home-ai-title">AI 诊断提示</div>
            <div class="medical-home-ai-subtitle">基于主诉、现病史、体格检查和过敏信息生成诊断建议，并同步检查/检验建议。</div>
          </div>
          <div class="medical-home-ai-chip-row">
            <span v-for="tip in medicalHomeDiagnosisLayout.assistant.tips" :key="tip" class="medical-home-ai-chip">{{ tip }}</span>
          </div>
          <div class="medical-home-ai-editor-shell">
            <div class="medical-home-ai-editor-label">AI 完整建议</div>
            <el-input v-model="aiDraft" class="medical-home-ai-editor" type="textarea" :rows="16" />
          </div>
          <div class="bottom-actions compact-actions medical-home-ai-actions">
            <el-button :loading="aiLoading" @click="generateInitialSuggestion">生成初诊建议</el-button>
            <el-button type="primary" plain @click="useAiDraft">填入诊断</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </section>

  <div v-else class="empty-box">
    <el-empty description="请先在患者查看中选择一个患者" />
  </div>
</template>
