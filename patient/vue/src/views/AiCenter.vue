<template>
  <div class="patient-page ai-center-page">
    <div class="patient-topbar">
      <span @click="$router.push('/patient/home')" class="patient-back">返回</span>
      <span class="patient-title">AI 就医中心</span>
      <span></span>
    </div>

    <div class="ai-hero">
      <div>
        <div class="ai-eyebrow">AI 问诊 · AI 导诊 · AI 病情咨询</div>
        <h2>先说症状，再给建议</h2>
        <p>系统会先查数据库里的已有内容，再让 AI 从已有候选里选择，不会乱编不存在的信息。</p>
      </div>
    </div>

    <div class="patient-tabs ai-tabs">
      <button class="patient-tab" :class="{ active: tab === 'consult' }" @click="tab = 'consult'">AI 问诊</button>
      <button class="patient-tab" :class="{ active: tab === 'guide' }" @click="tab = 'guide'">AI 导诊</button>
      <button class="patient-tab" :class="{ active: tab === 'case' }" @click="tab = 'case'">病情咨询</button>
    </div>

    <div class="patient-content ai-content">
      <section class="ai-panel patient-card">
        <div class="panel-head">
          <span class="panel-title">{{ activeTitle }}</span>
          <span class="panel-badge">{{ activeHint }}</span>
        </div>

        <textarea v-model="inputText" class="ai-input" :placeholder="activePlaceholder" />

        <div class="ai-actions">
          <button class="patient-button" @click="submit">提交</button>
          <button class="patient-button secondary" @click="clear">清空</button>
        </div>
      </section>

      <section class="ai-panel patient-card">
        <div class="panel-head">
          <span class="panel-title">结果</span>
          <span class="panel-badge" :class="resultState">{{ resultStateLabel }}</span>
        </div>
        <div class="ai-result" v-if="resultText">{{ resultText }}</div>
        <div class="ai-empty" v-else>等待输入内容</div>
      </section>

      <section class="ai-panel patient-card">
        <div class="panel-head">
          <span class="panel-title">历史记录</span>
          <span class="panel-badge">本地缓存</span>
        </div>
        <div class="history-list">
          <button class="history-item" v-for="item in history" :key="item.id" @click="openHistory(item)">
            <div class="history-top">
              <span class="history-title">{{ item.title }}</span>
              <span class="history-type">{{ item.type }}</span>
            </div>
            <div class="history-text">{{ item.preview }}</div>
          </button>
          <div v-if="history.length === 0" class="ai-empty">暂无记录</div>
        </div>
      </section>

      <section v-if="selectedHistory" class="ai-panel patient-card">
        <div class="panel-head">
          <span class="panel-title">记录详情</span>
          <button class="detail-close" @click="selectedHistory = null">关闭</button>
        </div>
        <div class="detail-list">
          <div class="detail-row">
            <span>主诉</span>
            <p>{{ selectedHistory.raw.chiefComplaint || '未记录' }}</p>
          </div>
          <div class="detail-row">
            <span>症状详情</span>
            <p>{{ selectedHistory.raw.symptomDetail || '未记录' }}</p>
          </div>
          <div class="detail-row">
            <span>风险等级</span>
            <p>{{ selectedHistory.type }}</p>
          </div>
          <div class="detail-row">
            <span>推荐科室</span>
            <p>{{ selectedHistory.raw.recommendedDeptName || (selectedHistory.raw.recommendedDeptId ? `科室ID: ${selectedHistory.raw.recommendedDeptId}` : '未匹配') }}</p>
          </div>
          <div class="detail-row">
            <span>AI摘要</span>
            <p>{{ selectedHistory.raw.aiSummary || '未生成' }}</p>
          </div>
          <div class="detail-row">
            <span>AI结果</span>
            <p>{{ selectedHistory.raw.aiResult || '未生成' }}</p>
          </div>
          <div class="detail-row">
            <span>生成时间</span>
            <p>{{ selectedHistory.raw.createdAt || '未记录' }}</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import feedback from '@/utils/feedback'
import { riskLevelText } from '@/utils/statusLabels'

export default {
  data() {
    return {
      tab: 'consult',
      inputText: '',
      resultText: '',
      resultState: 'idle',
      patientId: null,
      history: [],
      selectedHistory: null
    }
  },
  computed: {
    activeTitle() {
      if (this.tab === 'guide') return 'AI 导诊'
      if (this.tab === 'case') return 'AI 病情咨询'
      return 'AI 问诊'
    },
    activeHint() {
      if (this.tab === 'guide') return '科室推荐'
      if (this.tab === 'case') return '病情分析'
      return '症状分析'
    },
    activePlaceholder() {
      if (this.tab === 'guide') return '请输入主要症状、持续时间、是否发热等信息'
      if (this.tab === 'case') return '请输入你的病情描述、检查结果或疑问'
      return '请输入你的不适症状，例如咳嗽、发热、腹痛'
    },
    resultStateLabel() {
      if (this.resultState === 'success') return '已生成'
      if (this.resultState === 'error') return '未完成'
      return '待提交'
    }
  },
  async mounted() {
    await this.loadPatient()
    await this.loadHistory()
  },
  methods: {
    async loadPatient() {
      const userId = sessionStorage.getItem('userId')
      if (!userId) return
      try {
        const res = await axios.get(`/api/patient/info/${userId}`)
        if (res.data.success && res.data.data) {
          this.patientId = res.data.data.patientId
        }
      } catch (e) {}
    },
    async loadHistory() {
      if (!this.patientId) return
      try {
        const res = await axios.get(`/api/ai/my/${this.patientId}`)
        if (res.data.success) {
          this.history = (res.data.data || []).map(item => ({
            id: item.consultationId,
            type: riskLevelText(item.riskLevel) || 'AI',
            title: item.chiefComplaint || '问诊记录',
            preview: item.aiSummary || item.aiResult || '',
            raw: item
          }))
        }
      } catch (e) {}
    },
    async submit() {
      const text = this.inputText.trim()
      if (!text) {
        this.resultState = 'error'
        this.resultText = '请先输入内容'
        return
      }
      if (!this.patientId) {
        feedback.toast('请先登录')
        return
      }
      this.resultState = 'idle'
      try {
        const res = await axios.post('/api/ai/smart', {
          patientId: this.patientId,
          mode: this.tab,
          content: text
        })
        if (res.data.success && res.data.data) {
          const data = res.data.data
          this.resultState = 'success'
          this.resultText = data.aiResult || data.aiSummary || '已生成结果'
          this.history.unshift({
            id: data.consultationId || Date.now(),
            type: riskLevelText(data.riskLevel) || 'AI',
            title: text.slice(0, 12),
            preview: this.resultText.slice(0, 24),
            raw: data
          })
          this.inputText = ''
        } else {
          this.resultState = 'error'
          this.resultText = res.data.message || '生成失败'
        }
      } catch (e) {
        this.resultState = 'error'
        this.resultText = '生成失败，请稍后重试'
      }
    },
    clear() {
      this.inputText = ''
      this.resultText = ''
      this.resultState = 'idle'
    },
    openHistory(item) {
      this.selectedHistory = item
    }
  }
}
</script>

<style scoped>
.ai-center-page { padding-bottom: 22px; }
.ai-hero {
  margin: 14px 16px 0;
  padding: 18px;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, var(--medical-blue), var(--medical-blue-dark));
}
.ai-eyebrow { font-size: 12px; opacity: 0.85; margin-bottom: 8px; }
.ai-hero h2 { font-size: 22px; line-height: 1.25; margin-bottom: 6px; }
.ai-hero p { font-size: 13px; line-height: 1.6; opacity: 0.92; }
.ai-tabs { padding-top: 12px; }
.ai-content { display: flex; flex-direction: column; gap: 12px; }
.ai-panel { padding: 14px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 12px; }
.panel-title { font-size: 15px; font-weight: 800; color: var(--ink-strong); }
.panel-badge {
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: var(--medical-blue);
  background: var(--medical-blue-soft);
}
.panel-badge.success { color: var(--medical-green); background: #e8f8f2; }
.panel-badge.error { color: var(--medical-red); background: #fff0f2; }
.ai-input {
  width: 100%;
  min-height: 120px;
  border: 1px solid var(--line-soft);
  border-radius: 14px;
  padding: 12px;
  font: inherit;
  color: var(--ink-main);
  resize: none;
  outline: none;
  background: #fbfdff;
}
.ai-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 12px; }
.ai-result {
  min-height: 84px;
  padding: 12px;
  border-radius: 14px;
  background: #f7fbff;
  color: var(--ink-main);
  line-height: 1.7;
}
.ai-empty {
  min-height: 84px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-muted);
  background: #f7fbff;
  border-radius: 14px;
}
.history-list { display: flex; flex-direction: column; gap: 10px; }
.history-item {
  width: 100%;
  text-align: left;
  padding: 12px;
  border-radius: 14px;
  background: #f9fbfe;
  border: 1px solid var(--line-soft);
  cursor: pointer;
  font: inherit;
}
.history-item:active {
  transform: scale(0.99);
}
.history-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-strong);
}
.history-title {
  flex: 1 1 auto;
  min-width: 0;
  line-height: 1.45;
}
.history-type {
  flex: 0 0 44px;
  min-width: 44px;
  color: var(--medical-blue);
  text-align: center;
  white-space: nowrap;
}
.history-text { margin-top: 6px; color: var(--ink-muted); font-size: 13px; line-height: 1.6; }
.detail-close {
  border: 0;
  border-radius: 999px;
  padding: 5px 12px;
  color: var(--medical-blue);
  background: var(--medical-blue-soft);
  font-size: 12px;
  font-weight: 800;
}
.detail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.detail-row {
  padding: 12px;
  border-radius: 14px;
  background: #f7fbff;
}
.detail-row span {
  display: block;
  margin-bottom: 6px;
  color: var(--ink-muted);
  font-size: 12px;
  font-weight: 800;
}
.detail-row p {
  color: var(--ink-main);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>
