<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Tickets } from '@element-plus/icons-vue'
import {
  getRegistrationDepartments,
  getRegistrationDoctors,
  getRegistrationSchedules,
  submitOfflineRegistration,
  syncPatient
} from '../../api/registration'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const aiLoadingScene = ref('')
const result = ref(null)
const patient = ref(null)
const departments = ref([])
const doctors = ref([])
const schedules = ref([])
const auth = useAuthStore()
const aiSuggestion = ref('请输入主诉后点击 AI 分诊建议。')
const aiScenes = [
  { sceneCode: 'TRIAGE', label: 'AI 分诊建议' },
  { sceneCode: 'DEPARTMENT_RECOMMEND', label: '推荐科室' },
  { sceneCode: 'DOCTOR_RECOMMEND', label: '推荐医生' }
]

function formatLocalDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function buildRegistrationAiStreamUrl({ sceneCode, businessId, patientId, query }, direct = false) {
  const baseUrl = direct ? 'http://127.0.0.1:9600' : '/api'
  return `${baseUrl}/ai/registration/stream`
}

function aiRequestHeaders(includeAuth) {
  const headers = {
    'Content-Type': 'application/json',
    'X-Doctor-Id': String(auth.doctor?.doctorId || '')
  }
  if (includeAuth && auth.token && !auth.token.startsWith('demo-token-')) {
    headers.Authorization = `Bearer ${auth.token}`
  }
  return headers
}

async function fetchRegistrationAiStream(params) {
  const useGateway = Boolean(auth.token && !auth.token.startsWith('demo-token-'))
  let response = await fetch(buildRegistrationAiStreamUrl(params, !useGateway), {
    method: 'POST',
    headers: aiRequestHeaders(useGateway),
    body: JSON.stringify(params)
  })
  if (!response.ok && useGateway && [401, 403].includes(response.status)) {
    response = await fetch(buildRegistrationAiStreamUrl(params, true), {
      method: 'POST',
      headers: aiRequestHeaders(false),
      body: JSON.stringify(params)
    })
  }
  return response
}

const form = reactive({
  patientName: '',
  gender: '',
  idCard: '',
  phone: '',
  allergyHistory: '',
  pastHistory: '',
  chiefComplaint: '',
  deptId: null,
  doctorId: null,
  scheduleId: null,
  workDate: formatLocalDate()
})

const outpatientDepartments = computed(() => departments.value.filter((item) => item.deptType === 'OUTPATIENT'))
const selectedSchedule = computed(() => schedules.value.find((item) => item.scheduleId === form.scheduleId))

function isScheduleAvailable(schedule) {
  return schedule?.status === '可预约' && Number(schedule?.remainQuota || 0) > 0
}

function validatePatientForm({ includeChiefComplaint = true } = {}) {
  if (!form.patientName.trim()) {
    ElMessage.warning('请填写患者姓名')
    return false
  }
  if (!form.gender) {
    ElMessage.warning('请选择患者性别')
    return false
  }
  if (!form.phone.trim()) {
    ElMessage.warning('请填写联系电话')
    return false
  }
  if (includeChiefComplaint && !form.chiefComplaint.trim()) {
    ElMessage.warning('请填写患者主诉')
    return false
  }
  return true
}

async function loadDepartments() {
  departments.value = await getRegistrationDepartments()
  if (!outpatientDepartments.value.some((item) => item.deptId === form.deptId)) {
    form.deptId = outpatientDepartments.value[0]?.deptId || null
  }
}

async function loadDoctors() {
  if (!form.deptId) return
  doctors.value = await getRegistrationDoctors({ deptId: form.deptId })
  if (!doctors.value.some((item) => item.doctorId === form.doctorId)) {
    form.doctorId = doctors.value[0]?.doctorId || null
  }
}

async function loadSchedules() {
  if (!form.deptId && !form.doctorId) return
  schedules.value = await getRegistrationSchedules({
    deptId: form.deptId,
    doctorId: form.doctorId,
    workDate: form.workDate
  })
  if (!schedules.value.some((item) => item.scheduleId === form.scheduleId && isScheduleAvailable(item))) {
    form.scheduleId = schedules.value.find(isScheduleAvailable)?.scheduleId || null
  }
}

async function syncCurrentPatient() {
  if (!validatePatientForm({ includeChiefComplaint: false })) {
    return null
  }
  patient.value = await syncPatient({
    patientName: form.patientName,
    gender: form.gender,
    idCard: form.idCard,
    phone: form.phone,
    allergyHistory: form.allergyHistory,
    pastHistory: form.pastHistory
  })
  ElMessage.success(`患者已同步：${patient.value.patientNo}`)
  return patient.value
}

async function submit() {
  if (!validatePatientForm()) {
    return
  }
  if (!form.doctorId || !form.scheduleId) {
    ElMessage.warning('请先选择医生和排班')
    return
  }
  if (!isScheduleAvailable(selectedSchedule.value)) {
    ElMessage.warning('请选择可预约且仍有余号的排班')
    return
  }
  loading.value = true
  try {
    const currentPatient = patient.value || await syncCurrentPatient()
    result.value = await submitOfflineRegistration({
      patientId: currentPatient.patientId,
      doctorId: form.doctorId,
      scheduleId: form.scheduleId
    })
    ElMessage.success('线下挂号已提交，请完成缴费')
  } finally {
    loading.value = false
  }
}

function appendSseData(block) {
  const data = block
    .split(/\r?\n/)
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trimStart())
    .join('\n')
  if (data) {
    aiSuggestion.value += data
  }
}

async function requestAiSuggestion(sceneCode = 'TRIAGE', label = 'AI 分诊建议') {
  if (!form.chiefComplaint.trim()) {
    ElMessage.warning('请先填写患者主诉')
    return
  }
  aiLoadingScene.value = sceneCode
  aiSuggestion.value = `${label}：\n`
  const query = JSON.stringify({
    patientName: form.patientName,
    gender: form.gender,
    chiefComplaint: form.chiefComplaint,
    allergyHistory: form.allergyHistory,
    pastHistory: form.pastHistory,
    departments: departments.value,
    doctors: doctors.value,
    schedules: schedules.value
  })
  try {
    const response = await fetchRegistrationAiStream({
      sceneCode,
      patientId: patient.value?.patientId,
      query
    })
    if (!response.ok || !response.body) {
      throw new Error('AI stream unavailable')
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { value, done } = await reader.read()
      buffer += decoder.decode(value || new Uint8Array(), { stream: !done })
      let separatorIndex = buffer.indexOf('\n\n')
      while (separatorIndex !== -1) {
        appendSseData(buffer.slice(0, separatorIndex))
        buffer = buffer.slice(separatorIndex + 2)
        separatorIndex = buffer.indexOf('\n\n')
      }
      if (done) {
        break
      }
    }
    if (buffer.trim()) {
      appendSseData(buffer)
    }
  } catch {
    if (aiSuggestion.value === `${label}：\n`) {
      aiSuggestion.value = 'AI 服务暂不可用，请按主诉人工选择科室和医生。'
    }
  } finally {
    if (aiLoadingScene.value === sceneCode) {
      aiLoadingScene.value = ''
    }
  }
}

watch(() => form.deptId, async () => {
  await loadDoctors()
  await loadSchedules()
})

watch(() => [form.doctorId, form.workDate], loadSchedules)

onMounted(async () => {
  await loadDepartments()
  await loadDoctors()
  await loadSchedules()
})
</script>

<template>
  <div class="offline-registration page-surface">
    <el-card class="medical-card" shadow="never">
      <template #header>
        <div class="toolbar-line">
          <div>
            <p class="section-title">线下挂号</p>
            <p class="section-subtitle">同步患者信息，选择科室、医生、排班后生成待缴费挂号单</p>
          </div>
          <el-tag type="primary" effect="plain">Offline</el-tag>
        </div>
      </template>
      <el-form class="flat-form" :model="form" label-position="top">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="姓名">
              <el-input v-model="form.patientName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="性别">
              <el-segmented v-model="form.gender" :options="['男', '女']" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="就诊日期">
              <el-date-picker v-model="form.workDate" value-format="YYYY-MM-DD" type="date" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="过敏史">
              <el-input v-model="form.allergyHistory" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="既往史">
              <el-input v-model="form.pastHistory" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="就诊科室">
              <el-select v-model="form.deptId" filterable>
                <el-option
                  v-for="dept in outpatientDepartments"
                  :key="dept.deptId"
                  :label="dept.deptName"
                  :value="dept.deptId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="医生">
              <el-select v-model="form.doctorId" filterable>
                <el-option
                  v-for="doctor in doctors"
                  :key="doctor.doctorId"
                  :label="`${doctor.doctorName}｜${doctor.title || '医生'}`"
                  :value="doctor.doctorId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排班">
              <el-select v-model="form.scheduleId" filterable>
                <el-option
                  v-for="schedule in schedules"
                  :key="schedule.scheduleId"
                  :label="`${schedule.workDate} ${schedule.timePeriod}｜余号 ${schedule.remainQuota}｜¥${schedule.registrationFee}`"
                  :value="schedule.scheduleId"
                  :disabled="!isScheduleAvailable(schedule)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="主诉">
              <el-input v-model="form.chiefComplaint" type="textarea" :rows="4" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="action-row">
          <el-button type="primary" :icon="Tickets" :loading="loading" @click="submit">提交挂号</el-button>
          <el-button plain @click="syncCurrentPatient">同步患者</el-button>
          <el-button
            v-for="scene in aiScenes"
            :key="scene.sceneCode"
            :icon="MagicStick"
            :loading="aiLoadingScene === scene.sceneCode"
            plain
            @click="requestAiSuggestion(scene.sceneCode, scene.label)"
          >
            {{ scene.label }}
          </el-button>
        </div>
      </el-form>
    </el-card>

    <el-card class="medical-card" shadow="never">
      <template #header>
        <div>
          <p class="section-title">挂号结果</p>
          <p class="section-subtitle">缴费成功后患者进入门诊候诊队列</p>
        </div>
      </template>
      <el-alert class="ai-box" title="AI 建议" type="info" :closable="false">
        <div class="ai-suggestion-text">{{ aiSuggestion }}</div>
      </el-alert>
      <el-empty v-if="!result" description="尚未提交挂号" />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="挂号单号">{{ result.registrationNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ patient?.patientName || form.patientName }}</el-descriptions-item>
        <el-descriptions-item label="费用状态">{{ result.feeStatus }}</el-descriptions-item>
        <el-descriptions-item label="业务状态">{{ result.status }}</el-descriptions-item>
        <el-descriptions-item label="挂号费">¥{{ selectedSchedule?.registrationFee || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.offline-registration {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.ai-box {
  margin-bottom: 16px;
}

.ai-suggestion-text {
  max-height: clamp(220px, 42vh, 420px);
  overflow-y: auto;
  overscroll-behavior: contain;
  padding-right: 8px;
  white-space: pre-line;
  line-height: 1.7;
}

.ai-suggestion-text::-webkit-scrollbar {
  width: 6px;
}

.ai-suggestion-text::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

@media (max-width: 960px) {
  .offline-registration {
    grid-template-columns: 1fr;
  }
}
</style>
