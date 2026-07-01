import {
  diagnosisLibrary,
  drugLibrarySource,
  examLibrarySource,
  labLibrarySource,
  medicalRecords,
  patients,
  requestDefaults
} from './data.js';
import { clone, filterPatients } from '../utils/outpatientCore.js';

export const isLocalMockEnabled = import.meta.env.VITE_USE_LOCAL_MOCK !== 'false';

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const authAccounts = [
  {
    account: 'DOC2025001',
    password: '123456',
    token: 'demo-token-outpatient',
    roleCode: 'OUTPATIENT_DOCTOR',
    roleName: '门诊医生',
    workbenchRoute: '/patients',
    user: {
      username: 'DOC2025001',
      realName: '张仲景',
      doctorId: 1,
      userId: 1001
    }
  },
  {
    account: 'REG2025001',
    password: '123456',
    token: 'demo-token-registration',
    roleCode: 'REGISTRATION_DOCTOR',
    roleName: '挂号医生',
    workbenchRoute: '/registration/dashboard',
    user: {
      username: 'REG2025001',
      realName: '挂号窗口',
      doctorId: 9001,
      userId: 2001
    }
  },
  {
    account: 'EXAM2025001',
    password: '123456',
    token: 'demo-token-exam',
    roleCode: 'EXAM_DOCTOR',
    roleName: '检查医生',
    workbenchRoute: '/exam',
    user: {
      username: 'EXAM2025001',
      realName: '于景澄',
      doctorId: 11,
      deptId: 11,
      doctorType: 'EXAM',
      userId: 16
    }
  },
  {
    account: 'LAB2025001',
    password: '123456',
    token: 'demo-token-lab',
    roleCode: 'LAB_DOCTOR',
    roleName: '检验医生',
    workbenchRoute: '/lab',
    user: {
      username: 'LAB2025001',
      realName: '韩书瑶',
      doctorId: 16,
      deptId: 6,
      doctorType: 'LAB',
      userId: 21
    }
  },
  {
    account: 'PHAR2025001',
    password: '123456',
    token: 'demo-token-pharmacy',
    roleCode: 'PHARMACY_DOCTOR',
    roleName: '药房医生',
    workbenchRoute: '/pharmacy/dispatch',
    user: {
      username: 'PHAR2025001',
      realName: '李药师',
      doctorId: 5,
      deptId: 5,
      doctorType: 'PHARMACY',
      userId: 5001
    }
  }
];

const examLabReportStore = {
  '1000013': [
    {
      reportId: 1,
      orderId: 101,
      reportNo: 'RPT202606270001',
      itemName: '胸部正位片',
      reportType: '影像检查',
      status: '已发布',
      resultSummary: '双肺纹理增粗，右下肺可见片状模糊影。',
      findings: '右下肺片状密度增高影，边界欠清，余肺野未见明显实变。',
      conclusion: '考虑右下肺感染性病变，建议结合临床及炎症指标。',
      doctorReview: '结合血常规和体温变化，倾向呼吸道感染。',
      examFeatures: [
        { featureName: '右下肺阴影', featureValue: '片状密度增高影', unit: '', abnormalFlag: '异常' }
      ],
      labResultItems: []
    },
    {
      reportId: 2,
      orderId: 102,
      reportNo: 'RPT202606270002',
      itemName: '血常规五分类',
      reportType: '检验报告',
      status: '已发布',
      resultSummary: '白细胞和中性粒细胞升高。',
      findings: 'WBC 12.8×10^9/L，中性粒细胞 82%，CRP 升高。',
      conclusion: '提示急性炎症反应，考虑细菌感染可能。',
      doctorReview: '建议结合影像结果综合判断。',
      examFeatures: [],
      labResultItems: [
        { indicatorName: 'WBC', resultValue: '12.8', unit: '×10^9/L', referenceRange: '3.5-9.5', abnormalFlag: '高' },
        { indicatorName: '中性粒细胞', resultValue: '82', unit: '%', referenceRange: '40-75', abnormalFlag: '高' }
      ]
    }
  ],
  '1000015': [
    {
      reportId: 3,
      orderId: 103,
      reportNo: 'RPT202606270003',
      itemName: '尿常规',
      reportType: '检验报告',
      status: '已发布',
      resultSummary: '尿蛋白阴性，白细胞轻度升高。',
      findings: '尿白细胞 8-10/HP，亚硝酸盐阴性。',
      conclusion: '提示轻度泌尿系刺激表现，建议结合症状随访。',
      doctorReview: '暂未见明确严重异常。',
      examFeatures: [],
      labResultItems: [
        { indicatorName: '尿白细胞', resultValue: '8-10', unit: '/HP', referenceRange: '0-5', abnormalFlag: '高' }
      ]
    }
  ]
};

const prescriptionStore = {};
const examOrderStore = {};
const labOrderStore = {};
const feeOrderStore = {};
const registrationState = createRegistrationState();

function formatDate(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatDateTime(date = new Date()) {
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  const second = String(date.getSeconds()).padStart(2, '0');
  return `${formatDate(date)} ${hour}:${minute}:${second}`;
}

function createRegistrationState() {
  const today = formatDate();
  const tomorrow = formatDate(new Date(Date.now() + 24 * 60 * 60 * 1000));

  return {
    patientSeed: 2000000,
    registrationSeed: 4,
    feeOrderSeed: 5,
    queueSeed: 1,
    departments: [
      { deptId: 1, deptName: '呼吸内科', deptType: 'OUTPATIENT' },
      { deptId: 2, deptName: '消化内科', deptType: 'OUTPATIENT' },
      { deptId: 3, deptName: '全科门诊', deptType: 'OUTPATIENT' },
      { deptId: 4, deptName: '影像科', deptType: 'AUXILIARY' }
    ],
    doctors: [
      { doctorId: 1, doctorName: '张仲景', title: '主任医师', deptId: 1 },
      { doctorId: 2, doctorName: '华佗', title: '副主任医师', deptId: 1 },
      { doctorId: 3, doctorName: '李时珍', title: '主治医师', deptId: 2 },
      { doctorId: 4, doctorName: '扁鹊', title: '主治医师', deptId: 3 }
    ],
    schedules: [
      {
        scheduleId: 1,
        deptId: 1,
        deptName: '呼吸内科',
        doctorId: 1,
        doctorName: '张仲景',
        workDate: today,
        timePeriod: '上午',
        remainQuota: 8,
        registrationFee: 18
      },
      {
        scheduleId: 2,
        deptId: 1,
        deptName: '呼吸内科',
        doctorId: 2,
        doctorName: '华佗',
        workDate: today,
        timePeriod: '下午',
        remainQuota: 6,
        registrationFee: 16
      },
      {
        scheduleId: 3,
        deptId: 2,
        deptName: '消化内科',
        doctorId: 3,
        doctorName: '李时珍',
        workDate: today,
        timePeriod: '上午',
        remainQuota: 5,
        registrationFee: 20
      },
      {
        scheduleId: 4,
        deptId: 3,
        deptName: '全科门诊',
        doctorId: 4,
        doctorName: '扁鹊',
        workDate: tomorrow,
        timePeriod: '上午',
        remainQuota: 10,
        registrationFee: 15
      }
    ],
    registrations: [
      {
        registrationId: 1,
        registrationNo: 'GH202606270001',
        patientId: '1000013',
        patientName: '梦琪',
        deptId: 1,
        deptName: '呼吸内科',
        doctorId: 1,
        doctorName: '张仲景',
        scheduleId: 1,
        workDate: today,
        timePeriod: '上午',
        registrationFee: 18,
        feeStatus: '已支付',
        status: '候诊中',
        queueNo: 'A001',
        source: '线下',
        payMethod: '现金',
        createdAt: `${today} 09:10:00`
      },
      {
        registrationId: 2,
        registrationNo: 'GH202606270002',
        patientId: '1000014',
        patientName: '张伟',
        deptId: 1,
        deptName: '呼吸内科',
        doctorId: 1,
        doctorName: '张仲景',
        scheduleId: 1,
        workDate: today,
        timePeriod: '上午',
        registrationFee: 18,
        feeStatus: '已支付',
        status: '待确认',
        queueNo: '',
        source: '线上',
        payMethod: '线上支付',
        createdAt: `${today} 09:26:00`
      },
      {
        registrationId: 3,
        registrationNo: 'GH202606270003',
        patientId: '1000016',
        patientName: '王强',
        deptId: 1,
        deptName: '呼吸内科',
        doctorId: 2,
        doctorName: '华佗',
        scheduleId: 2,
        workDate: today,
        timePeriod: '下午',
        registrationFee: 16,
        feeStatus: '待支付',
        status: '待缴费',
        queueNo: '',
        source: '线下',
        payMethod: '',
        createdAt: `${today} 09:38:00`
      },
      {
        registrationId: 4,
        registrationNo: 'GH202606270004',
        patientId: '1000015',
        patientName: '李纳',
        deptId: 2,
        deptName: '消化内科',
        doctorId: 3,
        doctorName: '李时珍',
        scheduleId: 3,
        workDate: today,
        timePeriod: '上午',
        registrationFee: 20,
        feeStatus: '待支付',
        status: '待确认',
        queueNo: '',
        source: '线上',
        payMethod: '',
        createdAt: `${today} 10:02:00`
      }
    ],
    feeOrders: [
      {
        feeOrderId: 1,
        patientId: '1000013',
        patientName: '梦琪',
        registrationId: 1,
        feeType: '挂号费',
        totalAmount: 18,
        payStatus: '已支付',
        executed: false,
        payMethod: '现金',
        createdAt: `${today} 09:10:00`
      },
      {
        feeOrderId: 2,
        patientId: '1000014',
        patientName: '张伟',
        registrationId: 2,
        feeType: '挂号费',
        totalAmount: 18,
        payStatus: '已支付',
        executed: false,
        payMethod: '线上支付',
        createdAt: `${today} 09:26:00`
      },
      {
        feeOrderId: 3,
        patientId: '1000016',
        patientName: '王强',
        registrationId: 3,
        feeType: '挂号费',
        totalAmount: 16,
        payStatus: '待支付',
        executed: false,
        payMethod: '',
        createdAt: `${today} 09:38:00`
      },
      {
        feeOrderId: 4,
        patientId: '1000015',
        patientName: '李纳',
        registrationId: 4,
        feeType: '挂号费',
        totalAmount: 20,
        payStatus: '待支付',
        executed: false,
        payMethod: '',
        createdAt: `${today} 10:02:00`
      },
      {
        feeOrderId: 5,
        patientId: '1000013',
        patientName: '梦琪',
        registrationId: 1,
        feeType: '检查费',
        totalAmount: 28,
        payStatus: '待支付',
        executed: false,
        payMethod: '',
        createdAt: `${today} 10:22:00`
      }
    ]
  };
}

function createPayload(data, message = 'success', code = 200) {
  return {
    code,
    message,
    data
  };
}

function createResponse(config, payload, status = 200) {
  return {
    data: payload,
    status,
    statusText: status >= 400 ? 'ERROR' : 'OK',
    headers: {},
    config
  };
}

function createHttpError(config, status, message) {
  const error = new Error(message);
  error.config = config;
  error.response = createResponse(config, {
    code: status,
    message,
    data: null
  }, status);
  return error;
}

function filterLibrary(list, keyword) {
  const value = (keyword || '').trim();
  return list.filter((item) => {
    return !value || Object.values(item).some((field) => String(field).includes(value));
  });
}

function safeParse(raw) {
  if (!raw) {
    return {};
  }

  if (typeof raw === 'string') {
    try {
      return JSON.parse(raw);
    } catch {
      return {};
    }
  }

  return raw;
}

function normalizePatientStatus(status) {
  if (status === '医生接诊') {
    return '接诊中';
  }

  return status || '等待就诊';
}

function nextQueueNo() {
  registrationState.queueSeed += 1;
  return `A${String(registrationState.queueSeed).padStart(3, '0')}`;
}

function findPatient(patientId) {
  return patients.find((item) => String(item.id) === String(patientId));
}

function findRegistration(registrationId) {
  return registrationState.registrations.find((item) => Number(item.registrationId) === Number(registrationId));
}

function findFeeOrder(feeOrderId) {
  return registrationState.feeOrders.find((item) => Number(item.feeOrderId) === Number(feeOrderId));
}

function findSchedule(scheduleId) {
  return registrationState.schedules.find((item) => Number(item.scheduleId) === Number(scheduleId));
}

function buildPatientListRow(patient) {
  const activeRegistration = [...registrationState.registrations]
    .reverse()
    .find((item) => String(item.patientId) === String(patient.id) && item.status !== '已退号');

  let status = normalizePatientStatus(patient.status);
  let visitStatus = '待接诊';
  if (activeRegistration?.status === '接诊中') {
    status = '接诊中';
    visitStatus = '接诊中';
  } else if (activeRegistration?.status === '候诊中') {
    status = '等待就诊';
    visitStatus = '待接诊';
  }

  if (activeRegistration?.reviewStatus === '待回阅') {
    visitStatus = '报告待回阅';
  }
  if (activeRegistration?.reviewStatus === '已回阅' || activeRegistration?.status === '待确诊') {
    visitStatus = '待确诊';
  }
  if (activeRegistration?.status === '已完成') {
    visitStatus = '已完成';
  }

  return {
    patientId: patient.id,
    visitId: activeRegistration ? `VISIT-${patient.id}` : null,
    patientNo: patient.id,
    patientName: patient.name,
    idCard: patient.idCard || `11010119900101${String(patient.id).slice(-4)}`,
    age: patient.age,
    gender: patient.gender,
    status,
    visitStatus,
    queueNo: activeRegistration?.queueNo || '',
    registeredAt: activeRegistration?.createdAt || patient.registerTime || formatDateTime(),
    registerTime: activeRegistration?.createdAt || patient.registerTime || formatDateTime()
  };
}

function buildPatientContext(patientId) {
  const record = clone(medicalRecords[patientId] || createEmptyMedicalRecord());
  const registration = [...registrationState.registrations]
    .reverse()
    .find((item) => String(item.patientId) === String(patientId) && item.status !== '已退号');
  const visitId = `VISIT-${patientId}`;
  const visitStatus = registration?.reviewStatus === '待回阅'
    ? '报告待回阅'
    : registration?.reviewStatus === '已回阅' || registration?.status === '待确诊'
      ? '待确诊'
      : registration?.status === '接诊中'
        ? '接诊中'
        : registration?.status === '已完成'
          ? '已完成'
          : '待接诊';

  return {
    patientId,
    registration: registration ? clone(registration) : null,
    visit: {
      visitId,
      visitStatus
    },
    visitId,
    medicalRecord: {
      recordId: record.recordId || `MR-${patientId}`,
      chiefComplaint: record.chiefComplaint || '',
      presentIllness: record.presentIllness || '',
      currentTreatment: record.currentTreatment || '',
      pastHistory: record.pastHistory || '',
      allergyHistory: record.allergyHistory || '',
      physicalExam: record.physicalExam || '',
      auxiliaryExam: record.auxiliaryExam || '',
      diagnosis: record.diagnosisText || '',
      treatmentAdvice: record.examSuggestion || '',
      doctorNote: record.notes || '',
      finalDiagnosis: record.finalDiagnosis || '',
      finalOpinion: record.finalOpinion || '',
      status: record.status || '初诊暂存'
    },
    examOrders: clone(examOrderStore[visitId] || []),
    labOrders: clone(labOrderStore[visitId] || []),
    prescriptions: clone(prescriptionStore[visitId] || []),
    feeOrders: clone(feeOrderStore[visitId] || [])
  };
}

function updateMedicalRecord(payload) {
  medicalRecords[payload.patientId] = {
    ...(medicalRecords[payload.patientId] || {}),
    recordId: payload.recordId || medicalRecords[payload.patientId]?.recordId || `MR-${payload.patientId}`,
    chiefComplaint: payload.chiefComplaint || '',
    presentIllness: payload.presentIllness || '',
    currentTreatment: payload.currentTreatment || '',
    pastHistory: payload.pastHistory || '',
    allergyHistory: payload.allergyHistory || '',
    physicalExam: payload.physicalExam || '',
    auxiliaryExam: payload.auxiliaryExam || '',
    diagnosisText: payload.diagnosis || '',
    examSuggestion: payload.treatmentAdvice || '',
    notes: payload.doctorNote || '',
    finalDiagnosis: payload.finalDiagnosis || medicalRecords[payload.patientId]?.finalDiagnosis || '',
    finalOpinion: payload.finalOpinion || medicalRecords[payload.patientId]?.finalOpinion || '',
    status: payload.status || '初诊暂存'
  };
}

function buildOutpatientSuggestion(payload) {
  const complaint = String(payload.currentChiefComplaint || '').trim();
  const diagnosis = String(payload.currentDiagnosis || '').trim();
  const findings = payload.reportId
    ? findReportById(payload.reportId)?.conclusion || findReportById(payload.reportId)?.resultSummary || ''
    : '';

  if (payload.sceneCode === 'OUTPATIENT_POST_REPORT_SUGGESTION') {
    return {
      diagnosisDraft: findings
        ? `结合报告结果，考虑${findings}，建议继续完善抗感染及复查方案。`
        : '建议结合已发布报告完善最终诊断，并同步调整治疗方案。',
      reportSummary: findings || '报告已回阅，建议结合本次结果完善最终诊断。',
      planDraft: '建议继续抗感染治疗，并结合症状变化安排复诊或复查。',
      drugSuggestions: ['阿莫西林', '布洛芬缓释片'],
      riskFlags: ['关注药物过敏史', '如持续高热需及时复诊']
    };
  }

  if (complaint.includes('咳') || complaint.includes('发热')) {
    return {
      diagnosisDraft: diagnosis || '考虑呼吸道感染，建议完善血常规和胸部影像检查。'
    };
  }

  if (complaint.includes('腹痛') || complaint.includes('腹胀')) {
    return {
      diagnosisDraft: diagnosis || '考虑消化系统功能紊乱，建议结合腹部检查进一步评估。'
    };
  }

  return {
    diagnosisDraft: diagnosis || '建议结合主诉、现病史和体格检查信息综合判断。'
  };
}

function findReportById(reportId) {
  return Object.values(examLabReportStore)
    .flat()
    .find((item) => Number(item.reportId) === Number(reportId));
}

function buildRegistrationAiSuggestion(params = {}) {
  const query = safeParse(params.query);
  const complaint = String(query.chiefComplaint || '').trim();
  const outpatientDepartments = Array.isArray(query.departments)
    ? query.departments.filter((item) => item.deptType === 'OUTPATIENT')
    : [];
  const doctors = Array.isArray(query.doctors) ? query.doctors : [];
  const schedules = Array.isArray(query.schedules) ? query.schedules : [];

  let matchedDepartment = outpatientDepartments[0];
  if (complaint.includes('咳') || complaint.includes('发热') || complaint.includes('咽痛')) {
    matchedDepartment = outpatientDepartments.find((item) => item.deptName.includes('呼吸')) || matchedDepartment;
  } else if (complaint.includes('腹痛') || complaint.includes('腹泻') || complaint.includes('反酸')) {
    matchedDepartment = outpatientDepartments.find((item) => item.deptName.includes('消化')) || matchedDepartment;
  } else if (complaint.includes('头晕') || complaint.includes('乏力')) {
    matchedDepartment = outpatientDepartments.find((item) => item.deptName.includes('全科')) || matchedDepartment;
  }

  const matchedDoctor = doctors.find((item) => item.deptId === matchedDepartment?.deptId) || doctors[0];
  const matchedSchedule = schedules.find((item) => item.doctorId === matchedDoctor?.doctorId) || schedules[0];

  if (params.sceneCode === 'DEPARTMENT_RECOMMEND') {
    return [
      `建议优先挂号至${matchedDepartment?.deptName || '相关门诊'}。`,
      complaint ? `当前主诉为“${complaint}”，优先结合对应专科进行首诊分流。` : '建议结合患者主诉进一步人工确认分诊方向。'
    ].join('\n');
  }

  if (params.sceneCode === 'DOCTOR_RECOMMEND') {
    return [
      `建议优先选择${matchedDoctor?.doctorName || '值班医生'}${matchedDoctor?.title ? `（${matchedDoctor.title}）` : ''}。`,
      matchedSchedule
        ? `可参考排班：${matchedSchedule.workDate} ${matchedSchedule.timePeriod}，当前余号 ${matchedSchedule.remainQuota}。`
        : '当前暂无排班信息，请人工确认后分配医生。'
    ].join('\n');
  }

  return [
    `建议先分诊至${matchedDepartment?.deptName || '相关门诊'}。`,
    complaint ? `结合主诉“${complaint}”，建议优先完成基础问诊、体温和生命体征评估。` : '建议先补充主诉信息后再进行 AI 分诊。'
  ].join('\n');
}

export function createRegistrationAiStreamResponse(params = {}) {
  const message = buildRegistrationAiSuggestion(params);
  const body = message
    .split('\n')
    .filter(Boolean)
    .map((line) => `data: ${line}\n\n`)
    .join('');

  return Promise.resolve(new Response(body, {
    status: 200,
    headers: {
      'Content-Type': 'text/event-stream; charset=utf-8'
    }
  }));
}

function createRequestDefaultsIfNeeded(patientId) {
  requestDefaults[patientId] = requestDefaults[patientId] || createEmptyRequests();
}

function nextOrderNo(prefix) {
  return `${prefix}${Date.now()}${Math.floor(Math.random() * 1000).toString().padStart(3, '0')}`;
}

function ensureVisitFeeOrderStore(visitId) {
  feeOrderStore[visitId] = feeOrderStore[visitId] || [];
  return feeOrderStore[visitId];
}

function ensureVisitExamOrderStore(visitId) {
  examOrderStore[visitId] = examOrderStore[visitId] || [];
  return examOrderStore[visitId];
}

function ensureVisitLabOrderStore(visitId) {
  labOrderStore[visitId] = labOrderStore[visitId] || [];
  return labOrderStore[visitId];
}

function appendNote(patientId, text) {
  const current = medicalRecords[patientId] || createEmptyMedicalRecord();
  medicalRecords[patientId] = {
    ...current,
    notes: [current.notes, text].filter(Boolean).join('\n')
  };
}

function createPatientFromSync(payload) {
  const existingPatient = patients.find((item) => item.idCard === payload.idCard || (item.name === payload.patientName && item.phone === payload.phone));
  if (existingPatient) {
    createRequestDefaultsIfNeeded(existingPatient.id);
    medicalRecords[existingPatient.id] = medicalRecords[existingPatient.id] || createEmptyMedicalRecord();
    return existingPatient;
  }

  registrationState.patientSeed += 1;
  const patientId = String(registrationState.patientSeed);
  const patient = {
    id: patientId,
    name: payload.patientName,
    age: getAgeByIdCard(payload.idCard),
    gender: payload.gender,
    status: '待挂号',
    registerTime: formatDateTime(),
    idCard: payload.idCard,
    phone: payload.phone
  };

  patients.unshift(patient);
  medicalRecords[patientId] = {
    ...createEmptyMedicalRecord(),
    pastHistory: payload.pastHistory || '',
    allergyHistory: payload.allergyHistory || ''
  };
  requestDefaults[patientId] = createEmptyRequests();
  examLabReportStore[patientId] = [];

  return patient;
}

function getAgeByIdCard(idCard) {
  const value = String(idCard || '');
  if (!/^\d{17}[\dXx]$/.test(value)) {
    return 30;
  }

  const year = Number(value.slice(6, 10));
  const month = Number(value.slice(10, 12));
  const day = Number(value.slice(12, 14));
  const today = new Date();
  let age = today.getFullYear() - year;
  if (today.getMonth() + 1 < month || (today.getMonth() + 1 === month && today.getDate() < day)) {
    age -= 1;
  }
  return age;
}

function createRegistrationRecord(payload, source = '线下', forcePaid = false) {
  const patient = findPatient(payload.patientId);
  const schedule = findSchedule(payload.scheduleId);

  if (!patient || !schedule) {
    throw new Error('invalid registration payload');
  }

  registrationState.registrationSeed += 1;
  const registrationId = registrationState.registrationSeed;
  const registrationNo = `GH${formatDate().replace(/-/g, '')}${String(registrationId).padStart(4, '0')}`;
  const registration = {
    registrationId,
    registrationNo,
    patientId: patient.id,
    patientName: patient.name,
    deptId: schedule.deptId,
    deptName: schedule.deptName,
    doctorId: schedule.doctorId,
    doctorName: schedule.doctorName,
    scheduleId: schedule.scheduleId,
    workDate: schedule.workDate,
    timePeriod: schedule.timePeriod,
    registrationFee: schedule.registrationFee,
    feeStatus: forcePaid ? '已支付' : '待支付',
    status: source === '线上' ? '待确认' : (forcePaid ? '候诊中' : '待缴费'),
    queueNo: '',
    source,
    payMethod: forcePaid ? '线上支付' : '',
    createdAt: formatDateTime()
  };

  if (schedule.remainQuota > 0) {
    schedule.remainQuota -= 1;
  }

  registrationState.registrations.unshift(registration);

  registrationState.feeOrderSeed += 1;
  registrationState.feeOrders.unshift({
    feeOrderId: registrationState.feeOrderSeed,
    patientId: patient.id,
    patientName: patient.name,
    registrationId,
    feeType: '挂号费',
    totalAmount: schedule.registrationFee,
    payStatus: forcePaid ? '已支付' : '待支付',
    executed: false,
    payMethod: forcePaid ? '线上支付' : '',
    createdAt: formatDateTime()
  });

  if (forcePaid) {
    enqueueRegistration(registration);
  } else {
    patient.status = '待挂号';
  }

  return registration;
}

function enqueueRegistration(registration) {
  if (!registration.queueNo) {
    registration.queueNo = nextQueueNo();
  }
  registration.status = '候诊中';
  registration.feeStatus = '已支付';
  const patient = findPatient(registration.patientId);
  if (patient) {
    patient.status = '等待就诊';
  }
  return registration;
}

function markRegistrationPaid(registration, payMethod) {
  registration.feeStatus = '已支付';
  registration.payMethod = payMethod || registration.payMethod || '现金';

  const feeOrder = registrationState.feeOrders.find((item) => Number(item.registrationId) === Number(registration.registrationId) && item.feeType === '挂号费');
  if (feeOrder) {
    feeOrder.payStatus = '已支付';
    feeOrder.payMethod = registration.payMethod;
  }

  if (registration.source === '线上') {
    registration.status = '待确认';
    const patient = findPatient(registration.patientId);
    if (patient) {
      patient.status = '待挂号';
    }
    return registration;
  }

  return enqueueRegistration(registration);
}

function buildRefundCheck(feeOrderId) {
  const feeOrder = findFeeOrder(feeOrderId);
  if (!feeOrder) {
    return {
      feeOrderId,
      refundable: false,
      reason: '费用单不存在'
    };
  }

  if (feeOrder.payStatus === '已退费') {
    return {
      feeOrderId: feeOrder.feeOrderId,
      refundable: false,
      reason: '费用单已退费'
    };
  }

  if (feeOrder.payStatus !== '已支付') {
    return {
      feeOrderId: feeOrder.feeOrderId,
      refundable: false,
      reason: '当前费用未支付，无需退费'
    };
  }

  if (feeOrder.executed) {
    return {
      feeOrderId: feeOrder.feeOrderId,
      refundable: false,
      reason: '当前费用项目已执行，不可退费'
    };
  }

  const registration = feeOrder.registrationId ? findRegistration(feeOrder.registrationId) : null;
  if (registration?.status === '接诊中') {
    return {
      feeOrderId: feeOrder.feeOrderId,
      refundable: false,
      reason: '当前挂号已叫号，不可退费'
    };
  }

  return {
    feeOrderId: feeOrder.feeOrderId,
    refundable: true,
    reason: '当前费用允许退费'
  };
}

export async function mockAdapter(config) {
  await sleep(180);

  const url = config.url || '';
  const method = (config.method || 'get').toLowerCase();
  const params = config.params || {};
  const data = safeParse(config.data);

  if (method === 'post' && url === '/auth/login') {
    const account = authAccounts.find((item) => item.account === data.username && item.password === data.password);
    if (!account) {
      throw createHttpError(config, 401, '账户或密码错误');
    }

    return createResponse(config, createPayload({
      token: account.token,
      roleCode: account.roleCode,
      roleName: account.roleName,
      workbenchRoute: account.workbenchRoute,
      user: clone(account.user)
    }, '登录成功'));
  }

  if (method === 'get' && (url === '/outpatient/patients' || url === '/patients')) {
    const source = patients
      .map(buildPatientListRow)
      .filter((row) => {
        const matchedNo = !(params.patientNo || params.medicalNo) || row.patientNo.includes(params.patientNo || params.medicalNo);
        const matchedName = !(params.patientName || params.name) || row.patientName.includes(params.patientName || params.name);
        const matchedStatus = !params.visitStatus || row.visitStatus === params.visitStatus;
        const matchedGroup = !params.visitGroup || (
          params.visitGroup === 'ACTIVE'
            ? ['接诊中', '报告待回阅', '待确诊'].includes(row.visitStatus)
            : true
        );
        return matchedNo && matchedName && matchedStatus && matchedGroup;
      });

    return createResponse(config, createPayload(clone(source)));
  }

  if (method === 'get' && /^\/outpatient\/patients\/[^/]+\/context$/.test(url)) {
    const patientId = url.split('/')[3];
    return createResponse(config, createPayload(buildPatientContext(patientId)));
  }

  if (method === 'get' && url.startsWith('/patients/')) {
    const id = url.split('/').pop();
    return createResponse(config, createPayload({
      patient: buildPatientListRow(findPatient(id) || { id, name: '', age: '', gender: '', status: '', registerTime: '' }),
      record: clone(medicalRecords[id] || createEmptyMedicalRecord()),
      requests: clone(requestDefaults[id] || createEmptyRequests())
    }));
  }

  if (method === 'get' && url === '/diagnoses') {
    return createResponse(config, createPayload(filterLibrary(diagnosisLibrary, params.keyword)));
  }

  if (method === 'get' && url === '/exam-items') {
    return createResponse(config, createPayload(filterLibrary(examLibrarySource, params.keyword)));
  }

  if (method === 'get' && url === '/lab-items') {
    return createResponse(config, createPayload(filterLibrary(labLibrarySource, params.keyword)));
  }

  if (method === 'get' && url === '/drugs') {
    return createResponse(config, createPayload(filterLibrary(drugLibrarySource, params.keyword)));
  }

  if (method === 'get' && url === '/exam-lab-reports') {
    const patientId = String(params.patientId || '');
    return createResponse(config, createPayload(clone(examLabReportStore[patientId] || [])));
  }

  if (method === 'post' && url === '/exam-lab-reports/review') {
    const registration = [...registrationState.registrations]
      .reverse()
      .find((item) => String(item.patientId) === String(data.patientId) && item.status !== '已退号');
    if (registration) {
      registration.reviewStatus = '已回阅';
      registration.status = '待确诊';
    }
    const report = findReportById(data.reportId);
    if (report) {
      report.status = '已回阅';
    }
    return createResponse(config, createPayload({ reportId: data.reportId }, '报告已回阅'));
  }

  if (method === 'post' && url === '/outpatient/medical-records') {
    updateMedicalRecord(data);
    return createResponse(config, createPayload({ success: true }, '病历保存成功'));
  }

  if (method === 'post' && url === '/outpatient/start-encounter') {
    const registration = [...registrationState.registrations]
      .reverse()
      .find((item) => String(item.patientId) === String(data.patientId) && item.status !== '已退号');
    if (!registration) {
      throw createHttpError(config, 404, '挂号记录不存在');
    }
    registration.status = '接诊中';
    registration.reviewStatus = '接诊中';
    const patient = findPatient(data.patientId);
    if (patient) {
      patient.status = '接诊中';
    }
    return createResponse(config, createPayload(clone(registration), '接诊成功'));
  }

  if (method === 'post' && url === '/outpatient/final-diagnosis') {
    updateMedicalRecord({
      ...data,
      finalDiagnosis: data.finalDiagnosis,
      finalOpinion: data.finalOpinion,
      status: '已完成'
    });
    const registration = [...registrationState.registrations]
      .reverse()
      .find((item) => String(item.patientId) === String(data.patientId) && item.status !== '已退号');
    if (registration) {
      registration.status = '已完成';
      registration.reviewStatus = '已回阅';
    }
    const patient = findPatient(data.patientId);
    if (patient) {
      patient.status = '医生接诊';
    }
    return createResponse(config, createPayload({
      patientId: data.patientId,
      finalDiagnosis: data.finalDiagnosis,
      finalOpinion: data.finalOpinion
    }, '门诊确诊已保存'));
  }

  if (method === 'post' && url === '/outpatient/skip-exam') {
    appendNote(data.patientId, `【检查决策】本次无需检查。${data.reason ? `原因：${data.reason}` : ''}`);
    return createResponse(config, createPayload({ patientId: data.patientId }, '已记录本次无需检查'));
  }

  if (method === 'post' && url === '/outpatient/skip-lab') {
    appendNote(data.patientId, `【检验决策】本次无需检验。${data.reason ? `原因：${data.reason}` : ''}`);
    return createResponse(config, createPayload({ patientId: data.patientId }, '已记录本次无需检验'));
  }

  if (method === 'post' && url === '/medical-record') {
    medicalRecords[data.patientId] = clone(data.form);
    createRequestDefaultsIfNeeded(data.patientId);
    requestDefaults[data.patientId].diagnosisText = data.form?.diagnosisText || '';
    return createResponse(config, createPayload({ success: true }, '病历保存成功'));
  }

  if (method === 'post' && url === '/exam-request') {
    createRequestDefaultsIfNeeded(data.patientId);
    requestDefaults[data.patientId].examItems = clone(data.examItems || []);
    const visitId = data.visitId || `VISIT-${data.patientId}`;
    const order = {
      orderNo: nextOrderNo('EX'),
      orderType: '检查',
      status: '待执行',
      totalAmount: Number((data.examItems || []).reduce((sum, item) => sum + Number(item.price || 0), 0).toFixed(2)),
      appliedAt: formatDateTime(),
      purpose: data.form?.purpose || '',
      examSite: data.form?.site || '',
      remark: data.form?.notes || '',
      items: (data.examItems || []).map((item) => ({
        itemName: item.name,
        itemType: '检查',
        unitPrice: item.price,
        quantity: 1,
        amount: Number(item.price || 0),
        status: '待执行',
        resultSummary: ''
      }))
    };
    ensureVisitExamOrderStore(visitId).push(order);
    ensureVisitFeeOrderStore(visitId).push({
      feeOrderId: nextOrderNo('FEE'),
      orderNo: order.orderNo,
      feeType: '检查费',
      totalAmount: order.totalAmount,
      createdAt: order.appliedAt,
      items: order.items.map((item) => ({
        itemName: item.itemName,
        itemType: item.itemType,
        itemSpec: '',
        unitPrice: item.unitPrice,
        quantity: item.quantity,
        amount: item.amount,
        status: item.status
      }))
    });
    return createResponse(config, createPayload({ success: true, orderNo: order.orderNo }, '检查申请提交成功'));
  }

  if (method === 'post' && url === '/lab-request') {
    createRequestDefaultsIfNeeded(data.patientId);
    requestDefaults[data.patientId].labItems = clone(data.labItems || []);
    const visitId = data.visitId || `VISIT-${data.patientId}`;
    const order = {
      orderNo: nextOrderNo('LB'),
      orderType: '检验',
      status: '待执行',
      totalAmount: Number((data.labItems || []).reduce((sum, item) => sum + Number(item.price || 0), 0).toFixed(2)),
      appliedAt: formatDateTime(),
      purpose: data.form?.purpose || '',
      specimenType: data.form?.specimen || '',
      priority: data.form?.priority || '普通',
      collectionWay: data.form?.collectionWay || '门诊采样',
      remark: data.form?.notes || '',
      items: (data.labItems || []).map((item) => ({
        itemName: item.name,
        itemType: '检验',
        unitPrice: item.price,
        quantity: 1,
        amount: Number(item.price || 0),
        status: '待执行',
        resultSummary: ''
      }))
    };
    ensureVisitLabOrderStore(visitId).push(order);
    ensureVisitFeeOrderStore(visitId).push({
      feeOrderId: nextOrderNo('FEE'),
      orderNo: order.orderNo,
      feeType: '检验费',
      totalAmount: order.totalAmount,
      createdAt: order.appliedAt,
      items: order.items.map((item) => ({
        itemName: item.itemName,
        itemType: item.itemType,
        itemSpec: '',
        unitPrice: item.unitPrice,
        quantity: item.quantity,
        amount: item.amount,
        status: item.status
      }))
    });
    return createResponse(config, createPayload({ success: true, orderNo: order.orderNo }, '检验申请提交成功'));
  }

  if (method === 'post' && url === '/prescriptions') {
    const visitId = data.visitId || `VISIT-${data.patientId}`;
    prescriptionStore[visitId] = clone(data.items || []);
    ensureVisitFeeOrderStore(visitId).push({
      feeOrderId: nextOrderNo('FEE'),
      orderNo: nextOrderNo('RX'),
      feeType: '处方费',
      createdAt: formatDateTime(),
      totalAmount: Number((data.items || []).reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0).toFixed(2)),
      items: (data.items || []).map((item) => ({
        itemName: item.name,
        itemType: '药品',
        itemSpec: item.spec,
        unitPrice: item.price,
        quantity: item.quantity,
        amount: Number((Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2)),
        status: '待执行'
      }))
    });
    return createResponse(config, createPayload({
      prescriptionId: `RX-${data.patientId}-${Date.now()}`
    }, '处方已开立'));
  }

  if (method === 'get' && url === '/exam-lab-orders') {
    const visitId = params.visitId;
    const orderType = params.orderType;
    const source = orderType === '检查' ? examOrderStore[visitId] : labOrderStore[visitId];
    return createResponse(config, createPayload(clone(source || [])));
  }

  if (method === 'get' && url === '/fee-orders') {
    const source = feeOrderStore[params.visitId] || [];
    return createResponse(config, createPayload(clone(source)));
  }

  if (method === 'post' && url === '/ai/outpatient/suggestions') {
    return createResponse(config, createPayload(buildOutpatientSuggestion(data)));
  }

  if (method === 'post' && url === '/registration/patient/sync') {
    const patient = createPatientFromSync(data);
    return createResponse(config, createPayload({
      patientId: patient.id,
      patientNo: patient.id,
      patientName: patient.name,
      gender: patient.gender,
      phone: patient.phone || data.phone || '',
      allergyHistory: data.allergyHistory || '',
      pastHistory: data.pastHistory || ''
    }, '患者同步成功'));
  }

  if (method === 'get' && url === '/registration/departments') {
    return createResponse(config, createPayload(clone(registrationState.departments)));
  }

  if (method === 'get' && url === '/registration/doctors') {
    const rows = registrationState.doctors.filter((item) => !params.deptId || Number(item.deptId) === Number(params.deptId));
    return createResponse(config, createPayload(clone(rows)));
  }

  if (method === 'get' && url === '/registration/schedules') {
    const rows = registrationState.schedules.filter((item) => {
      return (!params.deptId || Number(item.deptId) === Number(params.deptId))
        && (!params.doctorId || Number(item.doctorId) === Number(params.doctorId))
        && (!params.workDate || item.workDate === params.workDate);
    });
    return createResponse(config, createPayload(clone(rows)));
  }

  if (method === 'post' && url === '/registration/offline/submit') {
    const registration = createRegistrationRecord(data, '线下', false);
    return createResponse(config, createPayload(clone(registration), '线下挂号已提交'));
  }

  if (method === 'post' && url === '/registration/online/submit') {
    const registration = createRegistrationRecord(data, '线上', true);
    registration.status = '待确认';
    registration.queueNo = '';
    return createResponse(config, createPayload(clone(registration), '线上挂号已提交'));
  }

  if (method === 'post' && url === '/registration/fee/charge') {
    const registration = findRegistration(data.registrationId);
    if (!registration) {
      throw createHttpError(config, 404, '挂号记录不存在');
    }
    return createResponse(config, createPayload(clone(markRegistrationPaid(registration, data.payMethod)), '收费成功'));
  }

  if (method === 'post' && url === '/registration/fee/order/charge') {
    const feeOrder = findFeeOrder(data.feeOrderId);
    if (!feeOrder) {
      throw createHttpError(config, 404, '费用单不存在');
    }

    feeOrder.payStatus = '已支付';
    feeOrder.payMethod = data.payMethod || feeOrder.payMethod || '现金';

    if (feeOrder.registrationId && feeOrder.feeType === '挂号费') {
      const registration = findRegistration(feeOrder.registrationId);
      if (registration) {
        markRegistrationPaid(registration, feeOrder.payMethod);
      }
    }

    return createResponse(config, createPayload(clone(feeOrder), '费用单已收款'));
  }

  if (method === 'get' && url === '/registration/queue') {
    const rows = registrationState.registrations
      .filter((item) => item.status === '候诊中')
      .filter((item) => !params.doctorId || Number(item.doctorId) === Number(params.doctorId))
      .filter((item) => !params.scheduleId || Number(item.scheduleId) === Number(params.scheduleId))
      .filter((item) => !params.workDate || item.workDate === params.workDate)
      .sort((a, b) => String(a.queueNo).localeCompare(String(b.queueNo)));

    return createResponse(config, createPayload(clone(rows)));
  }

  if (method === 'post' && url === '/registration/queue/call') {
    const queue = registrationState.registrations
      .filter((item) => item.status === '候诊中')
      .filter((item) => !params.doctorId || Number(item.doctorId) === Number(params.doctorId))
      .filter((item) => !params.scheduleId || Number(item.scheduleId) === Number(params.scheduleId))
      .filter((item) => !params.workDate || item.workDate === params.workDate)
      .sort((a, b) => String(a.queueNo).localeCompare(String(b.queueNo)));

    const current = queue[0];
    if (!current) {
      throw createHttpError(config, 400, '暂无可叫号患者');
    }

    current.status = '接诊中';
    const feeOrder = registrationState.feeOrders.find((item) => Number(item.registrationId) === Number(current.registrationId) && item.feeType === '挂号费');
    if (feeOrder) {
      feeOrder.executed = true;
    }

    const patient = findPatient(current.patientId);
    if (patient) {
      patient.status = '接诊中';
    }

    return createResponse(config, createPayload(clone(current), '叫号成功'));
  }

  if (method === 'get' && url === '/registration/online/pending') {
    const rows = registrationState.registrations.filter((item) => item.source === '线上' && item.status === '待确认');
    return createResponse(config, createPayload(clone(rows)));
  }

  if (method === 'put' && url === '/registration/online/confirm') {
    const registration = findRegistration(params.registrationId);
    if (!registration) {
      throw createHttpError(config, 404, '挂号记录不存在');
    }
    if (registration.feeStatus !== '已支付') {
      throw createHttpError(config, 400, '该线上挂号未支付，需先收取挂号费');
    }

    return createResponse(config, createPayload(clone(enqueueRegistration(registration)), '线上挂号已确认'));
  }

  if (method === 'get' && url === '/registration/fee/pending') {
    const rows = registrationState.feeOrders.filter((item) => {
      return item.payStatus === '待支付'
        && (!params.patientId || String(item.patientId) === String(params.patientId))
        && (!params.registrationId || Number(item.registrationId) === Number(params.registrationId));
    });
    return createResponse(config, createPayload(clone(rows)));
  }

  if (method === 'get' && url === '/registration/fee/history') {
    const orders = registrationState.feeOrders.filter((item) => {
      return (!params.patientId || String(item.patientId) === String(params.patientId))
        && (!params.registrationId || Number(item.registrationId) === Number(params.registrationId));
    });
    return createResponse(config, createPayload({ orders: clone(orders) }));
  }

  if (method === 'get' && url === '/registration/fee/refund/check') {
    return createResponse(config, createPayload(buildRefundCheck(Number(params.feeOrderId))));
  }

  if (method === 'post' && url === '/registration/fee/refund') {
    const check = buildRefundCheck(Number(data.feeOrderId));
    if (!check.refundable) {
      throw createHttpError(config, 400, check.reason);
    }

    const feeOrder = findFeeOrder(data.feeOrderId);
    feeOrder.payStatus = '已退费';
    feeOrder.executed = false;
    feeOrder.refundReason = data.reason || '';

    const registration = feeOrder.registrationId ? findRegistration(feeOrder.registrationId) : null;
    if (registration) {
      if (registration.status !== '已退号') {
        const schedule = findSchedule(registration.scheduleId);
        if (schedule) {
          schedule.remainQuota += 1;
        }
      }
      registration.status = '已退号';
      registration.feeStatus = '已退费';
      registration.queueNo = '';
    }

    const patient = findPatient(feeOrder.patientId);
    if (patient) {
      patient.status = '待挂号';
    }

    return createResponse(config, createPayload({
      feeOrderId: feeOrder.feeOrderId,
      reason: data.reason || ''
    }, '退费已完成'));
  }

  if (method === 'get' && url.startsWith('/pharmacy/dispatch/queue')) {
    const { patientId, keyword, department } = params || {};
    let filtered = dispatchQueue.filter((r) => {
      if (patientId && String(r.patientId) !== String(patientId)) return false;
      if (keyword && !r.patientName.includes(keyword) && !r.prescriptionNo.includes(keyword)) return false;
      if (department && !r.department.includes(department)) return false;
      return true;
    });
    return createResponse(config, createPayload(filtered));
  }

  if (method === 'get' && url.startsWith('/pharmacy/dispense/')) {
    const idStr = url.split('/').pop();
    const record = dispatchQueue.find((r) => String(r.dispenseId) === String(idStr));
    if (!record) {
      return createResponse(config, createPayload({}, '未找到发药记录', 404));
    }
    return createResponse(config, createPayload(record));
  }

  if (method === 'post' && url === '/pharmacy/dispense/mark') {
    const target = dispatchQueue.find((r) => String(r.dispenseId) === String(data.dispenseId));
    if (!target) {
      return createResponse(config, createPayload({}, '未找到发药记录', 404));
    }
    target.status = '已发药';
    return createResponse(config, createPayload({
      dispenseId: target.dispenseId,
      prescriptionNo: target.prescriptionNo,
      pharmacist: data.pharmacist || '药房药师',
      dispensedAt: new Date().toLocaleString()
    }, '发药完成'));
  }

  if (method === 'get' && url === '/pharmacy/stats') {
    return createResponse(config, createPayload(buildDispatchStats()));
  }

  if (method === 'get' && url.startsWith('/pharmacy/refund/list')) {
    const { keyword, status, patientId } = params || {};
    const records = dispatchQueue
      .filter((r) => r.status === '已发药' || r.status === '已退药')
      .map((r) => ({
        ...r,
        status: r.refundedAt ? '已退药' : '可退药'
      }))
      .filter((r) => {
        if (patientId && String(r.patientId) !== String(patientId)) return false;
        if (keyword && !r.patientName.includes(keyword) && !r.prescriptionNo.includes(keyword)) return false;
        if (status === '可退药' && r.status !== '可退药') return false;
        if (status === '已退药' && r.status !== '已退药') return false;
        return true;
      });
    return createResponse(config, createPayload(records));
  }

  if (method === 'post' && url === '/pharmacy/refund/submit') {
    const target = dispatchQueue.find((r) => String(r.dispenseId) === String(data.dispenseId));
    if (!target) {
      return createResponse(config, createPayload({}, '未找到发药记录', 404));
    }
    (data.items || []).forEach((refund) => {
      const item = target.items.find((x) => x.drugId === refund.drugId);
      if (item && typeof item.stock === 'number') {
        item.stock += Number(refund.refundQuantity) || 0;
      }
    });
    target.refundedAt = new Date().toLocaleString();
    target.status = '已退药';
    return createResponse(config, createPayload({
      refundId: 'RF' + target.dispenseId,
      dispenseId: target.dispenseId,
      prescriptionNo: target.prescriptionNo,
      refundedAt: target.refundedAt
    }, '退药成功，库存已同步回退'));
  }

  if (method === 'get' && url === '/pharmacy/stock') {
    const { keyword, status } = params || {};
    const list = pharmacyStockList.filter((d) => {
      if (keyword && !d.drugName.includes(keyword) && !d.drugId.includes(keyword)) return false;
      if (status === 'danger' && d.stock > 20) return false;
      if (status === 'warning' && (d.stock <= 20 || d.stock > 50)) return false;
      if (status === 'success' && d.stock <= 50) return false;
      return true;
    });
    return createResponse(config, createPayload(list));
  }

  if (method === 'post' && url.startsWith('/pharmacy/stock/')) {
    const idStr = url.split('/').pop();
    const target = pharmacyStockList.find((d) => String(d.drugId) === String(idStr));
    if (!target) return createResponse(config, createPayload({}, '未找到药品', 404));
    target.stock = data.stock != null ? Number(data.stock) : target.stock;
    target.updatedAt = new Date().toLocaleString();
    return createResponse(config, createPayload({
      drugId: target.drugId,
      drugName: target.drugName,
      stock: target.stock,
      updatedAt: target.updatedAt,
      purchaseQuantity: data.purchaseQuantity || 0,
      supplier: data.supplier || target.defaultSupplier
    }, '库存已更新'));
  }

  if (method === 'get' && url === '/pharmacy/purchase/list') {
    const { keyword, status } = params || {};
    const list = pharmacyStockList.filter((d) => {
      if (keyword && !d.drugName.includes(keyword) && !d.drugId.includes(keyword)) return false;
      if (status && String(status) === 'danger' && d.stock > 50) return false;
      return true;
    }).map((d) => ({
      drugId: d.drugId,
      drugName: d.drugName,
      specification: d.specification,
      unit: d.unit,
      currentStock: d.stock,
      defaultSupplier: d.defaultSupplier,
      updatedAt: d.updatedAt
    }));
    return createResponse(config, createPayload(list));
  }

  if (method === 'post' && url === '/pharmacy/purchase/submit') {
    const target = pharmacyStockList.find((d) => String(d.drugId) === String(data.drugId));
    if (!target) return createResponse(config, createPayload({}, '未找到药品', 404));
    const qty = Number(data.purchaseQuantity) || 0;
    target.stock += qty;
    target.updatedAt = new Date().toLocaleString();
    return createResponse(config, createPayload({
      purchaseId: 'PO' + Date.now(),
      drugId: target.drugId,
      drugName: target.drugName,
      purchaseQuantity: qty,
      supplier: data.supplier || target.defaultSupplier,
      stockAfterPurchase: target.stock,
      createdAt: target.updatedAt
    }, '购药申请已提交，库存同步增加'));
  }

  throw createHttpError(config, 404, `Mock endpoint not implemented: ${method.toUpperCase()} ${url}`);
}

export function createEmptyMedicalRecord() {
  return {
    recordId: null,
    patientId: null,
    visitId: null,
    chiefComplaint: '',
    presentIllness: '',
    currentTreatment: '',
    pastHistory: '',
    allergyHistory: '',
    physicalExam: '',
    auxiliaryExam: '',
    diagnosisText: '',
    examSuggestion: '',
    notes: '',
    finalDiagnosis: '',
    finalOpinion: '',
    status: '初诊暂存'
  };
}

function createEmptyRequests() {
  return {
    examItems: [],
    labItems: [],
    diagnosisText: ''
  };
}

const dispatchQueue = [
  {
    dispenseId: 1001,
    prescriptionNo: 'RX202606280001',
    patientId: '1000013',
    patientName: '梦琪',
    gender: '女',
    age: 32,
    department: '呼吸内科',
    doctorName: '张仲景',
    diagnosis: '急性上呼吸道感染',
    totalAmount: 68.5,
    payStatus: '已支付',
    status: '待发药',
    createdAt: '2026-06-28 09:15:22',
    items: [
      { drugId: 'D001', drugName: '阿莫西林胶囊', specification: '0.25g*24粒', quantity: 2, unit: '盒', usage: '口服，一次2粒，一日3次', stock: 120 },
      { drugId: 'D002', drugName: '布洛芬缓释片', specification: '0.3g*20片', quantity: 1, unit: '盒', usage: '口服，一次1片，必要时服用', stock: 85 }
    ]
  },
  {
    dispenseId: 1002,
    prescriptionNo: 'RX202606280002',
    patientId: '1000014',
    patientName: '张伟',
    gender: '男',
    age: 45,
    department: '呼吸内科',
    doctorName: '张仲景',
    diagnosis: '慢性支气管炎急性发作',
    totalAmount: 156.8,
    payStatus: '已支付',
    status: '待发药',
    createdAt: '2026-06-28 09:42:11',
    items: [
      { drugId: 'D003', drugName: '头孢克肟分散片', specification: '100mg*6片', quantity: 2, unit: '盒', usage: '口服，一次1片，一日2次', stock: 40 },
      { drugId: 'D004', drugName: '氨溴索口服液', specification: '100ml', quantity: 2, unit: '瓶', usage: '口服，一次10ml，一日3次', stock: 5 },
      { drugId: 'D005', drugName: '孟鲁司特钠片', specification: '10mg*7片', quantity: 1, unit: '盒', usage: '口服，每晚睡前1片', stock: 32 }
    ]
  },
  {
    dispenseId: 1003,
    prescriptionNo: 'RX202606280003',
    patientId: '1000015',
    patientName: '李纳',
    gender: '女',
    age: 28,
    department: '消化内科',
    doctorName: '李时珍',
    diagnosis: '功能性消化不良',
    totalAmount: 92.0,
    payStatus: '已支付',
    status: '待发药',
    createdAt: '2026-06-28 10:05:47',
    items: [
      { drugId: 'D006', drugName: '奥美拉唑肠溶胶囊', specification: '20mg*14粒', quantity: 2, unit: '盒', usage: '口服，晨起空腹1粒', stock: 60 },
      { drugId: 'D007', drugName: '多潘立酮片', specification: '10mg*30片', quantity: 1, unit: '盒', usage: '口服，一次1片，一日3次，餐前服用', stock: 8 }
    ]
  },
  {
    dispenseId: 1004,
    prescriptionNo: 'RX202606280004',
    patientId: '1000016',
    patientName: '王强',
    gender: '男',
    age: 38,
    department: '全科门诊',
    doctorName: '扁鹊',
    diagnosis: '急性胃肠炎',
    totalAmount: 45.5,
    payStatus: '待支付',
    status: '待发药',
    createdAt: '2026-06-28 10:28:30',
    items: [
      { drugId: 'D008', drugName: '蒙脱石散', specification: '3g*6袋', quantity: 1, unit: '盒', usage: '口服，一次1袋，一日3次', stock: 150 },
      { drugId: 'D009', drugName: '口服补液盐', specification: '5.125g*6袋', quantity: 1, unit: '盒', usage: '溶解于250ml温水，分次服用', stock: 4 }
    ]
  },
  {
    dispenseId: 1005,
    prescriptionNo: 'RX202606280005',
    patientId: '1000017',
    patientName: '赵敏',
    gender: '女',
    age: 5,
    department: '全科门诊',
    doctorName: '扁鹊',
    diagnosis: '小儿感冒',
    totalAmount: 38.0,
    payStatus: '已支付',
    status: '已发药',
    createdAt: '2026-06-28 08:55:12',
    items: [
      { drugId: 'D010', drugName: '小儿氨酚黄那敏颗粒', specification: '6g*10袋', quantity: 1, unit: '盒', usage: '口服，一次1袋，一日3次', stock: 200 }
    ]
  }
];

function buildDispatchStats() {
  const pending = dispatchQueue.filter((r) => r.status !== '已发药' && r.payStatus === '已支付').length;
  const today = dispatchQueue.filter((r) => r.status === '已发药').length;
  const lowStock = dispatchQueue
    .flatMap((r) => r.items)
    .filter((item) => item.stock <= 10)
    .reduce((acc, item) => {
      if (!acc.find((x) => x.drugId === item.drugId)) acc.push(item);
      return acc;
    }, []).length;
  return {
    pending,
    today,
    lowStock,
    total: dispatchQueue.length
  };
}

const pharmacyStockList = [
  { drugId: 'D001', drugName: '阿莫西林胶囊', specification: '0.25g*24粒', unit: '盒', stock: 120, defaultSupplier: '华东医药', updatedAt: '2026-06-28 08:30' },
  { drugId: 'D002', drugName: '布洛芬缓释片', specification: '0.3g*20片', unit: '盒', stock: 85, defaultSupplier: '哈药集团', updatedAt: '2026-06-27 15:22' },
  { drugId: 'D003', drugName: '头孢克肟分散片', specification: '100mg*6片', unit: '盒', stock: 40, defaultSupplier: '石药集团', updatedAt: '2026-06-26 09:11' },
  { drugId: 'D004', drugName: '氨溴索口服液', specification: '100ml', unit: '瓶', stock: 5, defaultSupplier: '云南白药', updatedAt: '2026-06-25 10:50' },
  { drugId: 'D005', drugName: '孟鲁司特钠片', specification: '10mg*7片', unit: '盒', stock: 32, defaultSupplier: '默沙东', updatedAt: '2026-06-24 16:04' },
  { drugId: 'D006', drugName: '奥美拉唑肠溶胶囊', specification: '20mg*14粒', unit: '盒', stock: 60, defaultSupplier: '阿斯利康', updatedAt: '2026-06-23 11:20' },
  { drugId: 'D007', drugName: '多潘立酮片', specification: '10mg*30片', unit: '盒', stock: 8, defaultSupplier: '西安杨森', updatedAt: '2026-06-28 07:55' },
  { drugId: 'D008', drugName: '蒙脱石散', specification: '3g*6袋', unit: '盒', stock: 150, defaultSupplier: '博福-益普生', updatedAt: '2026-06-22 14:35' },
  { drugId: 'D009', drugName: '口服补液盐', specification: '5.125g*6袋', unit: '盒', stock: 4, defaultSupplier: '博福-益普生', updatedAt: '2026-06-21 12:08' },
  { drugId: 'D010', drugName: '小儿氨酚黄那敏颗粒', specification: '6g*10袋', unit: '盒', stock: 200, defaultSupplier: '葵花药业', updatedAt: '2026-06-20 18:40' },
  { drugId: 'D011', drugName: '氯雷他定片', specification: '10mg*6片', unit: '盒', stock: 18, defaultSupplier: '拜耳医药', updatedAt: '2026-06-19 09:00' },
  { drugId: 'D012', drugName: '硝酸甘油片', specification: '0.5mg*100片', unit: '瓶', stock: 72, defaultSupplier: '信谊药厂', updatedAt: '2026-06-18 13:55' },
  { drugId: 'D013', drugName: '复方感冒灵颗粒', specification: '10g*9袋', unit: '盒', stock: 28, defaultSupplier: '广药集团', updatedAt: '2026-06-17 11:12' },
  { drugId: 'D014', drugName: '硝苯地平缓释片', specification: '20mg*30片', unit: '盒', stock: 12, defaultSupplier: '拜耳医药', updatedAt: '2026-06-16 08:47' },
  { drugId: 'D015', drugName: '盐酸二甲双胍片', specification: '0.5g*60片', unit: '盒', stock: 220, defaultSupplier: '中美上海施贵宝', updatedAt: '2026-06-15 17:22' }
];

function buildPurchaseStats() {
  const low = pharmacyStockList.filter((d) => d.stock <= 20).length;
  const warning = pharmacyStockList.filter((d) => d.stock > 20 && d.stock <= 50).length;
  const normal = pharmacyStockList.length - low - warning;
  return { normal, warning, low, total: pharmacyStockList.length };
}
