<template>
  <div class="detail-page">
    <div class="page-header">
      <span @click="$router.back()" class="back">‹ 返回</span>
      <span class="title">处方详情</span>
      <span></span>
    </div>
    <div class="content" v-if="prescription">
      <div class="section">
        <h4>处方信息</h4>
        <div class="field"><span class="label">编号</span><span>{{ prescription.prescriptionNo }}</span></div>
        <div class="field"><span class="label">诊断</span><span>{{ prescription.diagnosis }}</span></div>
        <div class="field"><span class="label">状态</span><span>{{ prescriptionStatusText(prescription.status) }}</span></div>
        <div class="field"><span class="label">总金额</span><span class="fee">¥{{ prescription.totalAmount }}</span></div>
      </div>
      <div class="section">
        <h4>用药说明</h4>
        <p>{{ prescription.usageNote || '遵医嘱' }}</p>
      </div>
      <div class="section">
        <h4>药品明细</h4>
        <div class="drug-item" v-for="item in items" :key="item.prescriptionItemId">
          <div class="drug-name">{{ item.drugName }}</div>
          <div class="drug-info">
            <span>{{ item.specification }}</span>
            <span>× {{ item.quantity }} {{ item.usageMethod || '' }}</span>
          </div>
          <div class="drug-dosage">
            <span>{{ item.dosage }}</span>
            <span>{{ item.frequency }}</span>
          </div>
          <div class="drug-price">¥{{ item.amount }}</div>
        </div>
        <div v-if="items.length === 0" class="empty">暂无药品明细</div>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script>
import axios from 'axios'
import { prescriptionStatusText } from '@/utils/statusLabels'
export default {
  data() {
    return { prescription: null, items: [] }
  },
  methods: { prescriptionStatusText },
  async mounted() {
    const id = this.$route.params.id
    const [r1, r2] = await Promise.all([
      axios.get(`/api/prescription/detail/${id}`),
      axios.get(`/api/prescription/items/${id}`)
    ])
    if (r1.data.success) this.prescription = r1.data.data
    if (r2.data.success) this.items = r2.data.data
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
.section h4 { font-size: 14px; color: #4a90d9; margin: 0 0 8px; }
.field { display: flex; justify-content: space-between; padding: 4px 0; font-size: 14px; }
.field .label { color: #999; }
.fee { color: #e55; font-weight: 600; }
.section p { font-size: 14px; color: #333; margin: 0; }
.drug-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.drug-item:last-child { border-bottom: none; }
.drug-name { font-size: 15px; font-weight: 500; }
.drug-info { font-size: 13px; color: #666; margin-top: 4px; display: flex; gap: 10px; }
.drug-dosage { font-size: 13px; color: #999; margin-top: 2px; display: flex; gap: 10px; }
.drug-price { font-size: 14px; color: #e55; font-weight: 600; margin-top: 4px; }
.empty { text-align: center; padding: 20px; color: #ccc; }
.loading { text-align: center; padding: 60px; color: #999; }
</style>
