<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemDetail, confirmSample } from '../api/medicalExamOrder.js'
import { getItemSchema } from '../api/medicalExamSchema.js'
import { saveLabResult } from '../api/medicalExamResult.js'
import { generateDraft, publishReport, rejectReport } from '../api/medicalExamReport.js'
import { showToast } from '../stores/medicalExamToast.js'
import { fmtTime, calcAge } from '../utils/medicalExamFormat.js'
import { abnormalFlagClass, abnormalFlagLabel, resolveOrderProgressStage } from '../utils/medicalExamStatusMap.js'

const route = useRoute()
const router = useRouter()
const orderItemId = Number(route.params.orderItemId)

const detail = ref(null)
const schema = ref(null)
const values = ref({})
const sampleInput = ref('')
const idChecked = ref(false)
const reportText = ref('')
const busy = ref(false)
const notFound = ref(false)

const STEPS = ['核对申请', '缴费确认', '采集/确认样本', '结果录入', '审核发布']

function isFinalReportStatus(status) {
  return status === '已发布' || status === '已回阅'
}

const stage = computed(() => {
  const d = detail.value
  if (!d) return 0
  const reportStatus = (d.reportStatus ?? '').trim()
  if (d.feeStatus !== '已支付' || d.orderStatus === '待缴费') return 1
  if (d.orderStatus === '待执行') return 2
  if (isFinalReportStatus(reportStatus)) return 6
  if (reportStatus === '草稿') return 5
  if (hasLabResult.value) return 4
  return 3
})
const progressStage = computed(() => resolveOrderProgressStage(detail.value))
const sampleId = computed(() => {
  const summary = detail.value?.sampleId || ''
  return summary === '检验结果已录入' ? '' : summary
})
const hasLabResult = computed(() => (detail.value?.labResultItems ?? []).length > 0 || detail.value?.sampleId === '检验结果已录入')
const sampleLabel = computed(() => hasLabResult.value && !sampleId.value ? '已采集' : (sampleId.value || '未采集'))
const canEditResult = computed(() => stage.value === 3)
const reportStatus = computed(() => (detail.value?.reportStatus ?? '').trim())
const isPublished = computed(() => isFinalReportStatus(reportStatus.value))
const resultReady = computed(() => stage.value >= 4)
const aiReady = computed(() => stage.value >= 3 && !isPublished.value && Boolean(schema.value))
const aiButtonText = computed(() => {
  if (isPublished.value) return '报告已发布'
  if (reportStatus.value === '草稿') return '重新生成检验结果解读'
  return '生成检验结果解读'
})
const reportBadge = computed(() => {
  if (isPublished.value) return '已发布'
  if (reportStatus.value === '草稿') return 'AI解读 · 待医生确认'
  return '待填写'
})

function stepCls(n) {
  const cur = Math.min(progressStage.value + 1, 6)
  if (n < cur) return 'is-done'
  if (n === cur) return 'is-current'
  return ''
}

async function load() {
  notFound.value = false
  schema.value = null
  reportText.value = ''
  try {
    detail.value = await getItemDetail(orderItemId)
    if (!detail.value) { notFound.value = true; return }
  } catch (_) {
    notFound.value = true
    return
  }

  try {
    schema.value = await getItemSchema(detail.value.itemName)
    initValues()
  } catch (_) {
    schema.value = null
  }
  sampleInput.value = sampleId.value || suggestedSampleId()
  reportText.value = detail.value.conclusion || detail.value.aiDraft || ''
}

function initValues() {
  const init = {}
  const saved = detail.value?.labResultItems ?? []
  ;(schema.value?.fields ?? []).forEach(f => {
    const hit = saved.find(r => indicatorCode(r) === f.key || indicatorName(r) === f.label)
    init[f.key] = hit ? resultValue(hit) : (f.def ?? '')
  })
  values.value = init
}

function suggestedSampleId() {
  const d = new Date()
  const p = n => String(n).padStart(2, '0')
  return `LB${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}-${orderItemId}`
}

onMounted(load)

async function doConfirmSample() {
  if (!idChecked.value) { showToast('请先核对患者身份信息'); return }
  const sid = sampleInput.value.trim()
  if (!sid) { showToast('样本编号不能为空'); return }
  busy.value = true
  try {
    await confirmSample(orderItemId, sid)
    showToast('样本采集已确认')
    await load()
  } finally { busy.value = false }
}

async function doSaveResult() {
  if (!schema.value) { showToast('当前检验项目缺少录入模板'); return }
  busy.value = true
  try {
    const payload = {}
    schema.value.fields.forEach(f => { payload[f.key] = values.value[f.key] ?? '' })
    await saveLabResult({ orderItemId, itemName: detail.value.itemName, values: payload })
    showToast('检验结果已保存')
    await load()
  } finally { busy.value = false }
}

function buildResultDetail() {
  const fields = schema.value?.fields ?? []
  const lines = fields.map(f => {
    const value = values.value[f.key]
    const flag = calcFlag(f, value)
    const unit = f.unit ? ` ${f.unit}` : ''
    const ref = referenceRange(f)
    return `${f.label}: ${value || f.def || '—'}${unit}，参考范围 ${ref}，判定 ${abnormalFlagLabel(flag)}`
  })
  return `本次${detail.value?.itemName || '检验'}结果：${lines.join('；')}。`
}

async function doGenerateDraft() {
  if (!resultReady.value) { showToast('请先保存检验结果'); return }
  busy.value = true
  try {
    const result = await generateDraft({ orderItemId, resultDetail: buildResultDetail(), aiReportContent: '' })
    reportText.value = result.conclusion || result.aiDraft || ''
    showToast('AI 已生成解读草稿，请审核后发布')
    await load()
  } finally { busy.value = false }
}

async function doPublish() {
  const text = reportText.value.trim()
  if (!text) { showToast('报告内容不能为空'); return }
  busy.value = true
  try {
    await publishReport({ reportId: detail.value.reportId, doctorConclusion: text })
    showToast('报告已发布并同步')
    await load()
  } finally { busy.value = false }
}

async function doReject() {
  busy.value = true
  try {
    await rejectReport(orderItemId)
    reportText.value = ''
    showToast('已退回，请重新核对并录入检验结果')
    await load()
  } finally { busy.value = false }
}

function calcFlag(field, val) {
  if (val === '' || val == null) return null
  if (field.options?.length) return val === field.normal ? 'NORMAL' : 'ABNORMAL'
  const n = parseFloat(val)
  if (Number.isNaN(n)) return null
  const lo = parseFloat(field.low)
  const hi = parseFloat(field.high)
  if (n < lo) return 'LOW'
  if (n > hi) return 'HIGH'
  return 'NORMAL'
}

function referenceRange(field) {
  if (field.options?.length) return field.normal || '—'
  if (field.low == null && field.high == null) return '—'
  return `${field.low} - ${field.high}`
}

function indicatorCode(row) {
  return row?.indicatorCode ?? row?.indicator_code ?? ''
}

function indicatorName(row) {
  return row?.indicatorName ?? row?.indicator_name ?? ''
}

function resultValue(row) {
  return row?.resultValue ?? row?.result_value ?? ''
}
</script>

<template>
  <div class="page medical-exam-page">

    <main v-if="notFound" class="container">
      <div class="empty-state" style="padding:60px 0;">
        未找到该检验申请。<br>
        <button class="btn btn-outline btn-sm" style="margin-top:12px;" @click="router.push('/lab')">返回检验工作台</button>
      </div>
    </main>

    <main v-else-if="!detail" class="container">
      <div class="empty-state" style="padding:60px 0;">加载中…</div>
    </main>

    <main v-else class="container">
      <div class="page-head detail-head">
        <div>
          <span class="eyebrow">检验详情</span>
          <h1>检验结果录入与报告发布</h1>
          <p>确认样本、录入检验指标，并通过 AI 生成结果解读草稿。</p>
        </div>
        <button class="btn btn-outline" @click="router.push('/lab')">返回检验工作台</button>
      </div>

      <div class="stepper">
        <div v-for="(label, i) in STEPS" :key="label" class="step" :class="stepCls(i + 1)">
          <span class="num">{{ stepCls(i + 1) === 'is-done' ? '✓' : i + 1 }}</span>
          <span>{{ label }}</span>
        </div>
      </div>

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
          <div>检验目的：{{ detail.purpose }}</div>
        </div>
      </div>
      <div class="card patient-extra">
        <div><span class="lbl">临床诊断</span><span class="val">{{ detail.clinicalDiagnosis }}</span></div>
        <div><span class="lbl">检验项目</span><span class="val">{{ detail.itemName }}</span></div>
        <div><span class="lbl">样本编号</span><span class="val">{{ sampleLabel }}</span></div>
        <div><span class="lbl">当前状态</span><span class="val">{{ detail.orderStatus || '—' }}</span></div>
      </div>

      <div class="detail-grid">
        <div>
          <div v-if="stage === 1" class="alert alert-warn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;margin-top:1px;">
              <path d="M12 9v4m0 4h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <div>
              <div class="alert-title">该患者尚未完成缴费</div>
              <p>请先提示患者到收费处完成缴费，缴费成功后方可采集样本并执行检验。</p>
              <button class="btn btn-outline btn-sm" @click="router.push('/lab')">提示患者缴费并返回列表</button>
            </div>
          </div>

          <div class="card section-card" :style="stage < 2 ? 'opacity:.55' : ''">
            <h2>
              <span style="color:var(--his-primary-dark);">②</span> 采集/确认样本
              <span v-if="sampleId || hasLabResult" class="badge badge-progress"><span class="badge-dot"></span>{{ sampleId ? '样本编号 ' + sampleId : '样本已采集' }}</span>
            </h2>
            <div class="hint">{{ sampleId || hasLabResult ? '已完成样本采集与检验执行。' : '核对患者信息后采集样本，并登记样本编号。' }}</div>
            <div v-if="stage === 2" class="body">
              <div class="form-grid">
                <div class="field"><label>样本编号</label><input type="text" v-model="sampleInput" /></div>
                <div class="field" style="display:flex;align-items:flex-end;">
                  <label class="check-row"><input type="checkbox" v-model="idChecked" /> 已核对患者身份信息</label>
                </div>
              </div>
              <button class="btn btn-primary" style="margin-top:14px;" :disabled="busy" @click="doConfirmSample">确认样本采集</button>
            </div>
          </div>

          <div class="card section-card" :style="stage < 3 ? 'opacity:.55' : ''">
            <h2>③ 结果录入 <span class="badge badge-item">{{ detail.itemName }}</span></h2>
            <div class="hint">系统会根据参考范围自动标记异常结果。</div>
            <div v-if="stage >= 3" class="body" :style="stage >= 5 ? 'pointer-events:none;opacity:.7' : ''">
              <table v-if="schema" class="lab-table">
                <thead><tr><th>项目</th><th>结果</th><th>单位</th><th>参考范围</th><th>判定</th></tr></thead>
                <tbody>
                  <tr v-for="f in schema.fields" :key="f.key">
                    <td>{{ f.label }}</td>
                    <td>
                      <select v-if="f.options?.length" v-model="values[f.key]">
                        <option v-for="o in f.options" :key="o" :value="o">{{ o }}</option>
                      </select>
                      <input v-else type="number" step="0.01" v-model="values[f.key]" />
                    </td>
                    <td class="ref">{{ f.unit || '—' }}</td>
                    <td class="ref">{{ referenceRange(f) }}</td>
                    <td>
                      <span v-if="calcFlag(f, values[f.key])" class="flag" :class="abnormalFlagClass(calcFlag(f, values[f.key]))">
                        {{ abnormalFlagLabel(calcFlag(f, values[f.key])) }}
                      </span>
                      <span v-else class="flag flag-na">--</span>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-else class="empty-state" style="padding:20px 0;">未获取到该检验项目的录入模板</div>
              <button v-if="canEditResult" class="btn btn-primary" style="margin-top:16px;" :disabled="busy || !schema" @click="doSaveResult">
                保存检验结果
              </button>
            </div>
          </div>

          <div class="card section-card" :style="stage < 5 ? 'opacity:.55' : ''">
            <h2>
              ④ 审核与发布
              <span v-if="isPublished" class="badge badge-published"><span class="badge-dot"></span>{{ reportBadge }}</span>
              <span v-else-if="stage === 5" class="badge badge-ai">{{ reportBadge }}</span>
              <span v-else class="badge badge-pending">{{ reportBadge }}</span>
            </h2>
            <div class="hint">可直接编辑报告内容，确认无误后发布；发布后将同步给门诊医生与患者端。</div>
            <div v-if="stage >= 5" class="body">
              <template v-if="detail.aiDraft">
                <label style="display:block;font-size:12.5px;color:var(--his-text-soft);font-weight:600;margin-bottom:5px;">AI 解读草稿（仅供参考）</label>
                <textarea class="report-textarea" disabled :value="detail.aiDraft"></textarea>
              </template>
              <label style="display:block;font-size:12.5px;color:var(--his-text-soft);font-weight:600;margin:14px 0 5px;">医生最终结论</label>
              <textarea
                class="report-textarea"
                v-model="reportText"
                :disabled="isPublished"
                placeholder="点击右侧“AI解读检验结果”，或在此直接撰写报告内容…"
              ></textarea>
              <div v-if="!isPublished" class="report-actions">
                <button class="btn btn-primary" :disabled="busy" @click="doPublish">通过并发布报告</button>
                <button class="btn btn-danger-outline" :disabled="busy" @click="doReject">不通过，退回重新录入</button>
              </div>
              <div v-else class="published-banner">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 6 9 17l-5-5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                报告已于 {{ fmtTime(detail.publishedAt) }} 发布，并同步给门诊医生和患者端。
              </div>
            </div>
          </div>
        </div>

        <div class="ai-rail">
          <div class="ai-panel">
            <h2><span class="ai-mark">AI</span> AI 检验结果解读</h2>
            <p class="desc">自动比对参考范围、标记异常指标，并生成解读草稿，医生仍需审核修改后发布。</p>
            <button class="btn btn-ai btn-block" :disabled="!aiReady || busy" @click="doGenerateDraft">
              {{ aiButtonText }}
            </button>
          </div>

          <div class="card timeline-panel">
            <h2>处理记录</h2>
            <ul class="timeline-list">
              <li v-if="detail.publishedAt">
                <span class="t-dot"></span>
                <span><span class="t-time">{{ fmtTime(detail.publishedAt) }}</span>报告已发布</span>
              </li>
              <li v-if="hasLabResult">
                <span class="t-dot"></span>
                <span><span class="t-time">{{ fmtTime(detail.executedAt) }}</span>检验结果已录入</span>
              </li>
              <li v-if="detail.executedAt">
                <span class="t-dot"></span>
                <span><span class="t-time">{{ fmtTime(detail.executedAt) }}</span>样本已采集</span>
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
