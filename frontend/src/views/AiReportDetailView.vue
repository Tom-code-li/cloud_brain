<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import EncounterTabs from '../components/EncounterTabs.vue';
import { fetchExamLabReports, fetchOutpatientAiSuggestion, markExamLabReportReviewed } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';
import { buildAiPanelSections, buildDoctorOpinionText, requireAiSuccessData } from '../utils/aiAssistant.js';

const router = useRouter();
const patientStore = usePatientStore();
const patient = computed(() => patientStore.state.activePatient);
const medicalRecord = computed(() => patientStore.state.medicalRecord);
const reports = computed(() => patientStore.state.examLabReports);
const activeReport = computed(() => patientStore.state.activeExamLabReport);
const aiDiagnosisResult = computed(() => patientStore.state.aiDiagnosisResult);
const aiDoctorOpinion = computed(() => patientStore.state.aiDoctorOpinion);
const aiPrescriptionSuggestions = computed(() => patientStore.state.aiPrescriptionSuggestions);
const aiRiskFlags = computed(() => patientStore.state.aiRiskFlags);
const aiSections = computed(() => buildAiPanelSections({
  diagnosisDraft: aiDiagnosisResult.value,
  planDraft: patientStore.state.aiPlanDraft,
  drugSuggestions: aiPrescriptionSuggestions.value,
  riskFlags: aiRiskFlags.value
}));
const diagnosisOpinion = ref('');
const aiSummary = ref('点击“生成AI建议”后，将基于真实报告生成 AI 诊断与处置建议。');
const aiLoading = ref(false);
const reportLoading = ref(false);

const activeExamFeatures = computed(() => activeReport.value?.examFeatures || []);
const activeLabResultItems = computed(() => activeReport.value?.labResultItems || []);

async function loadReports() {
  if (!patient.value || !medicalRecord.value) return;
  reportLoading.value = true;
  try {
    const res = await fetchExamLabReports({
      patientId: medicalRecord.value.patientId,
      visitId: medicalRecord.value.visitId
    });
    patientStore.setExamLabReports(res.data.list);
    if (!activeReport.value && res.data.list.length > 0) {
      patientStore.setActiveExamLabReport(res.data.list[0]);
    }
  } finally {
    reportLoading.value = false;
  }
}

function selectReport(report) {
  patientStore.setActiveExamLabReport(report);
}

async function generateAiSuggestion() {
  if (!patient.value || !medicalRecord.value) return;
  if (!activeReport.value) {
    ElMessage.warning('暂无真实检查/检验报告，不能生成 AI 分析');
    return;
  }
  aiLoading.value = true;
  try {
    const res = await fetchOutpatientAiSuggestion({
      sceneCode: 'OUTPATIENT_POST_REPORT_SUGGESTION',
      patientId: medicalRecord.value.patientId,
      visitId: medicalRecord.value.visitId,
      recordId: medicalRecord.value.recordId,
      orderId: activeReport.value.orderId,
      reportId: activeReport.value.reportId
    });
    const suggestion = requireAiSuccessData(res);
    patientStore.setAiDiagnosisResult(suggestion.diagnosisDraft || diagnosisOpinion.value);
    patientStore.setAiDoctorOpinion(buildDoctorOpinionText(suggestion));
    patientStore.setAiReportSummary(suggestion.reportSummary || '');
    patientStore.setAiPlanDraft(suggestion.planDraft || '');
    patientStore.setAiPrescriptionSuggestions(suggestion.drugSuggestions || []);
    patientStore.setAiRiskFlags(suggestion.riskFlags || []);
    diagnosisOpinion.value = suggestion.diagnosisDraft || diagnosisOpinion.value;
    aiSummary.value = suggestion.reportSummary || aiSummary.value;
    ElMessage.success('AI 建议已生成');
  } catch (error) {
    ElMessage.error(error?.message || '报告后 AI 建议生成失败');
    return;
  } finally {
    aiLoading.value = false;
  }
}

async function goToDiagnosisDetail() {
  if (!aiDiagnosisResult.value) {
    ElMessage.warning('请先生成 AI 建议，再进入确诊页面');
    return;
  }
  if (activeReport.value) {
    await markExamLabReportReviewed({
      patientId: medicalRecord.value.patientId,
      visitId: medicalRecord.value.visitId,
      reportId: activeReport.value.reportId
    });
    patientStore.setActiveExamLabReport({
      ...activeReport.value,
      status: '已回阅'
    });
    await patientStore.refreshSelectedPatient({ preserveAi: true, preserveReports: true });
  }
  router.push('/diagnosis-detail');
}

onMounted(loadReports);
</script>

<template>
  <section v-if="patient">
    <div class="ai-report-page">
      <div class="patient-banner ai-report-banner">
        <span class="label">患者信息：</span>
        <span class="chip">姓名：{{ patient.name }}</span>
        <span class="chip">病历号：{{ patient.id }}</span>
        <span class="chip">年龄：{{ patient.age }}</span>
        <span class="chip">性别：{{ patient.gender }}</span>
      </div>
      <EncounterTabs />

      <div class="ai-detail-old">
        <div class="ai-detail-topbar">
          <div class="ai-detail-title">AI诊断分析报告详情</div>
          <div class="ai-detail-no">报告编号: {{ activeReport?.reportNo || '暂无报告' }}</div>
        </div>

        <div class="ai-detail-grid">
          <div class="ai-detail-left" v-loading="reportLoading">
            <div class="ai-scroll-pane">
              <el-empty v-if="!reports.length" description="暂无已发布的检查/检验报告" />
              <template v-else>
                <div class="ai-card">
                  <div class="ai-card-head">
                    报告列表
                  </div>
                  <div class="ai-card-body ai-report-list">
                    <div
                      v-for="report in reports"
                      :key="report.reportId"
                      class="ai-report-list-item"
                      :class="{ active: activeReport?.reportId === report.reportId }"
                      @click="selectReport(report)"
                    >
                      <span>{{ report.itemName || report.reportType || '检查/检验报告' }}</span>
                      <el-tag size="small" effect="plain">{{ report.status }}</el-tag>
                    </div>
                  </div>
                </div>

                <div v-if="activeReport" class="ai-card">
                  <div class="ai-card-head">检查结果</div>
                  <div class="ai-card-body">
                    <div class="report-block">
                      <strong>报告号：</strong>{{ activeReport.reportNo || '-' }}
                    </div>
                    <div class="report-block">
                      <strong>结果摘要：</strong>{{ activeReport.resultSummary || '无' }}
                    </div>
                    <div class="report-block">
                      <strong>所见：</strong>{{ activeReport.findings || '无' }}
                    </div>
                    <div class="report-block">
                      <strong>结论：</strong>{{ activeReport.conclusion || '无' }}
                    </div>
                    <div v-if="activeReport.doctorReview" class="report-block">
                      <strong>医生复核：</strong>{{ activeReport.doctorReview }}
                    </div>

                    <div class="sub-title">结构化检查结果</div>
                    <el-table :data="activeExamFeatures" stripe border class="dialog-table">
                      <el-table-column prop="featureName" label="项目" min-width="160" />
                      <el-table-column prop="featureValue" label="结果" min-width="180" />
                      <el-table-column prop="unit" label="单位" width="100" />
                      <el-table-column prop="abnormalFlag" label="标志" width="100" />
                      <template #empty><div class="empty-hint">暂无结构化检查结果</div></template>
                    </el-table>
                  </div>
                </div>

                <div v-if="activeReport" class="ai-card">
                  <div class="ai-card-head">检验结果</div>
                  <div class="ai-card-body">
                    <el-table :data="activeLabResultItems" stripe border class="dialog-table">
                      <el-table-column prop="indicatorName" label="指标名称" min-width="160" />
                      <el-table-column prop="resultValue" label="结果值" width="120" />
                      <el-table-column prop="unit" label="单位" width="100" />
                      <el-table-column prop="referenceRange" label="参考范围" min-width="140" />
                      <el-table-column prop="abnormalFlag" label="标志" width="100" />
                      <template #empty><div class="empty-hint">暂无检验结果</div></template>
                    </el-table>
                  </div>
                </div>
              </template>
            </div>
          </div>

          <div class="ai-detail-right">
            <div class="ai-assistant-old">
              <div class="ai-assistant-head">AI 智能辅助诊断建议</div>
              <div class="ai-assistant-body">
                <div class="ai-scroll-pane ai-scroll-pane-right">
                  <div class="ai-summary-card">
                    <div class="ai-summary-top">
                      <div class="ai-summary-title">基于真实报告分析</div>
                      <div class="ai-pill success-pill">{{ activeReport ? '已选择报告' : '暂无报告' }}</div>
                    </div>
                    <div class="ai-summary-text">
                      {{ activeReport ? aiSummary : '请先选择一份已发布报告。' }}
                    </div>
                  </div>
                  <div class="ai-panel-grid">
                    <div
                      v-for="section in aiSections"
                      :key="section.key"
                      class="ai-panel-card"
                    >
                      <div class="ai-panel-card-title">{{ section.title }}</div>
                      <div class="ai-panel-card-content">{{ section.content }}</div>
                    </div>
                    <div v-if="!aiSections.length" class="ai-panel-empty">
                      点击“生成AI建议”后，这里会展示 AI 诊断、处置建议、处方建议和注意事项。
                    </div>
                  </div>
                  <div class="ai-opinion-block">
                    <div class="ai-opinion-head">
                      <span class="ai-opinion-title">医生最终诊断意见</span>
                      <span class="ai-opinion-note">{{ aiDoctorOpinion ? '已同步 AI 建议' : '可手工修改' }}</span>
                    </div>
                    <el-input v-model="diagnosisOpinion" class="ai-diagnosis-editor" type="textarea" :rows="12" />
                  </div>
                </div>
              </div>
              <div class="ai-actions">
                <el-button :loading="aiLoading" :disabled="!activeReport" @click="generateAiSuggestion">生成AI建议</el-button>
                <el-button type="primary" :disabled="!aiDiagnosisResult" @click="goToDiagnosisDetail">进入确诊</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <div v-else class="empty-box">
    <el-empty description="请先在患者列表中选择一个患者" />
  </div>
</template>
