import { reactive, readonly } from 'vue';
import { fetchPatientContext } from '../api/outpatient.js';
import { clone } from '../utils/outpatientCore.js';

const state = reactive({
  activePatient: null,
  registration: null,
  visit: null,
  medicalRecord: null,
  diagnoses: [],
  examOrderSummaries: [],
  labOrderSummaries: [],
  prescriptionSummaries: [],
  feeOrderSummaries: [],
  examItems: [],
  labItems: [],
  examLabReports: [],
  activeExamLabReport: null,
  aiDiagnosisResult: '',
  aiDoctorOpinion: '',
  aiReportSummary: '',
  aiPlanDraft: '',
  aiPrescriptionSuggestions: [],
  aiRiskFlags: []
});

function draftKey(kind, patientId, visitId) {
  return `his-${kind}-draft-${patientId || 'none'}-${visitId || 'none'}`;
}

function normalizeActivePatient(patient = {}) {
  const patientId = patient.patientId || patient.id || null;
  const patientNo = patient.patientNo || patient.id || '';
  const patientName = patient.patientName || patient.name || '';

  return {
    ...clone(patient),
    patientId,
    patientNo,
    patientName,
    id: patient.id || patientNo,
    name: patient.name || patientName,
    status: patient.status || patient.visitStatus || '',
    visitStatus: patient.visitStatus || patient.status || ''
  };
}

function buildMedicalRecordState(context = {}) {
  return {
    recordId: context.medicalRecord?.recordId || null,
    visitId: context.visit?.visitId || context.visitId || null,
    patientId: context.patientId || null,
    chiefComplaint: context.medicalRecord?.chiefComplaint || '',
    presentIllness: context.medicalRecord?.presentIllness || '',
    currentTreatment: context.medicalRecord?.currentTreatment || '',
    pastHistory: context.medicalRecord?.pastHistory || '',
    allergyHistory: context.medicalRecord?.allergyHistory || '',
    physicalExam: context.medicalRecord?.physicalExam || '',
    auxiliaryExam: context.medicalRecord?.auxiliaryExam || '',
    diagnosisText: context.medicalRecord?.diagnosis || '',
    examSuggestion: context.medicalRecord?.treatmentAdvice || '',
    notes: context.medicalRecord?.doctorNote || '',
    finalDiagnosis: context.medicalRecord?.finalDiagnosis || '',
    finalOpinion: context.medicalRecord?.finalOpinion || '',
    status: context.medicalRecord?.status || '初诊暂存'
  };
}

export function usePatientStore() {
  function clearAiSuggestion() {
    state.aiDiagnosisResult = '';
    state.aiDoctorOpinion = '';
    state.aiReportSummary = '';
    state.aiPlanDraft = '';
    state.aiPrescriptionSuggestions = [];
    state.aiRiskFlags = [];
  }

  async function hydrateSelectedPatient(patient, { preserveAi = false, preserveReports = false } = {}) {
    state.activePatient = normalizeActivePatient(patient);
    if (typeof window !== 'undefined') {
      window.sessionStorage.setItem('his-active-patient', JSON.stringify(state.activePatient));
    }
    const patientId = state.activePatient.patientId || state.activePatient.id;
    const res = await fetchPatientContext(patientId);
    const context = res.data.data;
    state.registration = clone(context.registration || null);
    state.visit = clone(context.visit || null);
    state.medicalRecord = buildMedicalRecordState(context);
    state.diagnoses = [];
    state.examOrderSummaries = clone(context.examOrders || []);
    state.labOrderSummaries = clone(context.labOrders || []);
    state.prescriptionSummaries = clone(context.prescriptions || []);
    state.feeOrderSummaries = clone(context.feeOrders || []);
    state.examItems = [];
    state.labItems = [];
    if (!preserveReports) {
      state.examLabReports = [];
      state.activeExamLabReport = null;
    }
    if (!preserveAi) {
      clearAiSuggestion();
    }
  }

  async function selectPatient(patient) {
    await hydrateSelectedPatient(patient);
  }

  async function refreshSelectedPatient(options = {}) {
    if (!state.activePatient) {
      return;
    }

    await hydrateSelectedPatient(state.activePatient, options);
  }

  function setMedicalRecord(record) {
    state.medicalRecord = clone(record);
  }

  function setDiagnoses(items) {
    state.diagnoses = clone(items);
  }

  function setExamItems(items) {
    state.examItems = clone(items);
  }

  function setLabItems(items) {
    state.labItems = clone(items);
  }

  function setVisit(visit) {
    state.visit = clone(visit);
  }

  function saveRequestDraft(kind, payload) {
    if (typeof window === 'undefined') return;
    const patientId = state.medicalRecord?.patientId || state.activePatient?.patientId;
    const visitId = state.medicalRecord?.visitId || state.visit?.visitId;
    window.sessionStorage.setItem(draftKey(kind, patientId, visitId), JSON.stringify(payload));
  }

  function loadRequestDraft(kind) {
    if (typeof window === 'undefined') return null;
    const patientId = state.medicalRecord?.patientId || state.activePatient?.patientId;
    const visitId = state.medicalRecord?.visitId || state.visit?.visitId;
    const raw = window.sessionStorage.getItem(draftKey(kind, patientId, visitId));
    return raw ? JSON.parse(raw) : null;
  }

  function clearRequestDraft(kind) {
    if (typeof window === 'undefined') return;
    const patientId = state.medicalRecord?.patientId || state.activePatient?.patientId;
    const visitId = state.medicalRecord?.visitId || state.visit?.visitId;
    window.sessionStorage.removeItem(draftKey(kind, patientId, visitId));
  }

  function setExamLabReports(items) {
    state.examLabReports = clone(items);
  }

  function setActiveExamLabReport(report) {
    state.activeExamLabReport = clone(report);
  }

  function setAiDiagnosisResult(value) {
    state.aiDiagnosisResult = value;
  }

  function setAiDoctorOpinion(value) {
    state.aiDoctorOpinion = value;
  }

  function setAiReportSummary(value) {
    state.aiReportSummary = value;
  }

  function setAiPlanDraft(value) {
    state.aiPlanDraft = value;
  }

  function setAiPrescriptionSuggestions(items) {
    state.aiPrescriptionSuggestions = clone(items || []);
  }

  function setAiRiskFlags(items) {
    state.aiRiskFlags = clone(items || []);
  }

  return {
    state: readonly(state),
    selectPatient,
    refreshSelectedPatient,
    clearAiSuggestion,
    setMedicalRecord,
    setDiagnoses,
    setExamItems,
    setLabItems,
    setVisit,
    saveRequestDraft,
    loadRequestDraft,
    clearRequestDraft,
    setExamLabReports,
    setActiveExamLabReport,
    setAiDiagnosisResult,
    setAiDoctorOpinion,
    setAiReportSummary,
    setAiPlanDraft,
    setAiPrescriptionSuggestions,
    setAiRiskFlags
  };
}
