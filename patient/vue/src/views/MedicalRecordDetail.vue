<template>
  <div class="detail-page">
    <div class="page-header">
      <span @click="$router.back()" class="back">‹ 返回</span>
      <span class="title">病历详情</span>
      <span></span>
    </div>
    <div class="content" v-if="record">
      <div class="section">
        <h4>基本信息</h4>
        <div class="field"><span class="label">就诊时间</span><span>{{ record.createdAt?.substring(0,16) }}</span></div>
        <div class="field"><span class="label">状态</span><span>{{ recordStatusText(record.status) }}</span></div>
      </div>
      <div class="section">
        <h4>主诉</h4>
        <p>{{ record.chiefComplaint || '无' }}</p>
      </div>
      <div class="section">
        <h4>现病史</h4>
        <p>{{ record.presentIllness || '无' }}</p>
      </div>
      <div class="section">
        <h4>既往史</h4>
        <p>{{ record.pastHistory || '无' }}</p>
      </div>
      <div class="section">
        <h4>过敏史</h4>
        <p>{{ record.allergyHistory || '无' }}</p>
      </div>
      <div class="section">
        <h4>体格检查</h4>
        <p>{{ record.physicalExam || '无' }}</p>
      </div>
      <div class="section highlight">
        <h4>诊断结果</h4>
        <p class="diagnosis">{{ record.diagnosis || '待诊断' }}</p>
      </div>
      <div class="section highlight">
        <h4>治疗建议</h4>
        <p>{{ record.treatmentAdvice || '无' }}</p>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script>
import axios from 'axios'
import { registrationStatusText } from '@/utils/statusLabels'
export default {
  data() { return { record: null } },
  methods: {
    recordStatusText: registrationStatusText
  },
  async mounted() {
    const id = this.$route.params.id
    const res = await axios.get(`/api/medical-record/detail/${id}`)
    if (res.data.success) this.record = res.data.data
  }
}
</script>

<style scoped>
.detail-page { padding-bottom: 30px; }
.page-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; border-bottom: 1px solid #f0f0f0; }
.back { font-size: 16px; color: #4a90d9; cursor: pointer; }
.title { font-size: 17px; font-weight: 600; }
.content { padding: 16px; }
.section { margin-bottom: 16px; background: #f8f9fa; border-radius: 12px; padding: 14px; }
.section.highlight { background: #e8f4fd; border-left: 3px solid #4a90d9; }
.section h4 { font-size: 14px; color: #4a90d9; margin: 0 0 8px; }
.section p { font-size: 14px; color: #333; margin: 0; line-height: 1.6; }
.field { display: flex; justify-content: space-between; padding: 4px 0; font-size: 14px; }
.field .label { color: #999; }
.diagnosis { font-size: 16px; font-weight: 600; color: #333; }
.loading { text-align: center; padding: 60px; color: #999; }
</style>
