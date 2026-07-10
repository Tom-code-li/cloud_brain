<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    default: '申请详情'
  },
  order: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:modelValue']);

function close() {
  emit('update:modelValue', false);
}
</script>

<template>
  <el-dialog :model-value="modelValue" :title="title" width="860px" @close="close">
    <template v-if="order">
      <div class="request-detail-meta">
        <div><strong>申请单号：</strong>{{ order.orderNo || '-' }}</div>
        <div><strong>状态：</strong>{{ order.status || '-' }}</div>
        <div><strong>金额：</strong>{{ Number(order.totalAmount || 0).toFixed(2) }} 元</div>
        <div><strong>申请时间：</strong>{{ order.appliedAt || '-' }}</div>
      </div>

      <div class="request-detail-grid">
        <div><strong>目的要求：</strong>{{ order.purpose || '无' }}</div>
        <div v-if="order.orderType === '检查'"><strong>检查部位：</strong>{{ order.examSite || '无' }}</div>
        <div v-if="order.orderType === '检验'"><strong>标本类型：</strong>{{ order.specimenType || '无' }}</div>
        <div v-if="order.orderType === '检验'"><strong>优先级：</strong>{{ order.priority || '无' }}</div>
        <div v-if="order.orderType === '检验'"><strong>采样方式：</strong>{{ order.collectionWay || '无' }}</div>
        <div><strong>备注：</strong>{{ order.remark || '无' }}</div>
      </div>

      <el-table :data="order.items || []" border stripe class="dialog-table request-detail-table">
        <el-table-column prop="itemName" label="项目名称" min-width="180" />
        <el-table-column prop="itemType" label="类型" width="90" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">{{ Number(row.unitPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">{{ Number(row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="resultSummary" label="结果摘要" min-width="220" />
      </el-table>
    </template>

    <template #footer>
      <el-button @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>
