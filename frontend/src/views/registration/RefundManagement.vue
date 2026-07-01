<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import FeeStatusTag from '../../components/FeeStatusTag.vue'
import { checkRefund, refundFee } from '../../api/registration'

const loading = ref(false)
const checkResult = ref(null)
const form = reactive({
  feeOrderId: 1,
  reason: '患者取消就诊'
})

async function check() {
  loading.value = true
  try {
    checkResult.value = await checkRefund(form.feeOrderId)
    if (checkResult.value.refundable) {
      ElMessage.success('当前费用允许退费')
    } else {
      ElMessage.warning(checkResult.value.reason)
    }
  } finally {
    loading.value = false
  }
}

async function process() {
  if (!checkResult.value || checkResult.value.feeOrderId !== form.feeOrderId) {
    await check()
  }
  if (!checkResult.value?.refundable) {
    return
  }
  const result = await refundFee({
    feeOrderId: form.feeOrderId,
    reason: form.reason
  })
  ElMessage.success('退费已完成并生成退费记录')
  checkResult.value = {
    feeOrderId: result.feeOrderId,
    refundable: false,
    reason: '费用单已退费'
  }
}
</script>

<template>
  <div class="refund-management page-surface">
    <el-card class="medical-card" shadow="never">
      <template #header>
        <div>
          <p class="section-title">退号退费</p>
          <p class="section-subtitle">退挂号费校验是否已接诊，退检查/检验费校验是否已执行</p>
        </div>
      </template>
      <el-form class="flat-form" :model="form" label-position="top">
        <el-form-item label="费用单 ID">
          <el-input-number v-model="form.feeOrderId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="退费原因">
          <el-input v-model="form.reason" type="textarea" :rows="4" />
        </el-form-item>
        <div class="action-row">
          <el-button :icon="Search" :loading="loading" @click="check">退费校验</el-button>
          <el-button type="warning" plain :icon="RefreshLeft" @click="process">执行退费</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card class="medical-card" shadow="never">
      <template #header>
        <div>
          <p class="section-title">校验结果</p>
          <p class="section-subtitle">只有校验通过的费用单才能退费</p>
        </div>
      </template>
      <el-empty v-if="!checkResult" description="尚未校验" />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="费用单 ID">{{ checkResult.feeOrderId }}</el-descriptions-item>
        <el-descriptions-item label="是否可退">
          <FeeStatusTag :status="checkResult.refundable ? '可退费' : '不可退'" />
        </el-descriptions-item>
        <el-descriptions-item label="原因">{{ checkResult.reason }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.refund-management {
  display: grid;
  grid-template-columns: minmax(320px, 0.7fr) minmax(0, 1fr);
  gap: 16px;
}

@media (max-width: 900px) {
  .refund-management {
    grid-template-columns: 1fr;
  }
}
</style>
