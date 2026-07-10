import { http } from './http.js';

function normalizePatient(row) {
  return {
    ...row,
    id: row.patientNo,
    name: row.patientName,
    medicalNo: row.patientNo,
    status: row.visitStatus || row.status,
    registeredAt: row.registeredAt || null
  };
}

function normalizeListResponse(res, normalizeItem = (item) => item) {
  const source = Array.isArray(res.data?.data) ? res.data.data : (res.data?.list || []);
  return {
    ...res,
    data: {
      ...res.data,
      list: source.map(normalizeItem)
    }
  };
}

export async function fetchPatients(params) {
  const res = await http.get('/outpatient/patients', {
    params: {
      patientNo: params?.medicalNo,
      patientName: params?.name,
      visitStatus: params?.visitStatus,
      visitGroup: params?.visitGroup
    }
  });

  return normalizeListResponse(res, normalizePatient);
}

export function fetchPatientContext(patientId) {
  return http.get(`/outpatient/patients/${patientId}/context`);
}

export function fetchDiagnoses(keyword = '') {
  return http.get('/diagnoses', { params: { keyword } });
}

export async function fetchExamItems(keyword = '') {
  const res = await http.get('/exam-items', { params: { keyword } });
  return normalizeListResponse(res);
}

export async function fetchLabItems(keyword = '') {
  const res = await http.get('/lab-items', { params: { keyword } });
  return normalizeListResponse(res);
}

export async function fetchDrugs(keyword = '') {
  const res = await http.get('/drugs', { params: { keyword } });
  return normalizeListResponse(res);
}

export async function fetchExamLabReports(params = {}) {
  const res = await http.get('/exam-lab-reports', { params });
  return normalizeListResponse(res);
}

export function markExamLabReportReviewed(payload) {
  return http.post('/exam-lab-reports/review', payload);
}

export async function fetchExamLabOrders(params = {}) {
  const res = await http.get('/exam-lab-orders', { params });
  return {
    ...res,
    data: {
      ...res.data,
      list: Array.isArray(res.data?.data) ? res.data.data : []
    }
  };
}

export async function fetchFeeOrders(params = {}) {
  const res = await http.get('/fee-orders', { params });
  return {
    ...res,
    data: {
      ...res.data,
      list: Array.isArray(res.data?.data) ? res.data.data : []
    }
  };
}

export function saveMedicalRecord(payload) {
  return http.post('/outpatient/medical-records', payload);
}

export function startEncounter(payload) {
  return http.post('/outpatient/start-encounter', payload);
}

export function saveFinalDiagnosis(payload) {
  return http.post('/outpatient/final-diagnosis', payload);
}

export function skipExam(payload) {
  return http.post('/outpatient/skip-exam', payload);
}

export function skipLab(payload) {
  return http.post('/outpatient/skip-lab', payload);
}

export function submitExamRequest(payload) {
  return http.post('/exam-request', payload);
}

export function submitLabRequest(payload) {
  return http.post('/lab-request', payload);
}

export function submitPrescription(payload) {
  return http.post('/prescriptions', payload);
}

export function fetchOutpatientAiSuggestion(payload) {
  return http.post('/ai/outpatient/suggestions', payload, {
    timeout: 90000
  });
}
