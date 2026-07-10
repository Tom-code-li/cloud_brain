<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemDetail, executeOrder } from '../api/medicalExamOrder.js'
import { getItemSchema } from '../api/medicalExamSchema.js'
import { saveExamResult } from '../api/medicalExamResult.js'
import { generateDraft, publishReport, rejectReport } from '../api/medicalExamReport.js'
import { showToast } from '../stores/medicalExamToast.js'
import { fmtTime, calcAge } from '../utils/medicalExamFormat.js'
import { resolveOrderProgressStage, resolveStage } from '../utils/medicalExamStatusMap.js'

const route  = useRoute()
const router = useRouter()
const orderItemId = Number(route.params.orderItemId)

const detail = ref(null)
const schema = ref(null)
const ecgForm    = ref({})
const ctFindings = ref([])
const ctNotes    = ref('')
const fallbackNotes = ref('')
const doctorConclusion = ref('')
const busy = ref(false)
const notFound = ref(false)

// 1=payment 2=execute 3=result 4=review 5=published
const stage = computed(() => resolveStage(detail.value))
const progressStage = computed(() => resolveOrderProgressStage(detail.value))
const schemaItemName = computed(() => normalizeExamItemName(detail.value?.itemName))
const schemaType = computed(() => schema.value?.schemaType ?? null)
const resultHint = computed(() => {
  if (schemaType.value === 'ecg') return '填写测量参数与心律判读'
  if (schemaType.value === 'ct') return '勾选影像所见，可补充文字描述'
  return '当前项目缺少结构化模板，可先填写检查描述后生成报告草稿'
})
const isResultReadonly = computed(() => stage.value >= 4)
const hasResultDetail = computed(() => Boolean(buildResultDetail().trim()))
const saveButtonText = computed(() => schema.value ? '保存检查结果' : '暂存检查描述')
const reportStatus = computed(() => (detail.value?.reportStatus ?? '').trim())
const hasDraftReport = computed(() => reportStatus.value === '草稿')
const isPublished = computed(() => isFinalReportStatus(reportStatus.value))
const aiButtonText = computed(() => {
  if (isPublished.value) return '报告已发布'
  if (hasDraftReport.value) return '重新生成检查报告初稿'
  return '生成检查报告初稿'
})
const reportBadge = computed(() => {
  if (isPublished.value) return reportStatus.value === '已回阅' ? '已回阅' : '已发布'
  if (hasDraftReport.value) return 'AI生成 · 待医生确认'
  return '待填写'
})

const STEPS = ['核对申请', '缴费确认', '执行检查', '结果录入', '审核发布']

function stepCls(n) {
  const cur = progressStage.value + 1
  if (n < cur) return 'is-done'
  if (n === cur) return 'is-current'
  return ''
}

function isFinalReportStatus(status) {
  return status === '已发布' || status === '已回阅'
}

async function load() {
  notFound.value = false
  schema.value = null
  doctorConclusion.value = ''
  try {
    detail.value = await getItemDetail(orderItemId)
    if (!detail.value) { notFound.value = true; return }
  } catch(e) {
    notFound.value = true
    return
  }
  try {
    schema.value = await getItemSchema(schemaItemName.value)
    if (schema.value) initForm(schema.value)
  } catch(_) {
    schema.value = null
    initFallback()
  }
  if (detail.value.resultFeatures?.length) rebuildForm()
  if (!schema.value) initFallback()
  doctorConclusion.value = detail.value.conclusion || ''
}

function initForm(s) {
  if (s.schemaType === 'ecg') {
    const v = {}
    s.fields.forEach(f => { v[f.key] = f.def ?? '' })
    v.rhythm = s.rhythmOptions?.[0] ?? ''
    v.axis   = s.axisOptions?.[0] ?? ''
    ecgForm.value = v
  } else {
    ctFindings.value = []
    ctNotes.value = ''
  }
  initFallback()
}

function initFallback() {
  fallbackNotes.value = detail.value?.findings || detail.value?.sampleId || ''
}

function rebuildForm() {
  const features = detail.value.resultFeatures ?? []
  if (schema.value?.schemaType === 'ecg') {
    schema.value.fields.forEach(f => {
      const hit = features.find(r => featureName(r) === f.label)
      if (hit) ecgForm.value[f.key] = featureValue(hit)
    })
    const rhythm = features.find(r => featureName(r) === '心律')
    const axis = features.find(r => featureName(r) === '电轴')
    if (rhythm) ecgForm.value.rhythm = featureValue(rhythm)
    if (axis) ecgForm.value.axis = featureValue(axis)
  } else if (schema.value?.schemaType === 'ct') {
    ctFindings.value = features
      .filter(f => featureName(f) !== '补充描述')
      .map(f => featureName(f))
      .filter(Boolean)
    const notes = features.find(f => featureName(f) === '补充描述')
    ctNotes.value = notes ? featureValue(notes) : (detail.value.findings || '')
  }
}

onMounted(load)

async function doExecute() {
  busy.value = true
  try {
    await executeOrder(orderItemId)
    showToast('检查已开始执行')
    await load()
  } finally { busy.value = false }
}

async function doSaveResult() {
  busy.value = true
  try {
    const s = schema.value
    let resultData
    if (!s) {
      showToast('当前项目缺少结构化模板，请填写描述后直接生成报告草稿')
      return
    } else if (s.schemaType === 'ecg') {
      resultData = {}
      s.fields.forEach(f => { resultData[f.key] = ecgForm.value[f.key] ?? '' })
      resultData.rhythm = ecgForm.value.rhythm ?? ''
      resultData.axis = ecgForm.value.axis ?? ''
    } else {
      resultData = { findings: ctFindings.value, notes: ctNotes.value }
    }
    await saveExamResult({ orderItemId, itemName: schemaItemName.value, resultData })
    showToast('检查结果已保存')
    await load()
  } finally { busy.value = false }
}

function buildResultDetail() {
  const s = schema.value
  if (!s) return fallbackNotes.value || detail.value?.findings || detail.value?.sampleId || ''
  if (s.schemaType === 'ecg') {
    const parts = s.fields
      .map(f => {
        const value = ecgForm.value[f.key]
        return value === '' || value == null ? '' : `${f.label}${value}${f.unit}`
      })
      .filter(Boolean)
    const extras = [ecgForm.value.rhythm, ecgForm.value.axis].filter(Boolean)
    return [parts.join('，'), extras.join('，')].filter(Boolean).join('。') + '。'
  }
  const findings = ctFindings.value.length ? ctFindings.value.join('；') : ''
  const notes = ctNotes.value.trim()
  return [findings, notes].filter(Boolean).join('。') + (findings || notes ? '。' : '')
}

async function doGenerateDraft() {
  const resultDetail = buildResultDetail().trim()
  if (!resultDetail) {
    showToast('请先录入检查结果或检查描述')
    return
  }
  busy.value = true
  try {
    const result = await generateDraft({ orderItemId, resultDetail, aiReportContent: '' })
    detail.value = {
      ...detail.value,
      reportId: result.reportId,
      reportStatus: result.status,
      findings: result.findings,
      conclusion: result.conclusion,
      aiDraft: result.aiDraft,
      doctorReview: result.doctorReview,
      publishedAt: result.publishedAt
    }
    doctorConclusion.value = result.conclusion || ''
    showToast('AI 已生成报告初稿，请审核后发布')
  } finally { busy.value = false }
}

async function doPublish() {
  if (!doctorConclusion.value.trim()) { showToast('请填写医生结论后再发布'); return }
  busy.value = true
  try {
    await publishReport({ reportId: detail.value.reportId, doctorConclusion: doctorConclusion.value })
    showToast('报告已发布并同步')
    await load()
  } finally { busy.value = false }
}

async function doReject() {
  busy.value = true
  try {
    await rejectReport(orderItemId)
    showToast('已退回，请重新核对并录入检查结果')
    await load()
    doctorConclusion.value = ''
  } finally { busy.value = false }
}

function toggleFinding(opt) {
  const i = ctFindings.value.indexOf(opt)
  if (i === -1) ctFindings.value.push(opt)
  else ctFindings.value.splice(i, 1)
}

function normalizeExamItemName(name = '') {
  if (name.includes('心电图')) return '心电图'
  if (name.includes('胸部DR') || name.includes('胸部CT') || name.includes('胸部正位片') || name.includes('DR正位片')) return '胸部CT'
  return name
}

function featureName(row) {
  return row?.featureName ?? row?.feature_name ?? ''
}

function featureValue(row) {
  return row?.featureValue ?? row?.feature_value ?? ''
}
</script>

<template>
  <div class="page medical-exam-page">

    <!-- Not found -->
    <main v-if="notFound" class="container">
      <div class="empty-state" style="padding:60px 0;">
        未找到该检查申请。<br>
        <button class="btn btn-outline btn-sm" style="margin-top:12px;" @click="router.push('/exam')">返回检查工作台</button>
      </div>
    </main>

    <!-- Loading -->
    <main v-else-if="!detail" class="container">
      <div class="empty-state" style="padding:60px 0;">加载中…</div>
    </main>

    <!-- Main content -->
    <main v-else class="container">
      <div class="page-head detail-head">
        <div>
          <span class="eyebrow">检查详情</span>
          <h1>检查结果录入与报告发布</h1>
          <p>核对申请、执行检查、录入结构化结果，并通过 AI 生成报告初稿。</p>
        </div>
        <button class="btn btn-outline" @click="router.push('/exam')">返回检查工作台</button>
      </div>

      <!-- Stepper -->
      <div class="stepper">
        <div v-for="(label, i) in STEPS" :key="i" class="step" :class="stepCls(i + 1)">
          <span class="num">{{ stepCls(i+1) === 'is-done' ? '✓' : i + 1 }}</span>
          <span>{{ label }}</span>
        </div>
      </div>

      <!-- Patient Banner -->
      <div class="card patient-banner">
        <div>
          <div class="pname">
            {{ detail.patientName }}
            <span style="font-weight:400;color:var(--his-text-soft);font-size:13px;">{{ detail.gender }} · {{ calcAge(detail.birthday) }}岁</span>
          </div>
          <div class="pmeta">{{ detail.executeDeptName }} · {{ detail.applyDoctorName }}医生申请 · 申请时间 {{ fmtTime(detail.appliedAt) }}</div>
        </div>
        <div class="pright">
          <div class="item-name">{{ detail.itemName }}</div>
          <div>检查目的：{{ detail.purpose }}</div>
        </div>
      </div>
      <div class="card patient-extra">
        <div><span class="lbl">临床诊断</span><span class="val">{{ detail.clinicalDiagnosis }}</span></div>
        <div><span class="lbl">检查项目</span><span class="val">{{ detail.itemName }}</span></div>
        <div><span class="lbl">费用状态</span><span class="val">{{ detail.feeStatus }}</span></div>
        <div><span class="lbl">当前状态</span><span class="val">{{ detail.orderStatus || '—' }}</span></div>
      </div>

      <div class="detail-grid">
        <!-- Left column -->
        <div>
          <!-- Payment Alert -->
          <div v-if="stage === 1" class="alert alert-warn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;margin-top:1px;">
              <path d="M12 9v4m0 4h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <div>
              <div class="alert-title">该患者尚未完成缴费</div>
              <p>请先提示患者到收费处完成缴费，缴费成功后方可执行检查。</p>
              <button class="btn btn-outline btn-sm" @click="router.push('/exam')">提示患者缴费并返回列表</button>
            </div>
          </div>

          <!-- Execute -->
          <div class="card section-card" :style="stage < 2 ? 'opacity:.55' : ''">
            <h2><span style="color:var(--his-primary-dark);">②</span> 执行检查</h2>
            <div class="hint">确认患者身份及检查部位后开始执行。</div>
            <div class="body">
              <span v-if="stage > 2" class="badge badge-progress"><span class="badge-dot"></span>已开始执行</span>
              <button v-else-if="stage === 2" class="btn btn-primary" :disabled="busy" @click="doExecute">开始执行检查</button>
            </div>
          </div>

          <!-- Result Form -->
          <div class="card section-card" :style="stage < 3 ? 'opacity:.55' : ''">
            <h2>③ 结果录入 <span class="badge badge-item">{{ detail.itemName }}</span></h2>
            <div class="hint">{{ resultHint }}</div>
            <div v-if="stage >= 3" class="body" :style="isResultReadonly ? 'pointer-events:none;opacity:.7' : ''">

              <!-- ECG Form -->
              <template v-if="schemaType === 'ecg'">
                <div class="form-grid">
                  <div v-for="f in schema.fields" :key="f.key" class="field">
                    <label>{{ f.label }}<span class="unit-suffix">参考 {{ f.low }}-{{ f.high }}{{ f.unit }}</span></label>
                    <input type="number" v-model="ecgForm[f.key]" />
                  </div>
                  <div class="field">
                    <label>心律</label>
                    <select v-model="ecgForm.rhythm">
                      <option v-for="o in schema.rhythmOptions" :key="o">{{ o }}</option>
                    </select>
                  </div>
                  <div class="field">
                    <label>电轴</label>
                    <select v-model="ecgForm.axis">
                      <option v-for="o in schema.axisOptions" :key="o">{{ o }}</option>
                    </select>
                  </div>
                </div>
              </template>

              <!-- CT Form -->
              <template v-else-if="schemaType === 'ct'">
                <div class="field" style="margin-bottom:14px;">
                  <label>影像所见（可多选）</label>
                  <div class="checkbox-grid">
                    <label
                      v-for="opt in schema.findingOptions" :key="opt"
                      class="cbx-chip" :class="{ 'is-checked': ctFindings.includes(opt) }"
                      @click.prevent="toggleFinding(opt)"
                    >
                      <input type="checkbox" :checked="ctFindings.includes(opt)" @change.stop />{{ opt }}
                    </label>
                  </div>
                </div>
                <div class="field">
                  <label>补充描述（选填）</label>
                  <textarea v-model="ctNotes" placeholder="如：右肺上叶见直径约6mm结节，边缘光整…"></textarea>
                </div>
              </template>

              <!-- Fallback: unsupported schema -->
              <template v-else>
                <div class="field">
                  <label>影像所见 / 检查描述</label>
                  <textarea v-model="fallbackNotes" placeholder="请填写检查所见及描述…"></textarea>
                </div>
              </template>

              <button v-if="stage === 3" class="btn btn-primary" style="margin-top:14px;" :disabled="busy" @click="doSaveResult">
                {{ saveButtonText }}
              </button>
            </div>
          </div>

          <!-- Report Section -->
          <div class="card section-card" :style="stage < 4 ? 'opacity:.55' : ''">
            <h2>
              ④ 审核与发布
              <span v-if="isPublished" class="badge badge-published"><span class="badge-dot"></span>{{ reportBadge }}</span>
              <span v-else-if="stage >= 4" class="badge badge-ai">{{ reportBadge }}</span>
              <span v-else class="badge badge-pending">{{ reportBadge }}</span>
            </h2>
            <div class="hint">可直接编辑报告结论，确认无误后发布；发布后将同步给门诊医生与患者端。</div>
            <div v-if="stage >= 4" class="body">
              <template v-if="detail.aiDraft">
                <label style="display:block;font-size:12.5px;color:var(--his-text-soft);font-weight:600;margin-bottom:5px;">AI 生成草稿（仅供参考）</label>
                <textarea class="report-textarea" disabled :value="detail.aiDraft"></textarea>
              </template>
              <template v-if="!isPublished">
                <label style="display:block;font-size:12.5px;color:var(--his-text-soft);font-weight:600;margin:14px 0 5px;">医生结论</label>
                <textarea class="report-textarea" v-model="doctorConclusion" placeholder="请在此填写最终诊断结论…" style="min-height:100px;"></textarea>
                <div class="report-actions">
                  <button class="btn btn-primary" :disabled="busy" @click="doPublish">通过并发布报告</button>
                  <button class="btn btn-danger-outline" :disabled="busy" @click="doReject">不通过，退回重新录入</button>
                </div>
              </template>
              <template v-else>
                <label style="display:block;font-size:12.5px;color:var(--his-text-soft);font-weight:600;margin:14px 0 5px;">医生最终结论</label>
                <textarea class="report-textarea" disabled :value="detail.conclusion || '—'" style="min-height:100px;"></textarea>
              </template>
              <div v-if="isPublished" class="published-banner">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 6 9 17l-5-5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                报告已于 {{ fmtTime(detail.publishedAt) }} 发布，并同步给门诊医生和患者端。
              </div>
            </div>
          </div>
        </div>

        <!-- AI Rail -->
        <div class="ai-rail">
          <div class="ai-panel">
            <h2><span class="ai-mark">AI</span> AI 检查报告生成辅助</h2>
            <p class="desc">基于已录入的测量值 / 影像所见，自动生成结构化报告初稿，医生仍需审核修改后发布。</p>
            <button
              class="btn btn-ai btn-block"
              :disabled="stage < 3 || isPublished || busy || !hasResultDetail"
              @click="doGenerateDraft"
            >{{ aiButtonText }}</button>
          </div>

          <div class="card timeline-panel">
            <h2>处理记录</h2>
            <ul class="timeline-list">
              <li v-if="detail.publishedAt">
                <span class="t-dot"></span>
                <span><span class="t-time">{{ fmtTime(detail.publishedAt) }}</span>报告已发布</span>
              </li>
              <li v-if="detail.executedAt">
                <span class="t-dot"></span>
                <span><span class="t-time">{{ fmtTime(detail.executedAt) }}</span>检查开始执行</span>
              </li>
              <li v-if="detail.appliedAt">
                <span class="t-dot"></span>
                <span><span class="t-time">{{ fmtTime(detail.appliedAt) }}</span>申请已生成</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
